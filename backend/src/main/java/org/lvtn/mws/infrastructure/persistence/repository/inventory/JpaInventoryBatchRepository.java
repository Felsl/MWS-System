package org.lvtn.mws.infrastructure.persistence.repository.inventory;

import org.lvtn.mws.infrastructure.persistence.entity.InventoryBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaInventoryBatchRepository extends JpaRepository<InventoryBatchEntity, String> {

    Optional<InventoryBatchEntity> findByBatchNumber(String batchNumber);

    /**
     * FEFO: ACTIVE batches with qty > 0, ordered by expiry_date ASC (nulls last), then created_at ASC.
     */
    @Query("SELECT b FROM InventoryBatchEntity b " +
           "WHERE b.productId = :productId AND b.warehouseId = :warehouseId " +
           "  AND b.status = 'ACTIVE' AND b.quantity > 0 " +
           "ORDER BY CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END, b.expiryDate ASC, b.createdAt ASC")
    List<InventoryBatchEntity> findActiveBatchesForPicking(@Param("productId") String productId,
                                                           @Param("warehouseId") String warehouseId);

    @Query("SELECT b FROM InventoryBatchEntity b " +
           "WHERE b.status = 'ACTIVE' AND b.expiryDate < :today")
    List<InventoryBatchEntity> findExpiredActiveBatches(@Param("today") LocalDate today);

    List<InventoryBatchEntity> findByProductIdAndWarehouseId(String productId, String warehouseId);

    /** [GIAI ĐOẠN 6] Toàn bộ lô của một kho — chụp ảnh tồn khi bắt đầu kiểm kê. */
    List<InventoryBatchEntity> findByWarehouseId(String warehouseId);
}
