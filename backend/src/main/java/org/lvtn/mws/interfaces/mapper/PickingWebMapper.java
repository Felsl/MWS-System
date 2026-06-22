package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListDetailResponse;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PickingWebMapper {

    default PickingListResponse toResponse(PickingList pl) {
        if (pl == null) return null;
        return new PickingListResponse(
                pl.getId(),
                pl.getSoId(),
                pl.getAssignedTo(),
                pl.getStatus() == null ? null : pl.getStatus().name(),
                pl.getStartedAt(),
                pl.getCompletedAt(),
                toDetailResponseList(pl.getDetails()));
    }

    default PickingListDetailResponse toDetailResponse(PickingListDetail d) {
        if (d == null) return null;
        return new PickingListDetailResponse(
                d.getId(), d.getPickingListId(), d.getProductId(),
                d.getBatchId(), d.getActualBatchId(), d.getBinLocationId(),
                d.getQuantityToPick(), d.getQuantityPicked(),
                d.isConfirmed(), d.getConfirmedBy(), d.getConfirmedAt());
    }

    default List<PickingListDetailResponse> toDetailResponseList(List<PickingListDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    default List<PickingListResponse> toResponseList(List<PickingList> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
