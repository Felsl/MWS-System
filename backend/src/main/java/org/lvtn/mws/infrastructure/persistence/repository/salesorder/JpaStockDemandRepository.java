package org.lvtn.mws.infrastructure.persistence.repository.salesorder;

import org.lvtn.mws.infrastructure.persistence.entity.StockDemandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaStockDemandRepository extends JpaRepository<StockDemandEntity, String> {

    List<StockDemandEntity> findByProductIdAndWarehouseIdAndStatusOrderByCreatedAtAsc(
            String productId, String warehouseId, String status);

    List<StockDemandEntity> findBySoIdAndStatus(String soId, String status);

    List<StockDemandEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<StockDemandEntity> findBySoId(String soId);
}
