package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllAdjustmentVouchersUseCase {

    private final AdjustmentDomainService adjustmentDomainService;

    public List<AdjustmentVoucher> execute() {
        return adjustmentDomainService.findAll();
    }
}
