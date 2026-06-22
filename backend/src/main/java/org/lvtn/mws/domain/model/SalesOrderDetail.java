package org.lvtn.mws.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/** Dòng đơn bán hàng (sales_order_details). Thuần Java. */
public class SalesOrderDetail {

    private final String id;
    private String soId;
    private final String productId;
    private final int quantityOrdered;
    private int quantityPicked;
    private final BigDecimal unitPrice;
    private final BigDecimal discountPercent;

    private SalesOrderDetail(Builder b) {
        this.id              = Objects.requireNonNull(b.id, "id is required");
        this.soId            = b.soId;
        this.productId       = Objects.requireNonNull(b.productId, "productId is required");
        if (b.quantityOrdered <= 0) {
            throw new IllegalArgumentException("Số lượng đặt phải lớn hơn 0");
        }
        this.quantityOrdered = b.quantityOrdered;
        this.quantityPicked  = b.quantityPicked;
        this.unitPrice       = b.unitPrice == null ? BigDecimal.ZERO : b.unitPrice;
        this.discountPercent = b.discountPercent == null ? BigDecimal.ZERO : b.discountPercent;
    }

    public static class Builder {
        private String id;
        private String soId;
        private String productId;
        private int quantityOrdered;
        private int quantityPicked = 0;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder soId(String v)            { this.soId = v; return this; }
        public Builder productId(String v)       { this.productId = v; return this; }
        public Builder quantityOrdered(int v)    { this.quantityOrdered = v; return this; }
        public Builder quantityPicked(int v)     { this.quantityPicked = v; return this; }
        public Builder unitPrice(BigDecimal v)   { this.unitPrice = v; return this; }
        public Builder discountPercent(BigDecimal v) { this.discountPercent = v; return this; }
        public SalesOrderDetail build()          { return new SalesOrderDetail(this); }
    }

    public void attachToOrder(String soId) { this.soId = soId; }

    public void addPicked(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhặt phải lớn hơn 0");
        if (this.quantityPicked + qty > this.quantityOrdered) {
            throw new IllegalArgumentException("Số lượng nhặt vượt quá số lượng đặt");
        }
        this.quantityPicked += qty;
    }

    public String getId()               { return id; }
    public String getSoId()             { return soId; }
    public String getProductId()        { return productId; }
    public int getQuantityOrdered()     { return quantityOrdered; }
    public int getQuantityPicked()      { return quantityPicked; }
    public BigDecimal getUnitPrice()    { return unitPrice; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
}
