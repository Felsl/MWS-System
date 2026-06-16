package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InitInventoryUseCase {
    private final InventoryDomainService domainService;

    @Transactional
    public Inventory execute(String productId, String warehouseId) {
        return domainService.initInventory(productId, warehouseId);
    }
}
