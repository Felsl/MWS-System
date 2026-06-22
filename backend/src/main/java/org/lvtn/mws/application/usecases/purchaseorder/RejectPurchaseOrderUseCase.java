package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectPurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;

    @Transactional
    public PurchaseOrder execute(String poId) {
        return domainService.reject(poId);
    }
}
