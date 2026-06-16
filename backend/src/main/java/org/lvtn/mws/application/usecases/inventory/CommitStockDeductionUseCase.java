package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BatchDeductionRequest;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommitStockDeductionUseCase {
    private final InventoryDomainService domainService;

    @Transactional
    public void execute(String productId, String warehouseId, List<BatchDeductionRequest> details) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                domainService.commitStockDeduction(productId, warehouseId, details);
                return;
            } catch (OptimisticLockingFailureException ex) {
                if (attempt == maxAttempts) throw ex;
                try { Thread.sleep(100L * attempt); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }
}
