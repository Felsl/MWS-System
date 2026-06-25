package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root cho luồng điều chuyển nội bộ (Internal Transfer).
 * Thuần Java — không phụ thuộc bất kỳ framework nào ngoài JDK.
 *
 * Vòng đời: DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> COMPLETED
 * (rẽ nhánh: REJECTED / CANCELLED)
 */
public class TransferOrder {

    public enum Status {
        DRAFT,        // vừa lập phiếu
        PENDING_APPROVAL,    // đã gửi duyệt + đã giữ chỗ ảo ở kho nguồn
        APPROVED,     // quản lý duyệt + đã chạy FEFO gán lô/ô kệ nguồn
        IN_TRANSIT,   // xe đã rời kho nguồn, hàng đang đi đường
        COMPLETED,    // kho đích đã nhận + đối soát xong
        REJECTED,     // bị từ chối duyệt
        CANCELLED     // huỷ
    }

    private final String id;
    private final String fromWarehouseId;
    private final String toWarehouseId;
    private final String transferNumber;
    private Status status;
    private final String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<TransferOrderDetail> details;

    private TransferOrder(Builder b) {
        this.id              = Objects.requireNonNull(b.id, "Transfer id is required");
        this.fromWarehouseId = Objects.requireNonNull(b.fromWarehouseId, "Kho nguồn không được trống");
        this.toWarehouseId   = Objects.requireNonNull(b.toWarehouseId, "Kho đích không được trống");
        // Bẫy lỗi logic chk_diff_warehouse: chặn ngay ở tầng domain
        if (this.fromWarehouseId.equals(this.toWarehouseId)) {
            throw new IllegalArgumentException("Kho nguồn và kho đích không được trùng nhau");
        }
        this.transferNumber  = Objects.requireNonNull(b.transferNumber, "Số phiếu điều chuyển không được trống");
        this.status          = b.status != null ? b.status : Status.DRAFT;
        this.createdBy       = Objects.requireNonNull(b.createdBy, "Người tạo không được trống");
        this.approvedBy      = b.approvedBy;
        this.approvedAt      = b.approvedAt;
        this.createdAt       = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        this.updatedAt       = b.updatedAt != null ? b.updatedAt : this.createdAt;
        this.details         = b.details != null ? new ArrayList<>(b.details) : new ArrayList<>();
    }

    public static class Builder {
        private String id;
        private String fromWarehouseId;
        private String toWarehouseId;
        private String transferNumber;
        private Status status;
        private String createdBy;
        private String approvedBy;
        private LocalDateTime approvedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<TransferOrderDetail> details;

        public Builder id(String v)                       { this.id = v; return this; }
        public Builder fromWarehouseId(String v)          { this.fromWarehouseId = v; return this; }
        public Builder toWarehouseId(String v)            { this.toWarehouseId = v; return this; }
        public Builder transferNumber(String v)           { this.transferNumber = v; return this; }
        public Builder status(Status v)                   { this.status = v; return this; }
        public Builder createdBy(String v)                { this.createdBy = v; return this; }
        public Builder approvedBy(String v)               { this.approvedBy = v; return this; }
        public Builder approvedAt(LocalDateTime v)        { this.approvedAt = v; return this; }
        public Builder createdAt(LocalDateTime v)         { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)         { this.updatedAt = v; return this; }
        public Builder details(List<TransferOrderDetail> v){ this.details = v; return this; }

        public TransferOrder build() { return new TransferOrder(this); }
    }

    // ── Business behaviour (chuyển trạng thái) ───────────────────────────────

    /** DRAFT -> PENDING_APPROVAL (sau khi đã giữ chỗ ảo ở kho nguồn). */
    public void requestApproval() {
        ensure(Status.DRAFT, "Chỉ phiếu ở trạng thái DRAFT mới được gửi duyệt");
        if (details.isEmpty()) {
            throw new IllegalStateException("Phiếu điều chuyển không có dòng hàng nào");
        }
        this.status = Status.PENDING_APPROVAL;
        touch();
    }

    /**
     * PENDING_APPROVAL -> APPROVED. Thay danh sách detail bằng danh sách đã chạy FEFO
     * (đã gán batchId + fromBinLocationId, có thể đã tách dòng theo nhiều lô).
     */
    public void approve(String approvedBy, List<TransferOrderDetail> allocatedDetails) {
        ensure(Status.PENDING_APPROVAL, "Chỉ phiếu ở trạng thái PENDING_APPROVAL mới được duyệt");
        Objects.requireNonNull(approvedBy, "Người duyệt không được trống");
        if (allocatedDetails == null || allocatedDetails.isEmpty()) {
            throw new IllegalStateException("Không phân bổ được lô hàng (FEFO) cho phiếu");
        }
        this.details.clear();
        this.details.addAll(allocatedDetails);
        this.status     = Status.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        touch();
    }

    /** APPROVED -> IN_TRANSIT (xe lăn bánh rời kho nguồn). */
    public void markInTransit() {
        ensure(Status.APPROVED, "Chỉ phiếu đã duyệt (APPROVED) mới được xuất kho đi đường");
        this.status = Status.IN_TRANSIT;
        touch();
    }

    /** IN_TRANSIT -> COMPLETED (kho đích đã nhận & đối soát). */
    public void complete() {
        ensure(Status.IN_TRANSIT, "Chỉ phiếu đang đi đường (IN_TRANSIT) mới được hoàn tất");
        this.status = Status.COMPLETED;
        touch();
    }

    public void reject(String by) {
        ensure(Status.PENDING_APPROVAL, "Chỉ phiếu chờ duyệt mới được từ chối");
        this.status = Status.REJECTED;
        this.approvedBy = by;
        this.approvedAt = LocalDateTime.now();
        touch();
    }

    public void cancel() {
        if (this.status == Status.IN_TRANSIT || this.status == Status.COMPLETED) {
            throw new IllegalStateException("Không thể huỷ phiếu đang đi đường hoặc đã hoàn tất");
        }
        this.status = Status.CANCELLED;
        touch();
    }

    /** Ghi nhận số lượng thực nhận + ô kệ cất hàng cho 1 dòng tại kho đích. */
    public TransferOrderDetail receiveLine(String detailId, int quantityReceived, String binLocationId) {
        TransferOrderDetail line = details.stream()
                .filter(d -> d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dòng chi tiết: " + detailId));
        line.receive(quantityReceived, binLocationId);
        touch();
        return line;
    }

    private void ensure(Status expected, String message) {
        if (this.status != expected) {
            throw new IllegalStateException(message + " (hiện tại: " + this.status + ")");
        }
    }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()                       { return id; }
    public String getFromWarehouseId()          { return fromWarehouseId; }
    public String getToWarehouseId()            { return toWarehouseId; }
    public String getTransferNumber()           { return transferNumber; }
    public Status getStatus()                   { return status; }
    public String getCreatedBy()                { return createdBy; }
    public String getApprovedBy()               { return approvedBy; }
    public LocalDateTime getApprovedAt()        { return approvedAt; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public List<TransferOrderDetail> getDetails() { return new ArrayList<>(details); }
}
