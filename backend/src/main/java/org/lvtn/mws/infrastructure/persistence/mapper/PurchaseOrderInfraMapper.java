package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.PurchaseOrder;
import org.lvtn.mws.infrastructure.persistence.entity.PurchaseOrderEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderInfraMapper {
    PurchaseOrder toDomain(PurchaseOrderEntity entity);
    PurchaseOrderEntity toEntity(PurchaseOrder domain);
    List<PurchaseOrder> toDomainList(List<PurchaseOrderEntity> entities);
}
