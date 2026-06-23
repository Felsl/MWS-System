package org.lvtn.mws.infrastructure.persistence.repository.stockmovement;

import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * [GIAI ĐOẠN 6 — ĐÃ SỬA] reference_type giờ là String; bổ sung truy vấn theo product.
 */
public interface JpaStockMovementRepository extends JpaRepository<StockMovementEntity, String> {
    List<StockMovementEntity> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
    List<StockMovementEntity> findByProductIdOrderByCreatedAtDesc(String productId);
}
