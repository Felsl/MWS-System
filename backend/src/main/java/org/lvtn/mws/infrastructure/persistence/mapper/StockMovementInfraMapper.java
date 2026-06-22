package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockMovementInfraMapper {
    StockMovement toDomain(StockMovementEntity entity);
    StockMovementEntity toEntity(StockMovement domain);
}
