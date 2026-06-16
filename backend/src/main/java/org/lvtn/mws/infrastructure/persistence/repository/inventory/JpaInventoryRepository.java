package org.lvtn.mws.infrastructure.persistence.repository.inventory;

import org.lvtn.mws.infrastructure.persistence.entity.InventoryEntity;
import org.lvtn.mws.infrastructure.persistence.entity.InventoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, InventoryId> {

    Optional<InventoryEntity> findById(InventoryId id);
    List<InventoryEntity> findByIdWarehouseId(String warehouseId);
    List<InventoryEntity> findByIdProductId(String productId);
}
