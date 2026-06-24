package org.lvtn.mws.domain.model;

import java.util.Objects;

/**
 * Dòng chi tiết phiếu điều chuyển (1 dòng = 1 lô hàng tại 1 ô kệ nguồn).
 * Thuần Java.
 */
public class TransferOrderDetail {

    private final String id;
    private final String transferOrderId;
    private final String productId;
    private String batchId;            // FEFO điền ở bước approve
    private final int quantity;        // số lượng bốc đi tại kho nguồn (> 0)
    private int quantityReceived;      // số lượng thực nhận tại kho đích
    private String fromBinLocationId;  // FEFO điền ở bước approve
    private String binLocationId;      // ô kệ cất hàng tại kho đích (putaway)

    private TransferOrderDetail(Builder b) {
        this.id                = Objects.requireNonNull(b.id, "Detail id is required");
        this.transferOrderId   = Objects.requireNonNull(b.transferOrderId, "transferOrderId is required");
        this.productId         = Objects.requireNonNull(b.productId, "productId is required");
        this.batchId           = b.batchId;
        this.quantity          = b.quantity;
        this.quantityReceived  = b.quantityReceived;
        this.fromBinLocationId = b.fromBinLocationId;
        this.binLocationId     = b.binLocationId;
        if (this.quantity <= 0) {
            throw new IllegalArgumentException("Số lượng điều chuyển phải lớn hơn 0");
        }
    }

    public static class Builder {
        private String id;
        private String transferOrderId;
        private String productId;
        private String batchId;
        private int quantity;
        private int quantityReceived = 0;
        private String fromBinLocationId;
        private String binLocationId;

        public Builder id(String v)               { this.id = v; return this; }
        public Builder transferOrderId(String v)  { this.transferOrderId = v; return this; }
        public Builder productId(String v)        { this.productId = v; return this; }
        public Builder batchId(String v)          { this.batchId = v; return this; }
        public Builder quantity(int v)            { this.quantity = v; return this; }
        public Builder quantityReceived(int v)    { this.quantityReceived = v; return this; }
        public Builder fromBinLocationId(String v){ this.fromBinLocationId = v; return this; }
        public Builder binLocationId(String v)    { this.binLocationId = v; return this; }

        public TransferOrderDetail build() { return new TransferOrderDetail(this); }
    }

    /** Gán kết quả FEFO: lô + ô kệ lấy hàng tại kho nguồn. */
    public void allocate(String batchId, String fromBinLocationId) {
        this.batchId           = Objects.requireNonNull(batchId, "batchId không được trống");
        this.fromBinLocationId = Objects.requireNonNull(fromBinLocationId, "fromBinLocationId không được trống");
    }

    /** Ghi nhận số nhận thực tế + ô kệ cất hàng tại kho đích. */
    public void receive(int quantityReceived, String binLocationId) {
        if (quantityReceived < 0) {
            throw new IllegalArgumentException("Số lượng nhận không được âm");
        }
        if (quantityReceived > this.quantity) {
            throw new IllegalArgumentException(
                    "Số nhận (" + quantityReceived + ") không được lớn hơn số gửi (" + this.quantity + ")");
        }
        this.quantityReceived = quantityReceived;
        this.binLocationId    = Objects.requireNonNull(binLocationId, "Ô kệ cất hàng (kho đích) không được trống");
    }

    /** Lượng hao hụt dọc đường = số gửi - số nhận. */
    public int lostQuantity() {
        return this.quantity - this.quantityReceived;
    }

    public String getId()                { return id; }
    public String getTransferOrderId()   { return transferOrderId; }
    public String getProductId()         { return productId; }
    public String getBatchId()           { return batchId; }
    public int getQuantity()             { return quantity; }
    public int getQuantityReceived()     { return quantityReceived; }
    public String getFromBinLocationId() { return fromBinLocationId; }
    public String getBinLocationId()     { return binLocationId; }
}
