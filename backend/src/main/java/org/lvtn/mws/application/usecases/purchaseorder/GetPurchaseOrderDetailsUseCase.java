package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPurchaseOrderDetailsUseCase {
    private final PurchaseOrderDomainService domainService;

    public List<PurchaseOrderDetail> execute(String poId) {
        return domainService.findDetails(poId);
    }
}
