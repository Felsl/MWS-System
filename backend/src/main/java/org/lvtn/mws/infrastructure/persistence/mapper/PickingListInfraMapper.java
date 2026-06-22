package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.infrastructure.persistence.entity.PickingListDetailEntity;
import org.lvtn.mws.infrastructure.persistence.entity.PickingListEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PickingListInfraMapper {

    default PickingListEntity toEntity(PickingList domain) {
        if (domain == null) return null;
        PickingListEntity e = new PickingListEntity();
        e.setId(domain.getId());
        e.setSoId(domain.getSoId());
        e.setAssignedTo(domain.getAssignedTo());
        e.setStatus(domain.getStatus().name());
        e.setStartedAt(domain.getStartedAt());
        e.setCompletedAt(domain.getCompletedAt());

        List<PickingListDetailEntity> details = new ArrayList<>();
        for (PickingListDetail d : domain.getDetails()) {
            PickingListDetailEntity de = toDetailEntity(d);
            de.setPickingList(e);
            details.add(de);
        }
        e.setDetails(details);
        return e;
    }

    default PickingListDetailEntity toDetailEntity(PickingListDetail d) {
        PickingListDetailEntity de = new PickingListDetailEntity();
        de.setId(d.getId());
        de.setProductId(d.getProductId());
        de.setBatchId(d.getBatchId());
        de.setActualBatchId(d.getActualBatchId());
        de.setBinLocationId(d.getBinLocationId());
        de.setQuantityToPick(d.getQuantityToPick());
        de.setQuantityPicked(d.getQuantityPicked());
        de.setIsConfirmed(d.isConfirmed());
        de.setConfirmedBy(d.getConfirmedBy());
        de.setConfirmedAt(d.getConfirmedAt());
        return de;
    }

    default PickingList toDomain(PickingListEntity e) {
        if (e == null) return null;
        List<PickingListDetail> details = new ArrayList<>();
        if (e.getDetails() != null) {
            for (PickingListDetailEntity de : e.getDetails()) {
                details.add(new PickingListDetail.Builder()
                        .id(de.getId())
                        .pickingListId(e.getId())
                        .productId(de.getProductId())
                        .batchId(de.getBatchId())
                        .actualBatchId(de.getActualBatchId())
                        .binLocationId(de.getBinLocationId())
                        .quantityToPick(de.getQuantityToPick())
                        .quantityPicked(de.getQuantityPicked() == null ? 0 : de.getQuantityPicked())
                        .confirmed(Boolean.TRUE.equals(de.getIsConfirmed()))
                        .confirmedBy(de.getConfirmedBy())
                        .confirmedAt(de.getConfirmedAt())
                        .build());
            }
        }
        return new PickingList.Builder()
                .id(e.getId())
                .soId(e.getSoId())
                .assignedTo(e.getAssignedTo())
                .status(PickingList.Status.valueOf(e.getStatus()))
                .startedAt(e.getStartedAt())
                .completedAt(e.getCompletedAt())
                .details(details)
                .build();
    }
}
