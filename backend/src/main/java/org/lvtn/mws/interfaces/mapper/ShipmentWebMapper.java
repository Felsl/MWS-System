package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.interfaces.dto.response.shipment.ShipmentResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShipmentWebMapper {

    default ShipmentResponse toResponse(Shipment s) {
        if (s == null) return null;
        return new ShipmentResponse(
                s.getId(),
                s.getShipmentNumber(),
                s.getSalesOrderId(),
                s.getTransferOrderId(),
                s.getCarrierId(),
                s.getTrackingNumber(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getShippedAt(),
                s.getDeliveredAt(),
                s.getCreatedAt());
    }

    default List<ShipmentResponse> toResponseList(List<Shipment> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
