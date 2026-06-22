package org.lvtn.mws.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root for a purchase order.
 * Pure domain object — no framework imports.
 * Workflow: DRAFT -> PENDING_REVIEW -> PENDING_APPROVAL -> APPROVED -> (CLOSED) | REJECTED | CANCELLED
 */
public class PurchaseOrder {

    public enum Status {
        DRAFT, PENDING_REVIEW, PENDING_APPROVAL, APPROVED, ORDERED, CLOSED, REJECTED, CANCELLED
    }

    private final String id;
    private final String poNumber;
    private final String supplierId;
    private final String warehouseId;
    private Status status;
    private LocalDate expectedDate;
    private final String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PurchaseOrder(Builder b) {
        this.id           = Objects.requireNonNull(b.id, "Purchase order id is required");
        this.poNumber     = Objects.requireNonNull(b.poNumber, "poNumber is required");
        this.supplierId   = Objects.requireNonNull(b.supplierId, "supplierId is required");
        this.warehouseId  = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.status       = b.status != null ? b.status : Status.DRAFT;
        this.expectedDate = b.expectedDate;
        this.createdBy    = Objects.requireNonNull(b.createdBy, "createdBy is required");
        this.approvedBy   = b.approvedBy;
        this.approvedAt   = b.approvedAt;
        this.createdAt    = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        this.updatedAt    = b.updatedAt != null ? b.updatedAt : this.createdAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, poNumber, supplierId, warehouseId, createdBy, approvedBy;
        private Status status;
        private LocalDate expectedDate;
        private LocalDateTime approvedAt, createdAt, updatedAt;

        public Builder id(String v)               { this.id = v; return this; }
        public Builder poNumber(String v)         { this.poNumber = v; return this; }
        public Builder supplierId(String v)       { this.supplierId = v; return this; }
        public Builder warehouseId(String v)      { this.warehouseId = v; return this; }
        public Builder status(Status v)           { this.status = v; return this; }
        public Builder expectedDate(LocalDate v)  { this.expectedDate = v; return this; }
        public Builder createdBy(String v)        { this.createdBy = v; return this; }
        public Builder approvedBy(String v)       { this.approvedBy = v; return this; }
        public Builder approvedAt(LocalDateTime v){ this.approvedAt = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public PurchaseOrder build()              { return new PurchaseOrder(this); }
    }

    // ── Workflow behaviour ────────────────────────────────────────────────────

    public void submitForReview() {
        if (this.status != Status.DRAFT) {
            throw new IllegalStateException("Chỉ đơn ở trạng thái DRAFT mới được gửi duyệt");
        }
        this.status = Status.PENDING_REVIEW;
        touch();
    }

    public void submitForApproval() {
        if (this.status != Status.PENDING_REVIEW) {
            throw new IllegalStateException("Chỉ đơn ở trạng thái PENDING_REVIEW mới được trình duyệt");
        }
        this.status = Status.PENDING_APPROVAL;
        touch();
    }

    public void approve(String approvedBy) {
        if (this.status != Status.PENDING_APPROVAL) {
            throw new IllegalStateException("Chỉ đơn ở trạng thái PENDING_APPROVAL mới được phê duyệt");
        }
        Objects.requireNonNull(approvedBy, "Người duyệt không được trống");
        this.status     = Status.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        touch();
    }

    public void reject() {
        if (this.status != Status.PENDING_REVIEW && this.status != Status.PENDING_APPROVAL) {
            throw new IllegalStateException("Chỉ đơn đang chờ duyệt mới được từ chối");
        }
        this.status = Status.REJECTED;
        touch();
    }

    public void cancel() {
        if (this.status == Status.CLOSED || this.status == Status.CANCELLED) {
            throw new IllegalStateException("Đơn đã đóng hoặc đã huỷ, không thể huỷ");
        }
        this.status = Status.CANCELLED;
        touch();
    }

    /** Mark as fully received. */
    public void close() {
        if (this.status != Status.APPROVED && this.status != Status.ORDERED) {
            throw new IllegalStateException("Chỉ đơn đã duyệt mới có thể đóng");
        }
        this.status = Status.CLOSED;
        touch();
    }

    /** Business guard: only APPROVED orders may back a goods receipt. */
    public boolean canBeReceived() {
        return this.status == Status.APPROVED || this.status == Status.ORDERED;
    }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getId()              { return id; }
    public String getPoNumber()        { return poNumber; }
    public String getSupplierId()      { return supplierId; }
    public String getWarehouseId()     { return warehouseId; }
    public Status getStatus()          { return status; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public String getCreatedBy()       { return createdBy; }
    public String getApprovedBy()      { return approvedBy; }
    public LocalDateTime getApprovedAt(){ return approvedAt; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
