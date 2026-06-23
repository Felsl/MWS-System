package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Phiên kiểm kê kho (UC Giai đoạn 6).
 *
 * Vòng đời trạng thái:
 *   (tạo) -> FREEZED  : ngay khi bắt đầu kiểm kê, kho bị "đóng băng" mọi biến động tồn.
 *   FREEZED -> ADJUSTED: khi hoàn tất phiên (mở băng + sinh phiếu điều chỉnh DRAFT).
 *
 * OPEN được giữ trong enum cho khả năng mở rộng (vd tạo phiên nháp trước khi đóng băng),
 * hiện luồng chuẩn tạo thẳng ở FREEZED.
 */
public class StocktakeSession {

    public enum Status { OPEN, FREEZED, ADJUSTED }

    private final String id;
    private final String warehouseId;
    private Status status;
    private LocalDateTime freezeStartedAt;
    private LocalDateTime freezeEndedAt;
    private final String createdBy;
    private final LocalDateTime createdAt;

    private StocktakeSession(Builder b) {
        this.id              = Objects.requireNonNull(b.id, "id is required");
        this.warehouseId     = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.status          = b.status != null ? b.status : Status.FREEZED;
        this.freezeStartedAt = b.freezeStartedAt;
        this.freezeEndedAt   = b.freezeEndedAt;
        this.createdBy       = Objects.requireNonNull(b.createdBy, "createdBy is required");
        this.createdAt       = b.createdAt != null ? b.createdAt : LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, warehouseId, createdBy;
        private Status status;
        private LocalDateTime freezeStartedAt, freezeEndedAt, createdAt;

        public Builder id(String v)                       { this.id = v; return this; }
        public Builder warehouseId(String v)              { this.warehouseId = v; return this; }
        public Builder status(Status v)                   { this.status = v; return this; }
        public Builder freezeStartedAt(LocalDateTime v)   { this.freezeStartedAt = v; return this; }
        public Builder freezeEndedAt(LocalDateTime v)     { this.freezeEndedAt = v; return this; }
        public Builder createdBy(String v)                { this.createdBy = v; return this; }
        public Builder createdAt(LocalDateTime v)         { this.createdAt = v; return this; }
        public StocktakeSession build()                   { return new StocktakeSession(this); }
    }

    /** Hoàn tất phiên: FREEZED -> ADJUSTED, mở băng kho. */
    public void complete() {
        if (this.status != Status.FREEZED) {
            throw new IllegalStateException("Chỉ hoàn tất được phiên đang ở trạng thái FREEZED");
        }
        this.status = Status.ADJUSTED;
        this.freezeEndedAt = LocalDateTime.now();
    }

    public boolean isFrozen() { return this.status == Status.FREEZED; }

    public String getId()                     { return id; }
    public String getWarehouseId()            { return warehouseId; }
    public Status getStatus()                 { return status; }
    public LocalDateTime getFreezeStartedAt() { return freezeStartedAt; }
    public LocalDateTime getFreezeEndedAt()   { return freezeEndedAt; }
    public String getCreatedBy()              { return createdBy; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
}
