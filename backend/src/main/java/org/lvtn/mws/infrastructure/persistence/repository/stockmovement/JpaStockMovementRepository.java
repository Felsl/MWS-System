package org.lvtn.mws.infrastructure.persistence.repository.stockmovement;

import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * [GIAI ĐOẠN 6 — ĐÃ SỬA] reference_type giờ là String; bổ sung truy vấn theo product.
 * [A2] Bổ sung JpaSpecificationExecutor để hỗ trợ lọc Data Scope động qua Specification.
 */
public interface JpaStockMovementRepository
        extends JpaRepository<StockMovementEntity, String>,
                JpaSpecificationExecutor<StockMovementEntity> {
    List<StockMovementEntity> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(String productId);
    List<StockMovementEntity> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(String productId, String warehouseId);

    /** [MỤC 6] Tồn đầu kỳ: tổng biến động (có dấu) trước mốc 'instant'. */
    @org.springframework.data.jpa.repository.Query(value =
            "SELECT COALESCE(SUM(quantity_change), 0) FROM stock_movements " +
            "WHERE created_at < :instant " +
            "  AND (:warehouseId IS NULL OR warehouse_id = :warehouseId)", nativeQuery = true)
    long sumQuantityChangeBefore(@org.springframework.data.repository.query.Param("instant") java.time.LocalDateTime instant,
                                 @org.springframework.data.repository.query.Param("warehouseId") String warehouseId);

    /** [MỤC 6] GROUP BY DATE(created_at): (ngày, tổng nhập, tổng xuất-dương). */
    @org.springframework.data.jpa.repository.Query(value =
            "SELECT DATE(created_at) AS d, " +
            "       SUM(CASE WHEN quantity_change > 0 THEN quantity_change ELSE 0 END) AS in_qty, " +
            "       SUM(CASE WHEN quantity_change < 0 THEN -quantity_change ELSE 0 END) AS out_qty " +
            "FROM stock_movements " +
            "WHERE created_at >= :fromInclusive AND created_at < :toExclusive " +
            "  AND (:warehouseId IS NULL OR warehouse_id = :warehouseId) " +
            "GROUP BY DATE(created_at) ORDER BY d", nativeQuery = true)
    List<Object[]> aggregateDailyFlow(@org.springframework.data.repository.query.Param("fromInclusive") java.time.LocalDateTime fromInclusive,
                                      @org.springframework.data.repository.query.Param("toExclusive") java.time.LocalDateTime toExclusive,
                                      @org.springframework.data.repository.query.Param("warehouseId") String warehouseId);
}
