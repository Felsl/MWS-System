package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeDetailResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeResponse;
import org.lvtn.mws.interfaces.dto.response.stocktake.StocktakeSessionResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StocktakeWebMapper {

    default StocktakeSessionResponse toSessionResponse(StocktakeSession s) {
        if (s == null) return null;
        return new StocktakeSessionResponse(
                s.getId(), s.getWarehouseId(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getFreezeStartedAt(), s.getFreezeEndedAt(),
                s.getCreatedBy(), s.getCreatedAt());
    }

    default StocktakeDetailResponse toDetailResponse(StocktakeDetail d) {
        if (d == null) return null;
        return new StocktakeDetailResponse(
                d.getId(), d.getSessionId(), d.getProductId(), d.getBinLocationId(), d.getBatchId(),
                d.getSystemQuantity(), d.getCountedQuantity(), d.getDifference(),
                d.getAdjustmentReason(), d.getCountedBy(), d.getCountedAt(),
                d.getApprovedBy(), d.getApprovedAt());
    }

    default List<StocktakeDetailResponse> toDetailResponseList(List<StocktakeDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    default StocktakeResponse toResponse(StocktakeSession session, List<StocktakeDetail> details) {
        return new StocktakeResponse(toSessionResponse(session), toDetailResponseList(details));
    }
}
