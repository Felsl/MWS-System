package org.lvtn.mws.domain.model;

import java.util.Objects;

/**
 * Một dòng của phiếu điều chỉnh tồn kho.
 *
 *   quantityChange = afterQuantity - beforeQuantity  (có dấu: + thừa / - thiếu)
 *   beforeQuantity = tồn hệ thống trước điều chỉnh (system_quantity từ kiểm kê)
 *   afterQuantity  = tồn sau điều chỉnh (counted_quantity)
 */
public class AdjustmentVoucherDetail {

    private final String id;
    private final String voucherId;
    private final String productId;
    private final String batchId;          // có thể null
    private final String binLocationId;
    private final int quantityChange;
    private final int beforeQuantity;
    private final int afterQuantity;
    private final String stocktakeDetailId; // có thể null (điều chỉnh thủ công)

    private AdjustmentVoucherDetail(Builder b) {
        this.id                = Objects.requireNonNull(b.id, "id is required");
        this.voucherId         = b.voucherId; // gán khi đưa vào aggregate
        this.productId         = Objects.requireNonNull(b.productId, "productId is required");
        this.batchId           = b.batchId;
        this.binLocationId     = Objects.requireNonNull(b.binLocationId, "binLocationId is required");
        this.quantityChange    = b.quantityChange;
        this.beforeQuantity    = b.beforeQuantity;
        this.afterQuantity     = b.afterQuantity;
        this.stocktakeDetailId = b.stocktakeDetailId;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, voucherId, productId, batchId, binLocationId, stocktakeDetailId;
        private int quantityChange, beforeQuantity, afterQuantity;

        public Builder id(String v)                { this.id = v; return this; }
        public Builder voucherId(String v)         { this.voucherId = v; return this; }
        public Builder productId(String v)         { this.productId = v; return this; }
        public Builder batchId(String v)           { this.batchId = v; return this; }
        public Builder binLocationId(String v)     { this.binLocationId = v; return this; }
        public Builder quantityChange(int v)       { this.quantityChange = v; return this; }
        public Builder beforeQuantity(int v)       { this.beforeQuantity = v; return this; }
        public Builder afterQuantity(int v)        { this.afterQuantity = v; return this; }
        public Builder stocktakeDetailId(String v) { this.stocktakeDetailId = v; return this; }
        public AdjustmentVoucherDetail build()     { return new AdjustmentVoucherDetail(this); }
    }

    /**
     * % chênh lệch của dòng so với tồn hệ thống = |quantityChange| / beforeQuantity * 100.
     * Khi beforeQuantity = 0 (hàng đáng lẽ không có nhưng đếm ra có) coi như 100%.
     */
    public double variancePercent() {
        if (beforeQuantity == 0) return 100.0;
        return Math.abs((double) quantityChange) / beforeQuantity * 100.0;
    }

    public String getId()                { return id; }
    public String getVoucherId()         { return voucherId; }
    public String getProductId()         { return productId; }
    public String getBatchId()           { return batchId; }
    public String getBinLocationId()     { return binLocationId; }
    public int getQuantityChange()       { return quantityChange; }
    public int getBeforeQuantity()       { return beforeQuantity; }
    public int getAfterQuantity()        { return afterQuantity; }
    public String getStocktakeDetailId() { return stocktakeDetailId; }
}
