package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * [Bán vượt tồn — backorder] Một NHU CẦU NHẬP phát sinh khi phân bổ đơn bán mà tồn của NCC
 * không đủ. quantityShort giảm dần khi hàng về (fulfillDemands); về 0 thì FULFILLED.
 * supplierId = NCC mong muốn của dòng bán (null = bất kỳ).
 */
public class StockDemand {

    public enum Status { OPEN, FULFILLED, CANCELLED }

    private final String id;
    private final String soId;
    private final String soDetailId;
    private final String productId;
    private final String warehouseId;
    private final String supplierId;
    private int quantityShort;
    private Status status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private StockDemand(Builder b) {
        this.id           = Objects.requireNonNull(b.id, "id is required");
        this.soId         = Objects.requireNonNull(b.soId, "soId is required");
        this.soDetailId   = Objects.requireNonNull(b.soDetailId, "soDetailId is required");
        this.productId    = Objects.requireNonNull(b.productId, "productId is required");
        this.warehouseId  = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.supplierId   = b.supplierId;
        this.quantityShort = b.quantityShort;
        this.status       = b.status == null ? Status.OPEN : b.status;
        this.createdAt    = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
        this.updatedAt    = b.updatedAt;
    }

    /** Bù qty vào nhu cầu (khi hàng về). Về 0 -> FULFILLED. */
    public void reduce(int qty) {
        if (qty <= 0) return;
        this.quantityShort = Math.max(0, this.quantityShort - qty);
        if (this.quantityShort == 0) this.status = Status.FULFILLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public String getId()            { return id; }
    public String getSoId()          { return soId; }
    public String getSoDetailId()    { return soDetailId; }
    public String getProductId()     { return productId; }
    public String getWarehouseId()   { return warehouseId; }
    public String getSupplierId()    { return supplierId; }
    public int getQuantityShort()    { return quantityShort; }
    public Status getStatus()        { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, soId, soDetailId, productId, warehouseId, supplierId;
        private int quantityShort;
        private Status status;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(String v)            { this.id = v; return this; }
        public Builder soId(String v)          { this.soId = v; return this; }
        public Builder soDetailId(String v)    { this.soDetailId = v; return this; }
        public Builder productId(String v)     { this.productId = v; return this; }
        public Builder warehouseId(String v)   { this.warehouseId = v; return this; }
        public Builder supplierId(String v)    { this.supplierId = v; return this; }
        public Builder quantityShort(int v)    { this.quantityShort = v; return this; }
        public Builder status(Status v)        { this.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public StockDemand build()             { return new StockDemand(this); }
    }
}
