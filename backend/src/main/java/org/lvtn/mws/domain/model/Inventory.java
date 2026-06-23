package org.lvtn.mws.domain.model;

import java.util.Objects;

public class Inventory {

    private final String productId;
    private final String warehouseId;
    private int quantity;
    private int reservedQuantity;
    private int version;

    private Inventory(Builder b) {
        this.productId        = Objects.requireNonNull(b.productId,   "productId is required");
        this.warehouseId      = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.quantity         = b.quantity;
        this.reservedQuantity = b.reservedQuantity;
        this.version          = b.version;
    }

    public static class Builder {
        private String productId, warehouseId;
        private int quantity = 0, reservedQuantity = 0, version = 0;

        public Builder productId(String v)        { this.productId = v; return this; }
        public Builder warehouseId(String v)      { this.warehouseId = v; return this; }
        public Builder quantity(int v)            { this.quantity = v; return this; }
        public Builder reservedQuantity(int v)    { this.reservedQuantity = v; return this; }
        public Builder version(int v)             { this.version = v; return this; }
        public Inventory build()                  { return new Inventory(this); }
    }

    // ── Business behaviour ───────────────────────────────────────────────────

    /** Increase quantity (on goods receipt). */
    public void addStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        this.quantity += qty;
    }

    /**
     * [GIAI ĐOẠN 6] Tăng tồn tổng do điều chỉnh kiểm kê (thừa hàng).
     * Tách riêng addStock để rõ ngữ nghĩa "điều chỉnh".
     */
    public void increaseQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng điều chỉnh tăng phải lớn hơn 0");
        this.quantity += qty;
    }

    /**
     * [GIAI ĐOẠN 6] Giảm tồn tổng do điều chỉnh kiểm kê (thiếu hàng).
     * Chỉ chạm 'quantity', không đụng 'reservedQuantity'; đảm bảo quantity - qty >= reserved
     * để không vi phạm ràng buộc chk_inv_logical (quantity >= reserved_quantity).
     */
    public void decreaseQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng điều chỉnh giảm phải lớn hơn 0");
        if (this.quantity - qty < this.reservedQuantity) {
            throw new IllegalStateException(
                    "Không thể giảm tồn xuống dưới phần đang giữ chỗ (reserved=" + reservedQuantity
                            + ", quantity=" + quantity + ", giảm=" + qty + ")");
        }
        this.quantity -= qty;
    }

    public void reserve(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng giữ chỗ phải lớn hơn 0");
        if (availableQuantity() < qty) {
            throw new InsufficientStockException(
                    "Tồn kho khả dụng không đủ: cần " + qty + ", còn " + availableQuantity());
        }
        this.reservedQuantity += qty;
    }

    public void release(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng giải phóng phải lớn hơn 0");
        if (this.reservedQuantity < qty) {
            throw new IllegalStateException("Số lượng giữ chỗ không đủ để giải phóng");
        }
        this.reservedQuantity -= qty;
    }

    public void commitDeduction(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        if (this.quantity < qty) {
            throw new InsufficientStockException(
                    "Tồn kho thực tế không đủ để xuất: cần " + qty + ", còn " + this.quantity);
        }
        if (this.reservedQuantity < qty) {
            throw new IllegalStateException("Số lượng giữ chỗ không đủ để commit");
        }
        this.quantity         -= qty;
        this.reservedQuantity -= qty;
    }

    public int availableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    public String getProductId()       { return productId; }
    public String getWarehouseId()     { return warehouseId; }
    public int getQuantity()           { return quantity; }
    public int getReservedQuantity()   { return reservedQuantity; }
    public int getVersion()            { return version; }
}
