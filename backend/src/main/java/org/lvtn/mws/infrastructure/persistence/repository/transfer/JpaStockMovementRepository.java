package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStockMovementRepository extends JpaRepository<StockMovementEntity, String> {
}
