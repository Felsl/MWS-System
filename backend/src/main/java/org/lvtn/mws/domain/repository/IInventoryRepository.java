package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Inventory;
import java.util.List;
import java.util.Optional;

public interface IInventoryRepository {
    Inventory save(Inventory inventory);
    Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId);
    List<Inventory> findByWarehouseId(String warehouseId);
    List<Inventory> findByProductId(String productId);
}
