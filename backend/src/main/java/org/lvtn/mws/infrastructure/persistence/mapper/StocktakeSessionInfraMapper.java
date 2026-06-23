package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.infrastructure.persistence.entity.StocktakeSessionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StocktakeSessionInfraMapper {

    default StocktakeSessionEntity toEntity(StocktakeSession d) {
        if (d == null) return null;
        StocktakeSessionEntity e = new StocktakeSessionEntity();
        e.setId(d.getId());
        e.setWarehouseId(d.getWarehouseId());
        e.setStatus(d.getStatus().name());
        e.setFreezeStartedAt(d.getFreezeStartedAt());
        e.setFreezeEndedAt(d.getFreezeEndedAt());
        e.setCreatedBy(d.getCreatedBy());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }

    default StocktakeSession toDomain(StocktakeSessionEntity e) {
        if (e == null) return null;
        return StocktakeSession.builder()
                .id(e.getId())
                .warehouseId(e.getWarehouseId())
                .status(StocktakeSession.Status.valueOf(e.getStatus()))
                .freezeStartedAt(e.getFreezeStartedAt())
                .freezeEndedAt(e.getFreezeEndedAt())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
