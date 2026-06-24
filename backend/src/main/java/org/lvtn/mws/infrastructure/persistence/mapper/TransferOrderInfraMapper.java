package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferOrderInfraMapper {

    /** Chỉ map phần header (details xử lý riêng ở RepositoryImpl). */
    default TransferOrderEntity toEntity(TransferOrder domain) {
        if (domain == null) return null;
        TransferOrderEntity e = new TransferOrderEntity();
        e.setId(domain.getId());
        e.setFromWarehouseId(domain.getFromWarehouseId());
        e.setToWarehouseId(domain.getToWarehouseId());
        e.setTransferNumber(domain.getTransferNumber());
        e.setStatus(domain.getStatus());
        e.setCreatedBy(domain.getCreatedBy());
        e.setApprovedBy(domain.getApprovedBy());
        e.setApprovedAt(domain.getApprovedAt());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        return e;
    }

    /** Lắp ráp domain từ header entity + danh sách detail domain. */
    default TransferOrder toDomain(TransferOrderEntity e, List<TransferOrderDetail> details) {
        if (e == null) return null;
        return new TransferOrder.Builder()
                .id(e.getId())
                .fromWarehouseId(e.getFromWarehouseId())
                .toWarehouseId(e.getToWarehouseId())
                .transferNumber(e.getTransferNumber())
                .status(e.getStatus())
                .createdBy(e.getCreatedBy())
                .approvedBy(e.getApprovedBy())
                .approvedAt(e.getApprovedAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .details(details)
                .build();
    }
}
