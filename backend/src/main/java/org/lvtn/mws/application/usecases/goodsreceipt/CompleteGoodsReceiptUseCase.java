package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Completes a goods receipt and runs the full putaway / inventory-sync chain.
 * Wrapped in @Transactional; retried on optimistic-lock contention because the
 * inventory aggregate (Stage-2) is guarded by @Version.
 *
 * Dùng vòng lặp retry thủ công (không phụ thuộc Spring Retry) theo đúng mẫu
 * CommitStockDeductionUseCase — nhất quán toàn dự án và không cần @EnableRetry.
 */
@Service
@RequiredArgsConstructor
public class CompleteGoodsReceiptUseCase {

    private final GoodsReceiptDomainService domainService;

    @Transactional
    public GoodsReceipt execute(String grnId) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return domainService.complete(grnId);
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