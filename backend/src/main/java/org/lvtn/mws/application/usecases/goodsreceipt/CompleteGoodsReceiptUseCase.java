package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Completes a goods receipt and runs the full putaway / inventory-sync chain.
 * Wrapped in @Transactional; retried on optimistic-lock contention because the
 * inventory aggregate (Stage-2) is guarded by @Version.
 */
@Service
@RequiredArgsConstructor
public class CompleteGoodsReceiptUseCase {
    private final GoodsReceiptDomainService domainService;

    @Retryable(
        retryFor = OptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public GoodsReceipt execute(String grnId) {
        return domainService.complete(grnId);
    }
}
