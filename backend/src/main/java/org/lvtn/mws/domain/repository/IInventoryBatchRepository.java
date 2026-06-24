package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.InventoryBatch;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IInventoryBatchRepository {
    InventoryBatch save(InventoryBatch batch);
    Optional<InventoryBatch> findById(String id);
    Optional<InventoryBatch> findByBatchNumber(String batchNumber);

    /** FEFO query: ACTIVE batches ordered by expiry_date ASC, then created_at ASC */
    List<InventoryBatch> findActiveBatchesForPicking(String productId, String warehouseId);

    /** For nightly cron: batches still ACTIVE but expiry_date < today */
    List<InventoryBatch> findExpiredActiveBatches(LocalDate today);

    /** [GIAI ĐOẠN 7] Lô ACTIVE còn hàng & SẮP hết hạn trong khoảng [today, threshold]. */
    List<InventoryBatch> findNearExpiryActiveBatches(LocalDate today, LocalDate threshold);

    List<InventoryBatch> findByProductIdAndWarehouseId(String productId, String warehouseId);

    /** [GIAI ĐOẠN 6] Toàn bộ lô của một kho — phục vụ chụp ảnh tồn (snapshot) khi bắt đầu kiểm kê. */
    List<InventoryBatch> findByWarehouseId(String warehouseId);

    void saveAll(List<InventoryBatch> batches);
}
