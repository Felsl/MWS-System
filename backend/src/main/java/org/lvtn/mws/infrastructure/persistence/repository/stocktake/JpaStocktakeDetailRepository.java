package org.lvtn.mws.infrastructure.persistence.repository.stocktake;

import org.lvtn.mws.infrastructure.persistence.entity.StocktakeDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaStocktakeDetailRepository extends JpaRepository<StocktakeDetailEntity, String> {
    List<StocktakeDetailEntity> findBySessionId(String sessionId);
}
