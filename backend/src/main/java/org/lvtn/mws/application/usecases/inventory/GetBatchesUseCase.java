package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBatchesUseCase {
    private final InventoryDomainService domainService;

    @Transactional(readOnly = true)
    public List<InventoryBatch> execute(String productId, String warehouseId) {
        return domainService.findBatchesByProductAndWarehouse(productId, warehouseId);
    }
}
