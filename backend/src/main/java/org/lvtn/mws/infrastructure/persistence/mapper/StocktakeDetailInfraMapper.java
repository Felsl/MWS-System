package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.infrastructure.persistence.entity.StocktakeDetailEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StocktakeDetailInfraMapper {

    default StocktakeDetailEntity toEntity(StocktakeDetail d) {
        if (d == null) return null;
        StocktakeDetailEntity e = new StocktakeDetailEntity();
        e.setId(d.getId());
        e.setSessionId(d.getSessionId());
        e.setProductId(d.getProductId());
        e.setBinLocationId(d.getBinLocationId());
        e.setBatchId(d.getBatchId());
        e.setSystemQuantity(d.getSystemQuantity());
        e.setCountedQuantity(d.getCountedQuantity());
        e.setDifference(d.getDifference());
        e.setAdjustmentReason(d.getAdjustmentReason());
        e.setCountedBy(d.getCountedBy());
        e.setCountedAt(d.getCountedAt());
        e.setApprovedBy(d.getApprovedBy());
        e.setApprovedAt(d.getApprovedAt());
        return e;
    }

    default StocktakeDetail toDomain(StocktakeDetailEntity e) {
        if (e == null) return null;
        return StocktakeDetail.builder()
                .id(e.getId())
                .sessionId(e.getSessionId())
                .productId(e.getProductId())
                .binLocationId(e.getBinLocationId())
                .batchId(e.getBatchId())
                .systemQuantity(e.getSystemQuantity())
                .countedQuantity(e.getCountedQuantity())
                .difference(e.getDifference())
                .adjustmentReason(e.getAdjustmentReason())
                .countedBy(e.getCountedBy())
                .countedAt(e.getCountedAt())
                .approvedBy(e.getApprovedBy())
                .approvedAt(e.getApprovedAt())
                .build();
    }
}
