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
    private int reservedQuantity;   // [Bán theo NCC] số đang giữ chỗ trên lô này
    private String supplierId;      // [Bán theo NCC] NCC của lô (đóng dấu khi nhập)
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
        this.reservedQuantity= b.reservedQuantity;
        this.supplierId      = b.supplierId;
        this.expiryDate      = b.expiryDate;
        this.manufacturedDate= b.manufacturedDate;
        this.status          = b.status != null ? b.status : Status.ACTIVE;
        this.createdAt       = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        this.version         = b.version;
    }

    public static class Builder {
        private String id, productId, warehouseId, binLocationId, batchNumber;
        private int quantity = 0;
        private int reservedQuantity = 0;
        private String supplierId;
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
        public Builder reservedQuantity(int v)    { this.reservedQuantity = v; return this; }
        public Builder supplierId(String v)       { this.supplierId = v; return this; }
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
        // Picking tiêu thụ hàng đã giữ chỗ: nhả bớt reserved cho khớp bất biến reserved<=quantity.
        if (this.reservedQuantity > this.quantity) this.reservedQuantity = this.quantity;
    }

    /** [Bán theo NCC] Số có thể bán/giữ thêm trên lô = tồn thật − đã giữ chỗ. */
    public int availableQuantity() { return this.quantity - this.reservedQuantity; }

    /** Giữ chỗ qty trên lô (khi phân bổ đơn bán). Ném nếu không đủ phần khả dụng. */
    public void reserve(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng giữ chỗ phải lớn hơn 0");
        if (availableQuantity() < qty) {
            throw new InsufficientStockException(
                    "Lô " + batchNumber + " không đủ để giữ chỗ: cần " + qty + ", khả dụng " + availableQuantity());
        }
        this.reservedQuantity += qty;
    }

    /** Nhả giữ chỗ (hủy đơn / khi picking đã tiêu thụ). Không cho âm. */
    public void release(int qty) {
        if (qty <= 0) return;
        this.reservedQuantity = Math.max(0, this.reservedQuantity - qty);
    }

    public void addQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        this.quantity += qty;
    }

    public void markExpired() { this.status = Status.EXPIRED; }
    public void hold()        { this.status = Status.HOLD; }
    public void activate()    { this.status = Status.ACTIVE; }

    /**
     * Sửa NSX/HSD hậu-nhập — dùng khi người dùng sửa 2 cột ngày trên phiếu nhập
     * đã complete và cần đồng bộ xuống lô. Không đụng SL/status/ô kệ để giữ
     * domain method có phạm vi hẹp, dễ suy ra hậu quả.
     */
    public void updateDates(LocalDate manufacturedDate, LocalDate expiryDate) {
        if (manufacturedDate != null && expiryDate != null
                && manufacturedDate.isAfter(expiryDate)) {
            throw new IllegalArgumentException("Ngày sản xuất không được sau hạn sử dụng");
        }
        this.manufacturedDate = manufacturedDate;
        this.expiryDate = expiryDate;
    }

    public boolean isAvailable() { return this.status == Status.ACTIVE && this.quantity > 0; }

    public String getId()               { return id; }
    public String getProductId()        { return productId; }
    public String getWarehouseId()      { return warehouseId; }
    public String getBinLocationId()    { return binLocationId; }
    public String getBatchNumber()      { return batchNumber; }
    public int getQuantity()            { return quantity; }
    public int getReservedQuantity()    { return reservedQuantity; }
    public String getSupplierId()       { return supplierId; }
    public LocalDate getExpiryDate()    { return expiryDate; }
    public LocalDate getManufacturedDate() { return manufacturedDate; }
    public Status getStatus()           { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getVersion()             { return version; }
}
