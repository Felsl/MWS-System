package org.lvtn.mws.infrastructure.persistence.repository.stocktake;

import org.lvtn.mws.infrastructure.persistence.entity.StocktakeSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaStocktakeSessionRepository extends JpaRepository<StocktakeSessionEntity, String> {
    List<StocktakeSessionEntity> findByWarehouseId(String warehouseId);
    Optional<StocktakeSessionEntity> findFirstByWarehouseIdAndStatus(String warehouseId, String status);
    boolean existsByWarehouseIdAndStatus(String warehouseId, String status);
}
