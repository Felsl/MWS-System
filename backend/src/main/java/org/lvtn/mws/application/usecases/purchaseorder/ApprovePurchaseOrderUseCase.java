package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovePurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;

    @Transactional
    public PurchaseOrder execute(String poId, String approvedBy) {
        return domainService.approve(poId, approvedBy);
    }
}
