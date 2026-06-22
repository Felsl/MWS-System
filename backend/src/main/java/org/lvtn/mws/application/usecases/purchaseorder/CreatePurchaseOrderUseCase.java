package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderLineCommand;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatePurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;

    @Transactional
    public PurchaseOrder execute(String supplierId, String warehouseId, LocalDate expectedDate,
                                 String createdBy, List<PurchaseOrderLineCommand> lines) {
        return domainService.create(supplierId, warehouseId, expectedDate, createdBy, lines);
    }
}
