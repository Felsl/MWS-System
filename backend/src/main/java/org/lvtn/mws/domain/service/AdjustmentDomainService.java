package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.AdjustmentApprovalPolicy;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.AdjustmentVoucherDetail;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.model.UnauthorizedAdjustmentException;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherNumberGenerator;
import org.lvtn.mws.domain.repository.IAdjustmentVoucherRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.domain.repository.IStocktakeDetailRepository;
import org.lvtn.mws.domain.repository.IStocktakeSessionRepository;

import java.util.List;
import java.util.Set;

/**
 * Nghiệp vụ Điều chỉnh tồn kho (Giai đoạn 6) — thuần Java.
 *
 *  - generateFromStocktake: từ các dòng kiểm kê lệch khác 0, sinh 1 phiếu điều chỉnh DRAFT.
 *  - approveAdjustmentVoucher: kiểm tra thẩm quyền theo % chênh lệch, rồi áp tồn kho
 *    (inventory_batches + inventory) kèm ghi thẻ kho. Ranh giới @Transactional + retry
 *    optimistic-lock áp ở tầng UseCase (Inventory có @Version).
 */
public class AdjustmentDomainService {

    private final IAdjustmentVoucherRepository voucherRepository;
    private final IStocktakeSessionRepository sessionRepository;
    private final IStocktakeDetailRepository detailRepository;
    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final IIdGenerator idGenerator;
    private final IAdjustmentVoucherNumberGenerator numberGenerator;

    public AdjustmentDomainService(IAdjustmentVoucherRepository voucherRepository,
                                   IStocktakeSessionRepository sessionRepository,
                                   IStocktakeDetailRepository detailRepository,
                                   IInventoryRepository inventoryRepository,
                                   IInventoryBatchRepository batchRepository,
                                   IStockMovementRepository stockMovementRepository,
                                   IIdGenerator idGenerator,
                                   IAdjustmentVoucherNumberGenerator numberGenerator) {
        this.voucherRepository       = voucherRepository;
        this.sessionRepository       = sessionRepository;
        this.detailRepository        = detailRepository;
        this.inventoryRepository     = inventoryRepository;
        this.batchRepository         = batchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.idGenerator             = idGenerator;
        this.numberGenerator         = numberGenerator;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public AdjustmentVoucher findById(String id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu điều chỉnh: " + id));
    }

    public List<AdjustmentVoucher> findAll() { return voucherRepository.findAll(); }

    // ── 1. Sinh phiếu điều chỉnh (DRAFT) từ phiên kiểm kê ────────────────────

    /**
     * Tạo phiếu DRAFT gồm các dòng có chênh lệch (difference != 0). Trả về null nếu
     * không có chênh lệch nào (không tạo phiếu rỗng).
     */
    public AdjustmentVoucher generateFromStocktake(String sessionId, String createdBy) {
        StocktakeSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên kiểm kê: " + sessionId));

        if (!voucherRepository.findBySessionId(sessionId).isEmpty()) {
            throw new IllegalStateException("Phiên " + sessionId + " đã sinh phiếu điều chỉnh trước đó");
        }

        List<StocktakeDetail> details = detailRepository.findBySessionId(sessionId);
        List<StocktakeDetail> diffs = details.stream()
                .filter(StocktakeDetail::hasDifference)
                .toList();
        if (diffs.isEmpty()) {
            return null; // không có chênh lệch -> không cần phiếu
        }

        String voucherId = idGenerator.generate();
        AdjustmentVoucher voucher = AdjustmentVoucher.builder()
                .id(voucherId)
                .voucherNumber(generateVoucherNumber())
                .warehouseId(session.getWarehouseId())
                .sessionId(sessionId)
                .reason("Điều chỉnh từ kiểm kê phiên " + sessionId)
                .status(AdjustmentVoucher.Status.DRAFT)
                .createdBy(createdBy)
                .build();

        for (StocktakeDetail d : diffs) {
            voucher.addDetail(AdjustmentVoucherDetail.builder()
                    .id(idGenerator.generate())
                    .productId(d.getProductId())
                    .batchId(d.getBatchId())
                    .binLocationId(d.getBinLocationId())
                    .quantityChange(d.getDifference())          // có dấu
                    .beforeQuantity(d.getSystemQuantity())
                    .afterQuantity(d.getCountedQuantity())
                    .stocktakeDetailId(d.getId())
                    .build());
        }
        return voucherRepository.save(voucher);
    }

    // ── 2. Duyệt phiếu + áp tồn kho ──────────────────────────────────────────

    /**
     * Duyệt phiếu điều chỉnh:
     *   1) Kiểm tra thẩm quyền theo % chênh lệch lớn nhất (policy phân tầng).
     *   2) Với mỗi dòng: cập nhật lô (inventory_batches) + tồn tổng (inventory) + ghi thẻ kho.
     *   3) Chuyển phiếu DRAFT -> APPROVED.
     *
     * @param approverAuthorities tập authority của người duyệt (lấy từ SecurityContext ở tầng controller)
     * @param approvedBy          userId người duyệt (FK users.id)
     */
    public AdjustmentVoucher approveAdjustmentVoucher(String voucherId,
                                                      Set<String> approverAuthorities,
                                                      String approvedBy,
                                                      AdjustmentApprovalPolicy policy) {
        AdjustmentVoucher voucher = findById(voucherId);
        if (voucher.getStatus() != AdjustmentVoucher.Status.DRAFT) {
            throw new IllegalStateException("Phiếu không ở trạng thái DRAFT, không thể duyệt");
        }

        // (1) Phân quyền theo ngưỡng %
        String required = policy.requiredAuthorityFor(voucher.maxVariancePercent());
        if (required != null && !required.isBlank()
                && (approverAuthorities == null || !approverAuthorities.contains(required))) {
            throw new UnauthorizedAdjustmentException(
                    "Mức chênh lệch " + String.format("%.1f", voucher.maxVariancePercent())
                            + "% vượt ngưỡng cho phép — cần quyền '" + required + "' để duyệt");
        }

        // (2) Áp tồn kho cho từng dòng
        for (AdjustmentVoucherDetail line : voucher.getDetails()) {
            applyLine(voucher, line, approvedBy);
        }

        // (3) Hoàn tất
        voucher.approve(approvedBy);
        return voucherRepository.save(voucher);
    }

    private void applyLine(AdjustmentVoucher voucher, AdjustmentVoucherDetail line, String approvedBy) {
        int change = line.getQuantityChange();
        if (change == 0) return;

        // 2a) Cập nhật lô vật lý (nếu dòng gắn lô)
        if (line.getBatchId() != null) {
            InventoryBatch batch = batchRepository.findById(line.getBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô: " + line.getBatchId()));
            if (change > 0) batch.addQuantity(change);
            else            batch.deduct(-change); // chk_batch_qty >= 0 do DB chốt chặn
            batchRepository.save(batch);
        }

        // 2b) Cập nhật tồn tổng (inventory) — chịu optimistic locking qua @Version
        Inventory inv = inventoryRepository
                .findByProductIdAndWarehouseId(line.getProductId(), voucher.getWarehouseId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy tồn tổng cho sản phẩm " + line.getProductId()
                                + " tại kho " + voucher.getWarehouseId()));
        int before = inv.getQuantity();
        if (change > 0) inv.increaseQuantity(change);
        else            inv.decreaseQuantity(-change);
        int after = before + change;
        inventoryRepository.save(inv);

        // 2c) Ghi thẻ kho (audit trail) ADJUST_IN / ADJUST_OUT
        StockMovement movement = StockMovement.builder()
                .id(idGenerator.generate())
                .productId(line.getProductId())
                .warehouseId(voucher.getWarehouseId())
                .batchId(line.getBatchId())
                .movementType(change > 0 ? StockMovement.MovementType.ADJUST_IN
                                         : StockMovement.MovementType.ADJUST_OUT)
                .quantityChange(change)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceType("ADJUSTMENT")
                .referenceId(voucher.getId())
                .note(voucher.getReason())
                .createdBy(approvedBy)
                .build();
        stockMovementRepository.save(movement);
    }

    private String generateVoucherNumber() {
        String number;
        do {
            number = numberGenerator.next();
        } while (voucherRepository.existsByVoucherNumber(number));
        return number;
    }
}
