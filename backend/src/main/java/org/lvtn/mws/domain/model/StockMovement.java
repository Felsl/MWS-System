package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable audit-trail record of a single stock movement (UC-24).
 * Created automatically when a goods receipt is completed. Pure domain object.
 */
public class StockMovement {

    public enum MovementType { IN, OUT, ADJUST }
    public enum ReferenceType { GOODS_RECEIPT, SALES_ORDER, TRANSFER, ADJUSTMENT }

    private final String id;
    private final String productId;
    private final String warehouseId;
    private final String binLocationId;
    private final String batchId;       // nullable when receipt has no batch
    private final MovementType movementType;
    private final int quantity;
    private final ReferenceType referenceType;
    private final String referenceId;
    private final LocalDateTime createdAt;

    private StockMovement(Builder b) {
        this.id            = Objects.requireNonNull(b.id, "StockMovement id is required");
        this.productId     = Objects.requireNonNull(b.productId, "productId is required");
        this.warehouseId   = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.binLocationId = b.binLocationId;
        this.batchId       = b.batchId;
        this.movementType  = Objects.requireNonNull(b.movementType, "movementType is required");
        if (b.quantity <= 0) throw new IllegalArgumentException("Số lượng ghi vết phải lớn hơn 0");
        this.quantity      = b.quantity;
        this.referenceType = Objects.requireNonNull(b.referenceType, "referenceType is required");
        this.referenceId   = b.referenceId;
        this.createdAt     = b.createdAt != null ? b.createdAt : LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, productId, warehouseId, binLocationId, batchId, referenceId;
        private MovementType movementType;
        private ReferenceType referenceType;
        private int quantity;
        private LocalDateTime createdAt;

        public Builder id(String v)                  { this.id = v; return this; }
        public Builder productId(String v)           { this.productId = v; return this; }
        public Builder warehouseId(String v)         { this.warehouseId = v; return this; }
        public Builder binLocationId(String v)       { this.binLocationId = v; return this; }
        public Builder batchId(String v)             { this.batchId = v; return this; }
        public Builder movementType(MovementType v)  { this.movementType = v; return this; }
        public Builder quantity(int v)               { this.quantity = v; return this; }
        public Builder referenceType(ReferenceType v){ this.referenceType = v; return this; }
        public Builder referenceId(String v)         { this.referenceId = v; return this; }
        public Builder createdAt(LocalDateTime v)    { this.createdAt = v; return this; }
        public StockMovement build()                 { return new StockMovement(this); }
    }

    public String getId()                 { return id; }
    public String getProductId()          { return productId; }
    public String getWarehouseId()        { return warehouseId; }
    public String getBinLocationId()      { return binLocationId; }
    public String getBatchId()            { return batchId; }
    public MovementType getMovementType() { return movementType; }
    public int getQuantity()              { return quantity; }
    public ReferenceType getReferenceType(){ return referenceType; }
    public String getReferenceId()        { return referenceId; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
}
