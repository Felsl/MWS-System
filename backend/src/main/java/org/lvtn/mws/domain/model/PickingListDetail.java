package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** Chi tiết dòng nhặt (picking_list_details). Thuần Java. Chứa logic bẫy lỗi FEFO khi quét mã. */
public class PickingListDetail {

    private final String id;
    private String pickingListId;
    private final String productId;
    private final String batchId;        // Lô hệ thống đề xuất (FEFO / hoặc lô chỉ định)
    private final String requiredBatchId; // != null => BẮT BUỘC quét đúng lô này (lô chỉ định); null => cho quét tự do
    private String actualBatchId;        // Lô thực tế quét được
    private final String binLocationId;
    private final int quantityToPick;
    private int quantityPicked;
    private boolean confirmed;
    private String confirmedBy;
    private LocalDateTime confirmedAt;

    private PickingListDetail(Builder b) {
        this.id             = Objects.requireNonNull(b.id, "id is required");
        this.pickingListId  = b.pickingListId;
        this.productId      = Objects.requireNonNull(b.productId, "productId is required");
        this.batchId        = Objects.requireNonNull(b.batchId, "batchId is required");
        this.requiredBatchId = b.requiredBatchId;
        this.actualBatchId  = b.actualBatchId;
        this.binLocationId  = Objects.requireNonNull(b.binLocationId, "binLocationId is required");
        if (b.quantityToPick <= 0) {
            throw new IllegalArgumentException("Số lượng cần nhặt phải lớn hơn 0");
        }
        this.quantityToPick = b.quantityToPick;
        this.quantityPicked = b.quantityPicked;
        this.confirmed      = b.confirmed;
        this.confirmedBy    = b.confirmedBy;
        this.confirmedAt    = b.confirmedAt;
    }

    public static class Builder {
        private String id;
        private String pickingListId;
        private String productId;
        private String batchId;
        private String requiredBatchId;
        private String actualBatchId;
        private String binLocationId;
        private int quantityToPick;
        private int quantityPicked = 0;
        private boolean confirmed = false;
        private String confirmedBy;
        private LocalDateTime confirmedAt;

        public Builder id(String v)            { this.id = v; return this; }
        public Builder requiredBatchId(String v){ this.requiredBatchId = v; return this; }
        public Builder pickingListId(String v) { this.pickingListId = v; return this; }
        public Builder productId(String v)     { this.productId = v; return this; }
        public Builder batchId(String v)       { this.batchId = v; return this; }
        public Builder actualBatchId(String v) { this.actualBatchId = v; return this; }
        public Builder binLocationId(String v) { this.binLocationId = v; return this; }
        public Builder quantityToPick(int v)   { this.quantityToPick = v; return this; }
        public Builder quantityPicked(int v)   { this.quantityPicked = v; return this; }
        public Builder confirmed(boolean v)    { this.confirmed = v; return this; }
        public Builder confirmedBy(String v)   { this.confirmedBy = v; return this; }
        public Builder confirmedAt(LocalDateTime v) { this.confirmedAt = v; return this; }
        public PickingListDetail build()       { return new PickingListDetail(this); }
    }

    public void attachToList(String pickingListId) { this.pickingListId = pickingListId; }

    /**
     * Đối soát mã lô (Double-Check Verification).
     * scannedBatchId = id lô tra cứu được từ batch_number (mã vạch) công nhân vừa quét.
     * Nếu khác lô hệ thống chỉ định -> chặn (kỷ luật FEFO).
     */
    public void confirmScan(String scannedBatchId, String confirmedBy) {
        Objects.requireNonNull(scannedBatchId, "scannedBatchId không được trống");
        if (this.confirmed) {
            throw new IllegalStateException("Dòng này đã được xác nhận trước đó");
        }
        if (!this.batchId.equals(scannedBatchId)) {
            throw new IllegalArgumentException(
                    "Sai mã lô hàng! Lô hàng bạn vừa quét không phải là lô cận hạn nhất cần xuất. " +
                    "Vui lòng kiểm tra lại!");
        }
        // Số lượng do FEFO engine chốt sẵn ở quantityToPick, công nhân không nhập tay.
        // Quét đúng lô = nhận trọn số lượng đã phân bổ cho dòng này.
        this.actualBatchId  = scannedBatchId;
        this.quantityPicked = this.quantityToPick;
        this.confirmed      = true;
        this.confirmedBy    = confirmedBy;
        this.confirmedAt    = LocalDateTime.now();
    }

    /**
     * Báo thiếu hàng thực tế (short-pick): công nhân tới đúng lô nhưng chỉ lấy được actualQty
     * (nhỏ hơn quantityToPick). Dòng này coi như đã xử lý xong với số thực lấy; phần thiếu sẽ
     * được DomainService tự bù bằng dòng nhặt mới từ lô FEFO kế tiếp.
     * quantityToPick GIỮ NGUYÊN để truy vết "định lấy bao nhiêu" so với "lấy được bao nhiêu".
     */
    /**
     * Xác nhận đã nhặt cho ĐIỀU CHUYỂN: ghi lô THỰC TẾ đã quét, KHÔNG khoá theo lô FEFO gợi ý.
     * (Việc kiểm "đúng lô chỉ định / lô ACTIVE hợp lệ" do TransferPickingDomainService lo trước khi gọi.)
     */
    public void confirmPicked(String actualBatchId, String confirmedBy) {
        Objects.requireNonNull(actualBatchId, "actualBatchId không được trống");
        if (this.confirmed) {
            throw new IllegalStateException("Dòng này đã được xác nhận trước đó");
        }
        this.actualBatchId  = actualBatchId;
        this.quantityPicked = this.quantityToPick;
        this.confirmed      = true;
        this.confirmedBy    = confirmedBy;
        this.confirmedAt    = LocalDateTime.now();
    }

    public void confirmShort(String scannedBatchId, int actualQty, String confirmedBy) {
        Objects.requireNonNull(scannedBatchId, "scannedBatchId không được trống");
        if (this.confirmed) {
            throw new IllegalStateException("Dòng này đã được xác nhận trước đó");
        }
        // [Báo thiếu] KHÔNG bắt buộc trùng lô FEFO chỉ định: người lấy có thể lấy từ lô khác
        // (cùng sản phẩm + NCC + kho) — tính hợp lệ của lô thay thế được kiểm ở tầng service.
        if (actualQty < 1 || actualQty > this.quantityToPick) {
            throw new IllegalArgumentException(
                    "Số lượng thực lấy phải trong [1, " + this.quantityToPick + "]");
        }
        this.actualBatchId  = scannedBatchId;
        this.quantityPicked = actualQty;
        this.confirmed      = true;
        this.confirmedBy    = confirmedBy;
        this.confirmedAt    = LocalDateTime.now();
    }

    /** Phần còn thiếu so với kế hoạch (dùng để tự bù từ lô kế tiếp). */
    public int shortfall() {
        return this.quantityToPick - this.quantityPicked;
    }

    public String getId()             { return id; }
    public String getPickingListId()  { return pickingListId; }
    public String getProductId()      { return productId; }
    public String getBatchId()        { return batchId; }
    public String getRequiredBatchId() { return requiredBatchId; }
    public String getActualBatchId()  { return actualBatchId; }
    public String getBinLocationId()  { return binLocationId; }
    public int getQuantityToPick()    { return quantityToPick; }
    public int getQuantityPicked()    { return quantityPicked; }
    public boolean isConfirmed()      { return confirmed; }
    public String getConfirmedBy()    { return confirmedBy; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
}
