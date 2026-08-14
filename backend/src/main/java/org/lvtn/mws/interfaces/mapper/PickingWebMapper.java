package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListDetailResponse;
import org.lvtn.mws.interfaces.dto.response.picking.PickingListResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/** Domain -> Response. Resolve người lấy (userId -> username) và lô cần (batchId -> batchNumber). */
@Component
public class PickingWebMapper {

    private final IUserRepository userRepository;
    private final IInventoryBatchRepository batchRepository;

    public PickingWebMapper(IUserRepository userRepository, IInventoryBatchRepository batchRepository) {
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
    }

    public PickingListResponse toResponse(PickingList pl) {
        if (pl == null) return null;
        String assignedToName = pl.getAssignedTo() == null ? null
                : userRepository.findById(pl.getAssignedTo()).map(User::getUsername).orElse(pl.getAssignedTo());
        return new PickingListResponse(
                pl.getId(),
                pl.getPickNumber(),
                pl.getSoId(),
                pl.getAssignedTo(),
                assignedToName,
                pl.getStatus() == null ? null : pl.getStatus().name(),
                pl.getStartedAt(),
                pl.getCompletedAt(),
                toDetailResponseList(pl.getDetails()));
    }

    public PickingListDetailResponse toDetailResponse(PickingListDetail d) {
        if (d == null) return null;
        String batchNumber = d.getBatchId() == null ? null
                : batchRepository.findById(d.getBatchId()).map(b -> b.getBatchNumber()).orElse(d.getBatchId());
        return new PickingListDetailResponse(
                d.getId(), d.getPickingListId(), d.getProductId(),
                d.getBatchId(), batchNumber, d.getRequiredBatchId(), d.getActualBatchId(), d.getBinLocationId(),
                d.getQuantityToPick(), d.getQuantityPicked(),
                d.isConfirmed(), d.getConfirmedBy(), d.getConfirmedAt());
    }

    public List<PickingListDetailResponse> toDetailResponseList(List<PickingListDetail> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toDetailResponse).toList();
    }

    public List<PickingListResponse> toResponseList(List<PickingList> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
