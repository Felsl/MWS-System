package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.infrastructure.persistence.entity.InventoryEntity;
import org.lvtn.mws.infrastructure.persistence.entity.InventoryId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryInfraMapper {

    default InventoryEntity toEntity(Inventory domain) {
        if (domain == null) return null;
        InventoryId pk = new InventoryId(domain.getProductId(), domain.getWarehouseId());
        InventoryEntity e = new InventoryEntity();
        e.setId(pk);
        e.setQuantity(domain.getQuantity());
        e.setReservedQuantity(domain.getReservedQuantity());
        e.setVersion(domain.getVersion());
        return e;
    }

    default Inventory toDomain(InventoryEntity e) {
        if (e == null) return null;
        return new Inventory.Builder()
                .productId(e.getId().getProductId())
                .warehouseId(e.getId().getWarehouseId())
                .quantity(e.getQuantity())
                .reservedQuantity(e.getReservedQuantity())
                .version(e.getVersion())
                .build();
    }
}
