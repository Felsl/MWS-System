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
}
