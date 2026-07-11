package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAdjustmentVoucherByIdUseCase {

    private final AdjustmentDomainService adjustmentDomainService;
    private final CreationDateScope creationDateScope;

    public AdjustmentVoucher execute(String id) {
        AdjustmentVoucher rec = adjustmentDomainService.findById(id);
        creationDateScope.assertVisible(rec.getCreatedAt());
        return rec;
    }
}
