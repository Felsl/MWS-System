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

    /** [GIAI ĐOẠN 7] Lô ACTIVE còn hàng, SẮP hết hạn: today <= expiry_date <= threshold. */
    @Query("SELECT b FROM InventoryBatchEntity b " +
           "WHERE b.status = 'ACTIVE' AND b.quantity > 0 " +
           "  AND b.expiryDate IS NOT NULL " +
           "  AND b.expiryDate >= :today AND b.expiryDate <= :threshold " +
           "ORDER BY b.expiryDate ASC")
    List<InventoryBatchEntity> findNearExpiryActiveBatches(@Param("today") LocalDate today,
                                                           @Param("threshold") LocalDate threshold);

    List<InventoryBatchEntity> findByProductIdAndWarehouseId(String productId, String warehouseId);

    /**
     * [MỤC 6] Lô ACTIVE còn hàng có hạn dùng ≤ threshold (= today + days), kho tuỳ chọn.
     * Không đặt cận dưới: gồm cả lô đã quá hạn còn tồn để bộ phận kho xử lý.
     */
    @Query("SELECT b FROM InventoryBatchEntity b " +
           "WHERE b.status = 'ACTIVE' AND b.quantity > 0 " +
           "  AND b.expiryDate IS NOT NULL AND b.expiryDate <= :threshold " +
           "  AND (:warehouseId IS NULL OR b.warehouseId = :warehouseId) " +
           "ORDER BY b.expiryDate ASC")
    List<InventoryBatchEntity> findExpiring(@Param("threshold") LocalDate threshold,
                                            @Param("warehouseId") String warehouseId);

    /** [GIAI ĐOẠN 6] Toàn bộ lô của một kho — chụp ảnh tồn khi bắt đầu kiểm kê. */
    List<InventoryBatchEntity> findByWarehouseId(String warehouseId);

    /**
     * [PA1] Tổng mức chiếm theo ô kệ cho một kho: [binLocationId, Σ(qty*weight), Σ(qty*volume)].
     * Theta-join sang ProductEntity (khóa String, không @ManyToOne). Chỉ tính lô còn hàng (quantity > 0),
     * mọi trạng thái (hàng niêm phong vẫn chiếm chỗ). Sản phẩm thiếu weight/volume -> COALESCE 0.
     */
    @Query("SELECT b.binLocationId, " +
           "COALESCE(SUM(b.quantity * p.weight), 0), " +
           "COALESCE(SUM(b.quantity * p.volume), 0) " +
           "FROM InventoryBatchEntity b, ProductEntity p " +
           "WHERE p.id = b.productId AND b.warehouseId = :warehouseId AND b.quantity > 0 " +
           "GROUP BY b.binLocationId")
    List<Object[]> sumOccupancyByBin(@Param("warehouseId") String warehouseId);
}
