package org.lvtn.mws.application.usecases.purchaseorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.domain.model.PurchaseOrderLineCommand;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreatePurchaseOrderUseCase {
    private final PurchaseOrderDomainService domainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    @Transactional
    public PurchaseOrder execute(String supplierId, String warehouseId, LocalDate expectedDate,
                                 String createdBy, List<PurchaseOrderLineCommand> lines) {
        // A2: chặn tạo phiếu mua nhập về kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return domainService.create(supplierId, warehouseId, expectedDate, createdBy, lines);
    }
}
