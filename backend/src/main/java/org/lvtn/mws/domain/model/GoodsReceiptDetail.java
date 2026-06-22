package org.lvtn.mws.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Line item of a goods receipt. Pure domain object.
 * Note: the goods_receipt_details table has no manufactured_date column, so it
 * is intentionally absent here; batch creation supplies null for that field.
 */
public class GoodsReceiptDetail {

    private final String id;
    private final String grnId;
    private final String productId;
    private final String poDetailId;   // nullable — receipt line may be off-PO
    private final int quantity;
    private final String batchNumber;  // nullable
    private final LocalDate expiryDate; // nullable
    private final String binLocationId;

    private GoodsReceiptDetail(Builder b) {
        this.id            = Objects.requireNonNull(b.id, "GRN detail id is required");
        this.grnId         = Objects.requireNonNull(b.grnId, "grnId is required");
        this.productId     = Objects.requireNonNull(b.productId, "productId is required");
        this.poDetailId    = b.poDetailId;
        if (b.quantity <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        this.quantity      = b.quantity;
        this.batchNumber   = b.batchNumber;
        this.expiryDate    = b.expiryDate;
        this.binLocationId = Objects.requireNonNull(b.binLocationId, "binLocationId is required");
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, grnId, productId, poDetailId, batchNumber, binLocationId;
        private int quantity;
        private LocalDate expiryDate;

        public Builder id(String v)            { this.id = v; return this; }
        public Builder grnId(String v)         { this.grnId = v; return this; }
        public Builder productId(String v)     { this.productId = v; return this; }
        public Builder poDetailId(String v)    { this.poDetailId = v; return this; }
        public Builder quantity(int v)         { this.quantity = v; return this; }
        public Builder batchNumber(String v)   { this.batchNumber = v; return this; }
        public Builder expiryDate(LocalDate v) { this.expiryDate = v; return this; }
        public Builder binLocationId(String v) { this.binLocationId = v; return this; }
        public GoodsReceiptDetail build()      { return new GoodsReceiptDetail(this); }
    }

    public boolean hasBatch() {
        return this.batchNumber != null && !this.batchNumber.isBlank();
    }

    public boolean isLinkedToPoDetail() {
        return this.poDetailId != null && !this.poDetailId.isBlank();
    }

    public String getId()           { return id; }
    public String getGrnId()        { return grnId; }
    public String getProductId()    { return productId; }
    public String getPoDetailId()   { return poDetailId; }
    public int getQuantity()        { return quantity; }
    public String getBatchNumber()  { return batchNumber; }
    public LocalDate getExpiryDate(){ return expiryDate; }
    public String getBinLocationId(){ return binLocationId; }
}
