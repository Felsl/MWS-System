package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.lvtn.mws.infrastructure.service.BatchNumberGeneratorService;
import org.lvtn.mws.infrastructure.service.IdGeneratorService;
import org.lvtn.mws.infrastructure.capacity.BinCapacityGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateInventoryBatchUseCase {
    private final InventoryDomainService domainService;
    private final IdGeneratorService idGenerator;
    private final BatchNumberGeneratorService batchNumberGenerator;
    private final BinCapacityGuard binCapacityGuard;

    @Transactional
    public InventoryBatch execute(String productId, String warehouseId, String binLocationId,
                                  int quantity, LocalDate expiryDate, LocalDate manufacturedDate) {
        // [PA1] Chặn cứng: đưa hàng vào ô kệ vượt sức chứa -> 409.
        binCapacityGuard.assertBatchFits(warehouseId, binLocationId, productId, quantity);
        InventoryBatch batch = new InventoryBatch.Builder()
                .id(idGenerator.generate())
                .productId(productId)
                .warehouseId(warehouseId)
                .binLocationId(binLocationId)
                .batchNumber(batchNumberGenerator.generate())
                .quantity(quantity)
                .expiryDate(expiryDate)
                .manufacturedDate(manufacturedDate)
                .status(InventoryBatch.Status.ACTIVE)
                .build();
        return domainService.createBatch(batch);
    }
}
