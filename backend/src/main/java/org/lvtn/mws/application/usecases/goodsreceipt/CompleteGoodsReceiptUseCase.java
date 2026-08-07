package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.event.StockMovementEvent;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.repository.IGoodsReceiptRepository;
import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.infrastructure.capacity.BinCapacityGuard;
import java.util.List;
import org.lvtn.mws.domain.model.GoodsReceiptCompletion;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Completes a goods receipt and runs the full putaway / inventory-sync chain.
 * Wrapped in @Transactional; retried on optimistic-lock contention because the
 * inventory aggregate (Stage-2) is guarded by @Version.
 *
 * [GIAI ĐOẠN 7] Việc tăng tồn vẫn chạy trong transaction; thẻ kho IN được phát qua
 * {@link StockMovementEvent} và ghi ở AFTER_COMMIT (StockMovementAuditListener) — chỉ ghi khi
 * phiếu nhập đã commit 100%.
 *
 * Dùng vòng lặp retry thủ công (không phụ thuộc Spring Retry) theo đúng mẫu
 * CommitStockDeductionUseCase — nhất quán toàn dự án và không cần @EnableRetry.
 */
@Service
@RequiredArgsConstructor
public class CompleteGoodsReceiptUseCase {

    private final GoodsReceiptDomainService domainService;
    private final ApplicationEventPublisher eventPublisher;
    private final IGoodsReceiptRepository goodsReceiptRepository;
    private final IGoodsReceiptDetailRepository goodsReceiptDetailRepository;
    private final BinCapacityGuard binCapacityGuard;

    @Transactional
    public GoodsReceipt execute(String grnId) {
        // [PA1] Chặn cứng trước khi hoàn tất: tổng hàng putaway theo ô kệ không được vượt sức chứa -> 409.
        GoodsReceipt grn = goodsReceiptRepository.findById(grnId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập: " + grnId));
        List<BinCapacityGuard.Incoming> incomings = goodsReceiptDetailRepository.findByGrnId(grnId).stream()
                .map(d -> new BinCapacityGuard.Incoming(d.getProductId(), d.getBinLocationId(), d.getQuantity()))
                .toList();
        binCapacityGuard.assertFits(grn.getWarehouseId(), incomings);

        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                GoodsReceiptCompletion completion = domainService.complete(grnId);
                // Phát sự kiện trong transaction → AuditListener ghi thẻ kho khi commit xong.
                for (StockMovement movement : completion.movements()) {
                    eventPublisher.publishEvent(new StockMovementEvent(movement));
                }
                return completion.goodsReceipt();
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == maxAttempts) throw ex;
                try {
                    Thread.sleep(100L * attempt); // backoff: 100ms, 200ms
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        // Không bao giờ tới đây (vòng lặp luôn return hoặc ném).
        throw new IllegalStateException("Không thể hoàn tất phiếu nhập sau " + maxAttempts + " lần thử");
    }
}
