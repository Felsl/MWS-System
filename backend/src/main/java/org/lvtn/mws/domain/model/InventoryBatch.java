package org.lvtn.mws.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class InventoryBatch {

    public enum Status { ACTIVE, HOLD, EXPIRED }

    private final String id;
    private final String productId;
    private final String warehouseId;
    private final String binLocationId;
    private final String batchNumber;
    private int quantity;
    private LocalDate expiryDate;
    private LocalDate manufacturedDate;
    private Status status;
    private final LocalDateTime createdAt;
    private int version;

    private InventoryBatch(Builder b) {
        this.id              = Objects.requireNonNull(b.id,            "Batch id is required");
        this.productId       = Objects.requireNonNull(b.productId,     "productId is required");
        this.warehouseId     = Objects.requireNonNull(b.warehouseId,   "warehouseId is required");
        this.binLocationId   = Objects.requireNonNull(b.binLocationId, "binLocationId is required");
        this.batchNumber     = Objects.requireNonNull(b.batchNumber,   "batchNumber is required");
        this.quantity        = b.quantity;
        this.expiryDate      = b.expiryDate;
        this.manufacturedDate= b.manufacturedDate;
        this.status          = b.status != null ? b.status : Status.ACTIVE;
        this.createdAt       = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        this.version         = b.version;
    }

    public static class Builder {
        private String id, productId, warehouseId, binLocationId, batchNumber;
        private int quantity = 0;
        private LocalDate expiryDate, manufacturedDate;
        private Status status;
        private LocalDateTime createdAt;
        private int version = 0;

        public Builder id(String v)               { this.id = v; return this; }
        public Builder productId(String v)        { this.productId = v; return this; }
        public Builder warehouseId(String v)      { this.warehouseId = v; return this; }
        public Builder binLocationId(String v)    { this.binLocationId = v; return this; }
        public Builder batchNumber(String v)      { this.batchNumber = v; return this; }
        public Builder quantity(int v)            { this.quantity = v; return this; }
        public Builder expiryDate(LocalDate v)    { this.expiryDate = v; return this; }
        public Builder manufacturedDate(LocalDate v){ this.manufacturedDate = v; return this; }
        public Builder status(Status v)           { this.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder version(int v)             { this.version = v; return this; }
        public InventoryBatch build()             { return new InventoryBatch(this); }
    }

    public void deduct(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        if (this.status != Status.ACTIVE) {
            throw new IllegalStateException("Lô hàng " + batchNumber + " không ở trạng thái ACTIVE, không thể xuất");
        }
        if (this.quantity < qty) {
            throw new InsufficientStockException(
                    "Lô " + batchNumber + " không đủ hàng: cần " + qty + ", còn " + this.quantity);
        }
        this.quantity -= qty;
    }

    public void addQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        this.quantity += qty;
    }

    public void markExpired() { this.status = Status.EXPIRED; }
    public void hold()        { this.status = Status.HOLD; }
    public void activate()    { this.status = Status.ACTIVE; }

    public boolean isAvailable() { return this.status == Status.ACTIVE && this.quantity > 0; }

    public String getId()               { return id; }
    public String getProductId()        { return productId; }
    public String getWarehouseId()      { return warehouseId; }
    public String getBinLocationId()    { return binLocationId; }
    public String getBatchNumber()      { return batchNumber; }
    public int getQuantity()            { return quantity; }
    public LocalDate getExpiryDate()    { return expiryDate; }
    public LocalDate getManufacturedDate() { return manufacturedDate; }
    public Status getStatus()           { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getVersion()             { return version; }
}
