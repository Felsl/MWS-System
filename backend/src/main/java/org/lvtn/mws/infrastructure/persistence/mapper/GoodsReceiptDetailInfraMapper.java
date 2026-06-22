package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.infrastructure.persistence.entity.GoodsReceiptDetailEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsReceiptDetailInfraMapper {
    GoodsReceiptDetail toDomain(GoodsReceiptDetailEntity entity);
    GoodsReceiptDetailEntity toEntity(GoodsReceiptDetail domain);
    List<GoodsReceiptDetail> toDomainList(List<GoodsReceiptDetailEntity> entities);
    List<GoodsReceiptDetailEntity> toEntityList(List<GoodsReceiptDetail> domains);
}
