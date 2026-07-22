package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BatchSuggestion;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocateBatchesUseCase {
    private final InventoryDomainService domainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    @Transactional(readOnly = true)
    public List<BatchSuggestion> execute(String productId, String warehouseId, int neededQuantity) {
        // A2: chặn gợi ý FEFO của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return domainService.allocateBatchesForPicking(productId, warehouseId, neededQuantity);
    }
}
