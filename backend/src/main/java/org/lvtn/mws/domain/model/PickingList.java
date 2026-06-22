package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Lệnh gom hàng (picking_lists) + chi tiết nhặt (picking_list_details).
 * Thuần Java.
 */
public class PickingList {

    public enum Status { PENDING, PICKING, COMPLETED }

    private final String id;
    private final String soId;
    private String assignedTo;
    private Status status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private final List<PickingListDetail> details = new ArrayList<>();

    private PickingList(Builder b) {
        this.id          = Objects.requireNonNull(b.id, "id is required");
        this.soId        = Objects.requireNonNull(b.soId, "soId is required");
        this.assignedTo  = b.assignedTo;
        this.status      = b.status == null ? Status.PENDING : b.status;
        this.startedAt   = b.startedAt;
        this.completedAt = b.completedAt;
        if (b.details != null) this.details.addAll(b.details);
    }

    public static class Builder {
        private String id;
        private String soId;
        private String assignedTo;
        private Status status;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private List<PickingListDetail> details;

        public Builder id(String v)              { this.id = v; return this; }
        public Builder soId(String v)            { this.soId = v; return this; }
        public Builder assignedTo(String v)      { this.assignedTo = v; return this; }
        public Builder status(Status v)          { this.status = v; return this; }
        public Builder startedAt(LocalDateTime v){ this.startedAt = v; return this; }
        public Builder completedAt(LocalDateTime v){ this.completedAt = v; return this; }
        public Builder details(List<PickingListDetail> v) { this.details = v; return this; }
        public PickingList build()               { return new PickingList(this); }
    }

    public void addDetail(PickingListDetail d) {
        this.details.add(Objects.requireNonNull(d, "detail không được null"));
    }

    /** PENDING -> PICKING: công nhân nhận lệnh. */
    public void assign(String userId) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("Lệnh đã được nhận hoặc đã hoàn tất");
        }
        this.assignedTo = Objects.requireNonNull(userId, "userId không được trống");
        this.startedAt  = LocalDateTime.now();
        this.status     = Status.PICKING;
    }

    /** PICKING -> COMPLETED: yêu cầu mọi dòng đã được xác nhận. */
    public void complete() {
        if (this.status != Status.PICKING) {
            throw new IllegalStateException("Chỉ lệnh đang PICKING mới được hoàn tất");
        }
        boolean allConfirmed = details.stream().allMatch(PickingListDetail::isConfirmed);
        if (!allConfirmed) {
            throw new IllegalStateException("Còn dòng chưa được đối soát/xác nhận");
        }
        this.status      = Status.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() { return this.status == Status.COMPLETED; }

    public String getId()              { return id; }
    public String getSoId()            { return soId; }
    public String getAssignedTo()      { return assignedTo; }
    public Status getStatus()          { return status; }
    public LocalDateTime getStartedAt(){ return startedAt; }
    public LocalDateTime getCompletedAt(){ return completedAt; }
    public List<PickingListDetail> getDetails() { return Collections.unmodifiableList(details); }
}
