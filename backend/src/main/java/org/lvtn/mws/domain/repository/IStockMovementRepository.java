package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StockMovement;
import java.util.List;

public interface IStockMovementRepository {
    StockMovement save(StockMovement movement);
    List<StockMovement> findByReference(String referenceType, String referenceId);
}
