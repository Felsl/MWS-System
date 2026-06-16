package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.infrastructure.persistence.entity.InventoryBatchEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryBatchInfraMapper {

    default InventoryBatchEntity toEntity(InventoryBatch domain) {
        if (domain == null) return null;
        InventoryBatchEntity e = new InventoryBatchEntity();
        e.setId(domain.getId());
        e.setProductId(domain.getProductId());
        e.setWarehouseId(domain.getWarehouseId());
        e.setBinLocationId(domain.getBinLocationId());
        e.setBatchNumber(domain.getBatchNumber());
        e.setQuantity(domain.getQuantity());
        e.setExpiryDate(domain.getExpiryDate());
        e.setManufacturedDate(domain.getManufacturedDate());
        e.setStatus(domain.getStatus());
        e.setCreatedAt(domain.getCreatedAt());
        return e;
    }

    default InventoryBatch toDomain(InventoryBatchEntity e) {
        if (e == null) return null;
        return new InventoryBatch.Builder()
                .id(e.getId())
                .productId(e.getProductId())
                .warehouseId(e.getWarehouseId())
                .binLocationId(e.getBinLocationId())
                .batchNumber(e.getBatchNumber())
                .quantity(e.getQuantity())
                .expiryDate(e.getExpiryDate())
                .manufacturedDate(e.getManufacturedDate())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
