package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.infrastructure.persistence.entity.BinLocationDbEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BinLocationInfraMapper {

    default BinLocationDbEntity toEntity(BinLocation domain) {
        if (domain == null) return null;
        BinLocationDbEntity e = new BinLocationDbEntity();
        e.setId(domain.getId());
        e.setWarehouseId(domain.getWarehouseId());
        e.setZone(domain.getZone());
        e.setAisle(domain.getAisle());
        e.setRack(domain.getRack());
        e.setBin(domain.getBin());
        return e;
    }

    default BinLocation toDomain(BinLocationDbEntity e) {
        if (e == null) return null;
        return new BinLocation.BinLocationBuilder()
                .id(e.getId())
                .warehouseId(e.getWarehouseId())
                .zone(e.getZone())
                .aisle(e.getAisle())
                .rack(e.getRack())
                .bin(e.getBin())
                .build();
    }
}
