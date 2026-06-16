package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.*;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IProductRepository;
import java.util.ArrayList;
import java.util.List;

public class InventoryDomainService {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository batchRepository;
    private final IProductRepository productRepository;

    public InventoryDomainService(IInventoryRepository inventoryRepository,
                                  IInventoryBatchRepository batchRepository,
                                  IProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.batchRepository     = batchRepository;
        this.productRepository   = productRepository;
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public Inventory findByProductAndWarehouse(String productId, String warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tồn kho cho sản phẩm " + productId + " tại kho " + warehouseId));
    }

    public List<Inventory> findByWarehouse(String warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    // ── Core inventory mutation ───────────────────────────────────────────────

    /**
     * Initialise an inventory record when a product is first stocked in a warehouse.
     */
    public Inventory initInventory(String productId, String warehouseId) {
        validateProductExists(productId);
        inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .ifPresent(inv -> { throw new IllegalStateException("Tồn kho đã tồn tại"); });
        Inventory inv = new Inventory.Builder()
                .productId(productId).warehouseId(warehouseId).build();
        return inventoryRepository.save(inv);
    }

    /**
     * Increase total inventory (called on goods receipt commit).
     */
    public Inventory addStock(String productId, String warehouseId, int qty) {
        Inventory inv = findByProductAndWarehouse(productId, warehouseId);
        inv.addStock(qty);
        return inventoryRepository.save(inv);
    }

    /**
     * Reserve stock for PENDING order — pure domain logic, optimistic locking applied at infra.
     */
    public Inventory reserve(String productId, String warehouseId, int qty) {
        Inventory inv = findByProductAndWarehouse(productId, warehouseId);
        inv.reserve(qty);
        return inventoryRepository.save(inv);
    }

    /**
     * Release previously reserved stock (order cancelled).
     */
    public Inventory release(String productId, String warehouseId, int qty) {
        Inventory inv = findByProductAndWarehouse(productId, warehouseId);
        inv.release(qty);
        return inventoryRepository.save(inv);
    }

    // ── FEFO Allocation ───────────────────────────────────────────────────────

    /**
     * FEFO algorithm: returns picking suggestions sorted by nearest expiry date first.
     */
    public List<BatchSuggestion> allocateBatchesForPicking(String productId, String warehouseId,
                                                            int neededQuantity) {
        if (neededQuantity <= 0) throw new IllegalArgumentException("Số lượng cần lấy phải lớn hơn 0");

        List<InventoryBatch> batches = batchRepository.findActiveBatchesForPicking(productId, warehouseId);
        List<BatchSuggestion> suggestions = new ArrayList<>();
        int remaining = neededQuantity;

        for (InventoryBatch batch : batches) {
            if (remaining <= 0) break;
            int take = Math.min(batch.getQuantity(), remaining);
            suggestions.add(new BatchSuggestion(
                    batch.getId(), batch.getBatchNumber(), batch.getBinLocationId(), take));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new InsufficientStockException(
                    "Không đủ hàng trong các lô ACTIVE: còn thiếu " + remaining + " đơn vị");
        }
        return suggestions;
    }

    // ── Commit deduction (physical stock leaves warehouse) ───────────────────

    /**
     * Transaction: decrease total inventory + reserved, then deduct each batch.
     * Called when shipment is confirmed (SHIPPED/COMPLETED).
     */
    public void commitStockDeduction(String productId, String warehouseId,
                                     List<BatchDeductionRequest> details) {
        int totalQty = details.stream().mapToInt(BatchDeductionRequest::getQuantity).sum();

        // 1. Deduct aggregate inventory
        Inventory inv = findByProductAndWarehouse(productId, warehouseId);
        inv.commitDeduction(totalQty);
        inventoryRepository.save(inv);

        // 2. Deduct each batch
        List<InventoryBatch> updatedBatches = new ArrayList<>();
        for (BatchDeductionRequest req : details) {
            InventoryBatch batch = batchRepository.findById(req.getBatchId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô: " + req.getBatchId()));
            batch.deduct(req.getQuantity());
            updatedBatches.add(batch);
        }
        batchRepository.saveAll(updatedBatches);
    }

    // ── Batch management ─────────────────────────────────────────────────────

    public InventoryBatch createBatch(InventoryBatch batch) {
        return batchRepository.save(batch);
    }

    public InventoryBatch updateBatchStatus(String batchId, InventoryBatch.Status status) {
        InventoryBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lô: " + batchId));
        switch (status) {
            case ACTIVE  -> batch.activate();
            case HOLD    -> batch.hold();
            case EXPIRED -> batch.markExpired();
        }
        return batchRepository.save(batch);
    }

    public List<InventoryBatch> findBatchesByProductAndWarehouse(String productId, String warehouseId) {
        return batchRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    private void validateProductExists(String productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + productId));
    }
}
