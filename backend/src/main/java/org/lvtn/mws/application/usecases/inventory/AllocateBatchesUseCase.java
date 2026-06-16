package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BatchSuggestion;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllocateBatchesUseCase {
    private final InventoryDomainService domainService;

    @Transactional(readOnly = true)
    public List<BatchSuggestion> execute(String productId, String warehouseId, int neededQuantity) {
        return domainService.allocateBatchesForPicking(productId, warehouseId, neededQuantity);
    }
}
