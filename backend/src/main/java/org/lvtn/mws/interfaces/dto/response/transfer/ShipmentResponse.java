package org.lvtn.mws.interfaces.dto.response.transfer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ShipmentResponse {
    private String id;
    private String shipmentNumber;
    private String salesOrderId;
    private String transferOrderId;
    private String carrierId;
    private String trackingNumber;
    private String status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}
