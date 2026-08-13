package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.BinOccupancy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IInventoryBatchRepository {
    InventoryBatch save(InventoryBatch batch);
    Optional<InventoryBatch> findById(String id);
    Optional<InventoryBatch> findByBatchNumber(String batchNumber);

    /** FEFO query: ACTIVE batches ordered by expiry_date ASC, then created_at ASC */
    List<InventoryBatch> findActiveBatchesForPicking(String productId, String warehouseId);

    /** [Bán theo NCC] FEFO nhưng chỉ lô của NCC chỉ định; supplierId null = mọi NCC. */
    List<InventoryBatch> findActiveBatchesForPickingBySupplier(String productId, String warehouseId, String supplierId);

    /** [Bán theo NCC] Tồn bán được (ACTIVE + chưa hết hạn) gom theo sản phẩm cho 1 kho: productId -> available. */
    java.util.Map<String, Integer> sumSellableByWarehouse(String warehouseId);

    /** For nightly cron: batches still ACTIVE but expiry_date < today */
    List<InventoryBatch> findExpiredActiveBatches(LocalDate today);

    /** [GIAI ĐOẠN 7] Lô ACTIVE còn hàng & SẮP hết hạn trong khoảng [today, threshold]. */
    List<InventoryBatch> findNearExpiryActiveBatches(LocalDate today, LocalDate threshold);

    /** [MỤC 6] Lô ACTIVE còn hàng có hạn dùng ≤ threshold; warehouseId null = mọi kho. */
    List<InventoryBatch> findExpiring(LocalDate threshold, String warehouseId);

    List<InventoryBatch> findByProductIdAndWarehouseId(String productId, String warehouseId);

    /** [GIAI ĐOẠN 6] Toàn bộ lô của một kho — phục vụ chụp ảnh tồn (snapshot) khi bắt đầu kiểm kê. */
    List<InventoryBatch> findByWarehouseId(String warehouseId);

    void saveAll(List<InventoryBatch> batches);

    /** [PA1] Mức chiếm (kg, thể tích) của từng ô kệ trong kho, tính sống từ tồn × products.weight/volume. */
    List<BinOccupancy> sumOccupancyByWarehouse(String warehouseId);
}
