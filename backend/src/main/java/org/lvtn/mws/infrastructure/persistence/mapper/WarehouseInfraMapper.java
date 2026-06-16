package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.infrastructure.persistence.entity.WarehouseDbEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WarehouseInfraMapper {

    default WarehouseDbEntity toEntity(Warehouse domain) {
        if (domain == null) return null;
        WarehouseDbEntity e = new WarehouseDbEntity();
        e.setId(domain.getId());
        e.setCode(domain.getCode());
        e.setName(domain.getName());
        e.setAddress(domain.getAddress());
        e.setStatus(domain.getStatus());
        e.setCreatedAt(domain.getCreatedAt());
        e.setDeletedAt(domain.getDeletedAt());
        return e;
    }

    default Warehouse toDomain(WarehouseDbEntity e) {
        if (e == null) return null;
        return new Warehouse.WarehouseBuilder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .address(e.getAddress())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
