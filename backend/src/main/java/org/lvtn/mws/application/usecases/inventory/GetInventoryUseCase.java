package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetInventoryUseCase {
    private final InventoryDomainService domainService;

    @Transactional(readOnly = true)
    public Inventory executeByProductAndWarehouse(String productId, String warehouseId) {
        return domainService.findByProductAndWarehouse(productId, warehouseId);
    }

    @Transactional(readOnly = true)
    public List<Inventory> executeByWarehouse(String warehouseId) {
        return domainService.findByWarehouse(warehouseId);
    }
}
