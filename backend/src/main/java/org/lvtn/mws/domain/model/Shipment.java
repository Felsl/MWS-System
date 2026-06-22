package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Vận đơn (shipments). Trung tâm điều phối hàng rời kho.
 * Ràng buộc chk_shipment_source: bắt buộc trỏ vào ĐÚNG 1 trong 2 nguồn (SO hoặc TO).
 * Thuần Java.
 */
public class Shipment {

    public enum Status { PACKED, HANDED_OVER, SHIPPING, DELIVERED }

    private final String id;
    private String shipmentNumber;
    private final String salesOrderId;     // có thể null nếu là transfer
    private final String transferOrderId;  // có thể null nếu là sales
    private String carrierId;
    private String trackingNumber;
    private Status status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private final LocalDateTime createdAt;

    private Shipment(Builder b) {
        this.id              = Objects.requireNonNull(b.id, "id is required");
        this.shipmentNumber  = b.shipmentNumber;
        this.salesOrderId    = b.salesOrderId;
        this.transferOrderId = b.transferOrderId;
        this.carrierId       = b.carrierId;
        this.trackingNumber  = b.trackingNumber;
        this.status          = b.status == null ? Status.PACKED : b.status;
        this.shippedAt       = b.shippedAt;
        this.deliveredAt     = b.deliveredAt;
        this.createdAt       = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
        validateSource();
    }

    /** chk_shipment_source: đúng 1 trong 2 nguồn. */
    private void validateSource() {
        boolean hasSo = salesOrderId != null;
        boolean hasTo = transferOrderId != null;
        if (hasSo == hasTo) {
            throw new IllegalArgumentException(
                    "Vận đơn phải gắn với ĐÚNG MỘT nguồn: hoặc Sales Order, hoặc Transfer Order");
        }
    }

    public static class Builder {
        private String id;
        private String shipmentNumber;
        private String salesOrderId;
        private String transferOrderId;
        private String carrierId;
        private String trackingNumber;
        private Status status;
        private LocalDateTime shippedAt;
        private LocalDateTime deliveredAt;
        private LocalDateTime createdAt;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder shipmentNumber(String v)  { this.shipmentNumber = v; return this; }
        public Builder salesOrderId(String v)    { this.salesOrderId = v; return this; }
        public Builder transferOrderId(String v) { this.transferOrderId = v; return this; }
        public Builder carrierId(String v)       { this.carrierId = v; return this; }
        public Builder trackingNumber(String v)  { this.trackingNumber = v; return this; }
        public Builder status(Status v)          { this.status = v; return this; }
        public Builder shippedAt(LocalDateTime v){ this.shippedAt = v; return this; }
        public Builder deliveredAt(LocalDateTime v){ this.deliveredAt = v; return this; }
        public Builder createdAt(LocalDateTime v){ this.createdAt = v; return this; }
        public Shipment build()                  { return new Shipment(this); }
    }

    public boolean isSalesShipment()    { return salesOrderId != null; }
    public boolean isTransferShipment() { return transferOrderId != null; }

    public void assignTracking(String carrierId, String trackingNumber) {
        this.carrierId      = carrierId;
        this.trackingNumber = trackingNumber;
    }

    public void assignShipmentNumber(String number) {
        if (this.shipmentNumber == null) this.shipmentNumber = number;
    }

    /**
     * PACKED -> SHIPPING: xe bên vận chuyển đến lấy hàng.
     * Đây là mốc kích hoạt khấu trừ tồn kho vật lý + ghi thẻ kho (qua Spring Event).
     */
    public void ship() {
        if (this.status != Status.PACKED && this.status != Status.HANDED_OVER) {
            throw new IllegalStateException("Chỉ vận đơn PACKED/HANDED_OVER mới được xuất (SHIPPING)");
        }
        this.status    = Status.SHIPPING;
        this.shippedAt = LocalDateTime.now();
    }

    /** SHIPPING -> DELIVERED. */
    public void deliver() {
        if (this.status != Status.SHIPPING) {
            throw new IllegalStateException("Chỉ vận đơn SHIPPING mới được đánh dấu DELIVERED");
        }
        this.status      = Status.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public String getId()              { return id; }
    public String getShipmentNumber()  { return shipmentNumber; }
    public String getSalesOrderId()    { return salesOrderId; }
    public String getTransferOrderId() { return transferOrderId; }
    public String getCarrierId()       { return carrierId; }
    public String getTrackingNumber()  { return trackingNumber; }
    public Status getStatus()          { return status; }
    public LocalDateTime getShippedAt(){ return shippedAt; }
    public LocalDateTime getDeliveredAt(){ return deliveredAt; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
}
