package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.TransferOrderDetail;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderDetailEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferOrderDetailInfraMapper {

    default TransferOrderDetailEntity toEntity(TransferOrderDetail d) {
        if (d == null) return null;
        TransferOrderDetailEntity e = new TransferOrderDetailEntity();
        e.setId(d.getId());
        e.setTransferOrderId(d.getTransferOrderId());
        e.setProductId(d.getProductId());
        e.setBatchId(d.getBatchId());
        e.setQuantity(d.getQuantity());
        e.setQuantityReceived(d.getQuantityReceived());
        e.setFromBinLocationId(d.getFromBinLocationId());
        e.setBinLocationId(d.getBinLocationId());
        return e;
    }

    default TransferOrderDetail toDomain(TransferOrderDetailEntity e) {
        if (e == null) return null;
        return new TransferOrderDetail.Builder()
                .id(e.getId())
                .transferOrderId(e.getTransferOrderId())
                .productId(e.getProductId())
                .batchId(e.getBatchId())
                .quantity(e.getQuantity() != null ? e.getQuantity() : 0)
                .quantityReceived(e.getQuantityReceived() != null ? e.getQuantityReceived() : 0)
                .fromBinLocationId(e.getFromBinLocationId())
                .binLocationId(e.getBinLocationId())
                .build();
    }
}
