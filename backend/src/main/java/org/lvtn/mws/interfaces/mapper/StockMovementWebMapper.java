package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.interfaces.dto.response.StockMovementResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMovementWebMapper {

    default StockMovementResponse toResponse(StockMovement m) {
        if (m == null) return null;
        return new StockMovementResponse(
                m.getId(), m.getProductId(), m.getWarehouseId(), m.getBatchId(), m.getBinLocationId(),
                m.getMovementType() == null ? null : m.getMovementType().name(),
                m.getQuantityChange(), m.getQuantityBefore(), m.getQuantityAfter(),
                m.getReferenceType(), m.getReferenceId(), m.getNote(),
                m.getCreatedBy(), m.getCreatedAt());
    }

    default List<StockMovementResponse> toResponseList(List<StockMovement> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
