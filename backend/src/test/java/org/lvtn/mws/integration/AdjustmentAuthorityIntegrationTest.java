package org.lvtn.mws.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.AdjustmentApprovalPolicy;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.AdjustmentVoucherDetail;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.UnauthorizedAdjustmentException;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TẦNG 3 — PHÂN QUYỀN DUYỆT ĐIỀU CHỈNH THEO NGƯỠNG %.
 *
 * <p>Chính sách phân tầng: chênh lệch càng lớn thì càng cần quyền cao để duyệt.
 * Kiểm chứng ba nhánh:
 * <ol>
 *   <li>Chênh lệch VƯỢT ngưỡng nhưng người duyệt THIẾU quyền tầng cao -> {@link UnauthorizedAdjustmentException}.</li>
 *   <li>Chênh lệch VƯỢT ngưỡng và người duyệt CÓ đủ quyền -> duyệt thành công, áp tồn kho.</li>
 *   <li>Chênh lệch nhỏ (tầng thấp nhất) -> không cần quyền đặc biệt, duyệt được.</li>
 * </ol>
 *
 * <p>Policy dùng trong test: ngưỡng tier2/3/4 = 5% / 10% / 20%; authority tương ứng.
 * <p>{@code @Transactional}: mỗi test tự rollback. Yêu cầu Docker.
 */
@Transactional
class AdjustmentAuthorityIntegrationTest extends AbstractIntegrationTest {

    private static final String WH = "WH-ADJ";
    private static final String PROD = "P-ADJ-1";
    private static final String BIN = "BIN-ADJ-1";

    // tier2=5%, tier3=10%, tier4=20%; tier1 để rỗng (chênh lệch nhỏ không cần quyền đặc biệt)
    private static final AdjustmentApprovalPolicy POLICY = new AdjustmentApprovalPolicy(
            5.0, 10.0, 20.0, "", "ADJ_T2", "ADJ_T3", "ADJ_T4");

    @Autowired AdjustmentDomainService adjustmentService;
    @Autowired IAdjustmentVoucherRepository voucherRepository;
    @Autowired IInventoryRepository inventoryRepository;

    /**
     * Tạo phiếu điều chỉnh DRAFT gồm 1 dòng, với % chênh lệch = |change| / before * 100.
     *
     * <p>Các trường BẮT BUỘC của domain model (Objects.requireNonNull):
     * phiếu cần {@code id, voucherNumber, warehouseId, reason}; dòng cần {@code id, productId, binLocationId}.
     * {@code batchId} để null = điều chỉnh trên tồn tổng, không đụng lô vật lý
     * (applyLine bỏ qua bước cập nhật lô khi batchId null).
     */
    private AdjustmentVoucher seedVoucher(String id, int beforeQty, int change) {
        AdjustmentVoucherDetail line = AdjustmentVoucherDetail.builder()
                .id(id + "-D1")
                .voucherId(id)
                .productId(PROD)
                .binLocationId(BIN)              // BẮT BUỘC
                .beforeQuantity(beforeQty)
                .quantityChange(change)
                .afterQuantity(beforeQty + change)
                .build();

        AdjustmentVoucher v = AdjustmentVoucher.builder()
                .id(id)
                .voucherNumber("ADJ-" + id)      // BẮT BUỘC + UNIQUE ở tầng CSDL
                .warehouseId(WH)
                .reason("Lệch kiểm kê")
                .status(AdjustmentVoucher.Status.DRAFT)
                .createdBy("auditor")
                .createdAt(LocalDateTime.now())
                .details(List.of(line))
                .build();
        return voucherRepository.save(v);
    }

    private void seedInventory(int qty) {
        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(WH).quantity(qty).reservedQuantity(0).version(0).build());
    }

    @Test
    @DisplayName("Vượt ngưỡng (25%) nhưng người duyệt thiếu quyền tầng cao -> bị từ chối (403)")
    void approveOverThreshold_withoutAuthority_rejected() {
        // before=100, change=-25 -> variance 25% > 20% (tier4) -> cần quyền ADJ_T4
        AdjustmentVoucher v = seedVoucher("ADJ-OVER-1", 100, -25);
        assertThat(v.maxVariancePercent()).isEqualTo(25.0);

        assertThatThrownBy(() -> adjustmentService.approveAdjustmentVoucher(
                v.getId(), Set.of("ADJ_T2"), "manager", POLICY))
                .isInstanceOf(UnauthorizedAdjustmentException.class);

        // Phiếu vẫn ở DRAFT (chưa duyệt).
        assertThat(adjustmentService.findById(v.getId()).getStatus())
                .isEqualTo(AdjustmentVoucher.Status.DRAFT);
    }

    @Test
    @DisplayName("Vượt ngưỡng (25%) và người duyệt đủ quyền ADJ_T4 -> duyệt được, áp tồn kho")
    void approveOverThreshold_withAuthority_succeeds() {
        seedInventory(100);
        AdjustmentVoucher v = seedVoucher("ADJ-OVER-2", 100, -25);

        AdjustmentVoucher approved = adjustmentService.approveAdjustmentVoucher(
                v.getId(), Set.of("ADJ_T4"), "director", POLICY);

        assertThat(approved.getStatus()).isEqualTo(AdjustmentVoucher.Status.APPROVED);
        // Tồn kho được áp theo dòng điều chỉnh: 100 - 25 = 75.
        assertThat(inventoryRepository.findByProductIdAndWarehouseId(PROD, WH).orElseThrow()
                .getQuantity()).isEqualTo(75);
    }

    @Test
    @DisplayName("Chênh lệch nhỏ (3%, tầng thấp nhất) -> không cần quyền đặc biệt, duyệt được")
    void approveWithinLowestTier_needsNoSpecialAuthority() {
        seedInventory(100);
        // before=100, change=-3 -> variance 3% <= 5% (ngưỡng tier2) -> tier1Authority = "" (không yêu cầu)
        AdjustmentVoucher v = seedVoucher("ADJ-LOW-1", 100, -3);

        AdjustmentVoucher approved = adjustmentService.approveAdjustmentVoucher(
                v.getId(), Set.of(), "staff", POLICY);

        assertThat(approved.getStatus()).isEqualTo(AdjustmentVoucher.Status.APPROVED);
        assertThat(inventoryRepository.findByProductIdAndWarehouseId(PROD, WH).orElseThrow()
                .getQuantity()).isEqualTo(97);
    }
}
