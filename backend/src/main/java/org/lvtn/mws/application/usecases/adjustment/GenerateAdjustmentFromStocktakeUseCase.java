package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Sinh phiếu điều chỉnh DRAFT từ phiên kiểm kê (kích hoạt thủ công nếu cần). */
@Service
@RequiredArgsConstructor
public class GenerateAdjustmentFromStocktakeUseCase {

    private final AdjustmentDomainService adjustmentDomainService;

    @Transactional
    public AdjustmentVoucher execute(String sessionId, String createdBy) {
        return adjustmentDomainService.generateFromStocktake(sessionId, createdBy);
    }
}
