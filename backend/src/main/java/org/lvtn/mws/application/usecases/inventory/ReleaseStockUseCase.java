package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReleaseStockUseCase {
    private final InventoryDomainService domainService;

    @Transactional
    public Inventory execute(String productId, String warehouseId, int quantity) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return domainService.release(productId, warehouseId, quantity);
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == maxAttempts) throw ex;
                try { Thread.sleep(100L * attempt); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }
}
