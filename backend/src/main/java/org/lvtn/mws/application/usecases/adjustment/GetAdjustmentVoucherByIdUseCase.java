package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdjustmentVoucherByIdUseCase {

    private final AdjustmentDomainService adjustmentDomainService;

    public AdjustmentVoucher execute(String id) {
        return adjustmentDomainService.findById(id);
    }
}
