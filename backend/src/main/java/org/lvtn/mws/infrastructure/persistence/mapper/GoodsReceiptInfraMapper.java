package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsReceiptInfraMapper {
    GoodsReceipt toDomain(GoodsReceiptEntity entity);
    GoodsReceiptEntity toEntity(GoodsReceipt domain);
    List<GoodsReceipt> toDomainList(List<GoodsReceiptEntity> entities);
}
