package org.lvtn.mws.interfaces.dto.response.shipment;

import java.time.LocalDateTime;

public record ShipmentResponse(
        String id,
        String shipmentNumber,
        String salesOrderId,
        String transferOrderId,
        String carrierId,
        String trackingNumber,
        String status,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt) {
}
