package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root for a goods receipt note (GRN). Pure domain object.
 * Status: PENDING -> COMPLETED.
 */
public class GoodsReceipt {

    public enum Status { PENDING, COMPLETED }

    private final String id;
    private final String grnNumber;
    private final String poId;        // nullable — allows ad-hoc receipts outside a PO
    private final String warehouseId;
    private Status status;
    private final String receivedBy;
    private final LocalDateTime receivedAt;
    private String note;

    private GoodsReceipt(Builder b) {
        this.id          = Objects.requireNonNull(b.id, "Goods receipt id is required");
        this.grnNumber   = Objects.requireNonNull(b.grnNumber, "grnNumber is required");
        this.poId        = b.poId;
        this.warehouseId = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.status      = b.status != null ? b.status : Status.PENDING;
        this.receivedBy  = Objects.requireNonNull(b.receivedBy, "receivedBy is required");
        this.receivedAt  = b.receivedAt != null ? b.receivedAt : LocalDateTime.now();
        this.note        = b.note;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, grnNumber, poId, warehouseId, receivedBy, note;
        private Status status;
        private LocalDateTime receivedAt;

        public Builder id(String v)               { this.id = v; return this; }
        public Builder grnNumber(String v)        { this.grnNumber = v; return this; }
        public Builder poId(String v)             { this.poId = v; return this; }
        public Builder warehouseId(String v)      { this.warehouseId = v; return this; }
        public Builder status(Status v)           { this.status = v; return this; }
        public Builder receivedBy(String v)       { this.receivedBy = v; return this; }
        public Builder receivedAt(LocalDateTime v){ this.receivedAt = v; return this; }
        public Builder note(String v)             { this.note = v; return this; }
        public GoodsReceipt build()               { return new GoodsReceipt(this); }
    }

    // ── Behaviour ────────────────────────────────────────────────────────────

    public boolean isLinkedToPurchaseOrder() {
        return this.poId != null && !this.poId.isBlank();
    }

    public void complete() {
        if (this.status == Status.COMPLETED) {
            throw new IllegalStateException("Phiếu nhập đã ở trạng thái COMPLETED");
        }
        this.status = Status.COMPLETED;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()             { return id; }
    public String getGrnNumber()      { return grnNumber; }
    public String getPoId()           { return poId; }
    public String getWarehouseId()    { return warehouseId; }
    public Status getStatus()         { return status; }
    public String getReceivedBy()     { return receivedBy; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public String getNote()           { return note; }
}
