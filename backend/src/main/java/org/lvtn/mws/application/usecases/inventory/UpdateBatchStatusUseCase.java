package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateBatchStatusUseCase {
    private final InventoryDomainService domainService;

    @Transactional
    public InventoryBatch execute(String batchId, InventoryBatch.Status status) {
        return domainService.updateBatchStatus(batchId, status);
    }
}
