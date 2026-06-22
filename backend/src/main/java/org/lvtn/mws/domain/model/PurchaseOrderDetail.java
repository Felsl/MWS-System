package org.lvtn.mws.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Line item of a purchase order. Tracks ordered vs cumulative received quantity.
 * Pure domain object — no framework imports.
 */
public class PurchaseOrderDetail {

    private final String id;
    private final String poId;
    private final String productId;
    private final int quantityOrdered;
    private int quantityReceived;
    private final BigDecimal unitPrice;

    private PurchaseOrderDetail(Builder b) {
        this.id               = Objects.requireNonNull(b.id, "PO detail id is required");
        this.poId             = Objects.requireNonNull(b.poId, "poId is required");
        this.productId        = Objects.requireNonNull(b.productId, "productId is required");
        if (b.quantityOrdered <= 0) {
            throw new IllegalArgumentException("Số lượng đặt phải lớn hơn 0");
        }
        this.quantityOrdered  = b.quantityOrdered;
        this.quantityReceived = b.quantityReceived;
        this.unitPrice        = b.unitPrice != null ? b.unitPrice : BigDecimal.ZERO;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, poId, productId;
        private int quantityOrdered;
        private int quantityReceived = 0;
        private BigDecimal unitPrice;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder poId(String v)            { this.poId = v; return this; }
        public Builder productId(String v)       { this.productId = v; return this; }
        public Builder quantityOrdered(int v)    { this.quantityOrdered = v; return this; }
        public Builder quantityReceived(int v)   { this.quantityReceived = v; return this; }
        public Builder unitPrice(BigDecimal v)   { this.unitPrice = v; return this; }
        public PurchaseOrderDetail build()       { return new PurchaseOrderDetail(this); }
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    /** Over-receiving guard: would receiving {@code qty} more exceed the ordered amount? */
    public boolean wouldExceedOnReceiving(int qty) {
        return (this.quantityReceived + qty) > this.quantityOrdered;
    }

    /** Accumulate received quantity on goods-receipt commit. */
    public void addReceived(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhận phải lớn hơn 0");
        if (wouldExceedOnReceiving(qty)) {
            throw new IllegalArgumentException(
                    "Lỗi: Số lượng nhập vượt quá số lượng đặt mua trong đơn PO gốc");
        }
        this.quantityReceived += qty;
    }

    public boolean isFullyReceived() {
        return this.quantityReceived >= this.quantityOrdered;
    }

    public int remainingQuantity() {
        return Math.max(0, this.quantityOrdered - this.quantityReceived);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()           { return id; }
    public String getPoId()         { return poId; }
    public String getProductId()    { return productId; }
    public int getQuantityOrdered() { return quantityOrdered; }
    public int getQuantityReceived(){ return quantityReceived; }
    public BigDecimal getUnitPrice(){ return unitPrice; }
}
