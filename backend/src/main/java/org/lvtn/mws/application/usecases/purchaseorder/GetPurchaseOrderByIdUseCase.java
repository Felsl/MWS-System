package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lvtn.mws.infrastructure.security.scope.CreationDateScope;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPurchaseOrderByIdUseCase {
    private final PurchaseOrderDomainService domainService;
    private final CreationDateScope creationDateScope;

    public PurchaseOrder execute(String poId) {
        PurchaseOrder rec = domainService.findById(poId);
        creationDateScope.assertVisible(rec.getCreatedAt());
        return rec;
    }
}
