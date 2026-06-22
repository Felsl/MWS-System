package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.infrastructure.persistence.entity.ShipmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentInfraMapper {

    default ShipmentEntity toEntity(Shipment domain) {
        if (domain == null) return null;
        ShipmentEntity e = new ShipmentEntity();
        e.setId(domain.getId());
        e.setShipmentNumber(domain.getShipmentNumber());
        e.setSalesOrderId(domain.getSalesOrderId());
        e.setTransferOrderId(domain.getTransferOrderId());
        e.setCarrierId(domain.getCarrierId());
        e.setTrackingNumber(domain.getTrackingNumber());
        e.setStatus(domain.getStatus().name());
        e.setShippedAt(domain.getShippedAt());
        e.setDeliveredAt(domain.getDeliveredAt());
        e.setCreatedAt(domain.getCreatedAt());
        return e;
    }

    default Shipment toDomain(ShipmentEntity e) {
        if (e == null) return null;
        return new Shipment.Builder()
                .id(e.getId())
                .shipmentNumber(e.getShipmentNumber())
                .salesOrderId(e.getSalesOrderId())
                .transferOrderId(e.getTransferOrderId())
                .carrierId(e.getCarrierId())
                .trackingNumber(e.getTrackingNumber())
                .status(Shipment.Status.valueOf(e.getStatus()))
                .shippedAt(e.getShippedAt())
                .deliveredAt(e.getDeliveredAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
