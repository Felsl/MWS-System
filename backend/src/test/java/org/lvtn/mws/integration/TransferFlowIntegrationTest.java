package org.lvtn.mws.integration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TẦNG 3 — Chặng ĐẦU của luồng điều chuyển trên MySQL thật: lập phiếu -> gửi duyệt (giữ chỗ tồn nguồn)
 * -> duyệt.
 *
 * <p><b>Cập nhật theo INC3:</b> bước DUYỆT KHÔNG còn gán lô theo FEFO nữa — việc chọn lô đã dời sang
 * bước gom hàng (picking). Vì vậy test cũ (kỳ vọng approve gán sẵn batchId, và dispatch chạy thẳng
 * không qua picking) đã lỗi thời và được thay bằng file này. Toàn bộ luồng end-to-end
 * (approve -> picking -> dispatch -> receipt) hiện được khoá hồi quy ở
 * {@code TransferPickingIntegrationTest}.
 *
 * <p>{@code @Transactional}: mỗi test tự rollback.
 */
@Transactional
class TransferFlowIntegrationTest extends AbstractIntegrationTest {

    private static final String SRC = "WH-SRC-T";
    private static final String DST = "WH-DST-T";
    private static final String PROD = "P-TRANSFER-1";

    @Autowired TransferOrderDomainService transferService;
    @Autowired IInventoryRepository inventoryRepository;
    @Autowired IInventoryBatchRepository batchRepository;
    @PersistenceContext EntityManager em;

    private void seedWarehouse(String id, String code) {
        em.createNativeQuery(
                "INSERT INTO warehouses (id, code, name, address) VALUES (?, ?, ?, ?)")
                .setParameter(1, id).setParameter(2, code)
                .setParameter(3, "Kho " + code).setParameter(4, "Địa chỉ " + code)
                .executeUpdate();
    }

    private InventoryBatch batch(String id, String bin, int qty, LocalDate expiry) {
        return new InventoryBatch.Builder()
                .id(id).productId(PROD).warehouseId(SRC).binLocationId(bin)
                .batchNumber("LOT-" + id).quantity(qty).expiryDate(expiry)
                .status(InventoryBatch.Status.ACTIVE).createdAt(LocalDateTime.now()).version(0)
                .build();
    }

    @Test
    @DisplayName("Lập -> gửi duyệt (giữ chỗ 40 ở kho nguồn) -> duyệt (INC3: KHÔNG gán lô, chờ picking)")
    void createRequestApprove_reservesSourceStock_noBatchAllocationAtApprove() {
        // Seed 2 kho + tồn nguồn 100 + 2 lô (để chứng minh approve KHÔNG đụng tới lô).
        seedWarehouse(SRC, "SRC-T");
        seedWarehouse(DST, "DST-T");
        em.flush();

        inventoryRepository.save(new Inventory.Builder()
                .productId(PROD).warehouseId(SRC).quantity(100).reservedQuantity(0).version(0).build());
        batchRepository.save(batch("BATCH-A", "BIN-A", 30, LocalDate.now().plusDays(10)));
        batchRepository.save(batch("BATCH-B", "BIN-B", 100, LocalDate.now().plusDays(60)));

        // Lập phiếu điều chuyển 40 đơn vị SRC -> DST.
        TransferOrder created = transferService.createTransferOrder(
                SRC, DST, "u1", List.of(new NewTransferLine(PROD, 40)));
        assertThat(created.getStatus()).isEqualTo(TransferOrder.Status.DRAFT);

        // Gửi duyệt: giữ chỗ 40 tại tồn nguồn (khả dụng còn 60).
        transferService.requestTransferApproval(created.getId());
        Inventory srcInv = inventoryRepository
                .findByProductIdAndWarehouseId(PROD, SRC).orElseThrow();
        assertThat(srcInv.getReservedQuantity()).isEqualTo(40);
        assertThat(srcInv.availableQuantity()).isEqualTo(60);

        // Duyệt: chỉ chuyển trạng thái sang APPROVED; INC3 KHÔNG gán lô (batchId vẫn rỗng).
        TransferOrder approved = transferService.approveTransferOrder(created.getId(), "boss");
        assertThat(approved.getStatus()).isEqualTo(TransferOrder.Status.APPROVED);

        List<TransferOrderDetail> lines = approved.getDetails();
        assertThat(lines.stream().mapToInt(TransferOrderDetail::getQuantity).sum()).isEqualTo(40);
        assertThat(lines).allSatisfy(d ->
                assertThat(d.getBatchId()).isNull()); // việc chọn lô dời sang bước picking

        // Tồn vật lý & các lô CHƯA bị trừ ở bước duyệt.
        assertThat(srcInv.getQuantity()).isEqualTo(100);
        assertThat(batchRepository.findById("BATCH-A").orElseThrow().getQuantity()).isEqualTo(30);
        assertThat(batchRepository.findById("BATCH-B").orElseThrow().getQuantity()).isEqualTo(100);
    }
}
