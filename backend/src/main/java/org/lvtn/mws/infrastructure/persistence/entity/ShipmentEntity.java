package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ShipmentEntity {

    @Id
    @Column(length = 20)
    private String id;

    @Column(name = "shipment_number", nullable = false, unique = true, length = 50)
    private String shipmentNumber;

    @Column(name = "sales_order_id", length = 20)
    private String salesOrderId;

    @Column(name = "transfer_order_id", length = 20)
    private String transferOrderId;

    @Column(name = "carrier_id", length = 20)
    private String carrierId;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
