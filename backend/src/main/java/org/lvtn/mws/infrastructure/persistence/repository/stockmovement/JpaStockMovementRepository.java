package org.lvtn.mws.infrastructure.persistence.repository.stockmovement;

import org.lvtn.mws.domain.model.StockMovement.ReferenceType;
import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaStockMovementRepository extends JpaRepository<StockMovementEntity, String> {
    List<StockMovementEntity> findByReferenceTypeAndReferenceId(ReferenceType referenceType, String referenceId);
}
