package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.mapstruct.Mapper;

/**
 * [GIAI ĐOẠN 6 — ĐÃ SỬA] Mapper viết tay (default) để đảm bảo khớp shape mới của
 * StockMovement (immutable + builder) với entity.
 */
@Mapper(componentModel = "spring")
public interface StockMovementInfraMapper {

    default StockMovementEntity toEntity(StockMovement m) {
        if (m == null) return null;
        StockMovementEntity e = new StockMovementEntity();
        e.setId(m.getId());
        e.setProductId(m.getProductId());
        e.setWarehouseId(m.getWarehouseId());
        e.setBatchId(m.getBatchId());
        e.setMovementType(m.getMovementType());
        e.setQuantityChange(m.getQuantityChange());
        e.setQuantityBefore(m.getQuantityBefore());
        e.setQuantityAfter(m.getQuantityAfter());
        e.setReferenceType(m.getReferenceType());
        e.setReferenceId(m.getReferenceId());
        e.setNote(m.getNote());
        e.setCreatedBy(m.getCreatedBy());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    default StockMovement toDomain(StockMovementEntity e) {
        if (e == null) return null;
        return StockMovement.builder()
                .id(e.getId())
                .productId(e.getProductId())
                .warehouseId(e.getWarehouseId())
                .batchId(e.getBatchId())
                .movementType(e.getMovementType())
                .quantityChange(e.getQuantityChange())
                .quantityBefore(e.getQuantityBefore())
                .quantityAfter(e.getQuantityAfter())
                .referenceType(e.getReferenceType())
                .referenceId(e.getReferenceId())
                .note(e.getNote())
                .createdBy(e.getCreatedBy())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
