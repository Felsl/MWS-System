package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hoàn tất phiên kiểm kê: mở băng kho (FREEZED -> ADJUSTED) rồi tự sinh phiếu điều chỉnh
 * DRAFT từ các dòng lệch. Cả hai chạy trong cùng một giao dịch.
 */
@Service
@RequiredArgsConstructor
public class CompleteStocktakeSessionUseCase {

    private final StocktakeDomainService stocktakeDomainService;
    private final AdjustmentDomainService adjustmentDomainService;

    /** voucher có thể null khi không có chênh lệch nào. */
    public record Result(StocktakeSession session, AdjustmentVoucher voucher) {}

    @Transactional
    public Result execute(String sessionId, String createdBy) {
        StocktakeSession session = stocktakeDomainService.completeStocktakeSession(sessionId);
        AdjustmentVoucher voucher = adjustmentDomainService.generateFromStocktake(sessionId, createdBy);
        return new Result(session, voucher);
    }
}
