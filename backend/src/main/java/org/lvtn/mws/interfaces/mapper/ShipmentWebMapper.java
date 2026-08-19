package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;
import org.lvtn.mws.interfaces.dto.response.shipment.ShipmentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Chuyển domain -> DTO cho vận đơn. @Component để inject repo và resolve id -> mã:
 * salesOrderId -> soNumber, transferOrderId -> transferNumber (FE khỏi tra lookup client).
 */
@Component
public class ShipmentWebMapper {

    private final ISalesOrderRepository salesOrderRepository;
    private final ITransferOrderRepository transferOrderRepository;

    public ShipmentWebMapper(ISalesOrderRepository salesOrderRepository,
                             ITransferOrderRepository transferOrderRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.transferOrderRepository = transferOrderRepository;
    }

    private String soNumber(String id) {
        return id == null ? null : salesOrderRepository.findById(id).map(o -> o.getSoNumber()).orElse(id);
    }
    private String transferNumber(String id) {
        return id == null ? null : transferOrderRepository.findById(id).map(o -> o.getTransferNumber()).orElse(id);
    }

    public ShipmentResponse toResponse(Shipment s) {
        if (s == null) return null;
        return new ShipmentResponse(
                s.getId(),
                s.getShipmentNumber(),
                s.getSalesOrderId(),
                soNumber(s.getSalesOrderId()),
                s.getTransferOrderId(),
                transferNumber(s.getTransferOrderId()),
                s.getCarrierId(),
                s.getTrackingNumber(),
                s.getStatus() == null ? null : s.getStatus().name(),
                s.getShippedAt(),
                s.getDeliveredAt(),
                s.getCreatedAt());
    }

    public List<ShipmentResponse> toResponseList(List<Shipment> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toResponse).toList();
    }
}
