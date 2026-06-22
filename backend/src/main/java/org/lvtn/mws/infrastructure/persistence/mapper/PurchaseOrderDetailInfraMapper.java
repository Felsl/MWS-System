package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.PurchaseOrderDetail;
import org.lvtn.mws.infrastructure.persistence.entity.PurchaseOrderDetailEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderDetailInfraMapper {
    PurchaseOrderDetail toDomain(PurchaseOrderDetailEntity entity);
    PurchaseOrderDetailEntity toEntity(PurchaseOrderDetail domain);
    List<PurchaseOrderDetail> toDomainList(List<PurchaseOrderDetailEntity> entities);
    List<PurchaseOrderDetailEntity> toEntityList(List<PurchaseOrderDetail> domains);
}
