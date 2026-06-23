package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Một dòng đối chiếu trong phiên kiểm kê: ảnh chụp tồn hệ thống của một lô (batch)
 * tại một vị trí (bin), và số lượng đếm thực tế do công nhân nhập.
 *
 * difference = countedQuantity - systemQuantity  (dương = thừa, âm = thiếu).
 */
public class StocktakeDetail {

    private final String id;
    private final String sessionId;
    private final String productId;
    private final String binLocationId;
    private final String batchId;
    private final int systemQuantity;
    private Integer countedQuantity;   // null khi chưa kiểm đếm
    private Integer difference;        // null khi chưa kiểm đếm
    private String adjustmentReason;
    private String countedBy;
    private LocalDateTime countedAt;
    private String approvedBy;
    private LocalDateTime approvedAt;

    private StocktakeDetail(Builder b) {
        this.id               = Objects.requireNonNull(b.id, "id is required");
        this.sessionId        = Objects.requireNonNull(b.sessionId, "sessionId is required");
        this.productId        = Objects.requireNonNull(b.productId, "productId is required");
        this.binLocationId    = Objects.requireNonNull(b.binLocationId, "binLocationId is required");
        this.batchId          = Objects.requireNonNull(b.batchId, "batchId is required");
        this.systemQuantity   = b.systemQuantity;
        this.countedQuantity  = b.countedQuantity;
        this.difference       = b.difference;
        this.adjustmentReason = b.adjustmentReason;
        this.countedBy        = b.countedBy;
        this.countedAt        = b.countedAt;
        this.approvedBy       = b.approvedBy;
        this.approvedAt       = b.approvedAt;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, sessionId, productId, binLocationId, batchId;
        private int systemQuantity;
        private Integer countedQuantity, difference;
        private String adjustmentReason, countedBy, approvedBy;
        private LocalDateTime countedAt, approvedAt;

        public Builder id(String v)                  { this.id = v; return this; }
        public Builder sessionId(String v)           { this.sessionId = v; return this; }
        public Builder productId(String v)           { this.productId = v; return this; }
        public Builder binLocationId(String v)       { this.binLocationId = v; return this; }
        public Builder batchId(String v)             { this.batchId = v; return this; }
        public Builder systemQuantity(int v)         { this.systemQuantity = v; return this; }
        public Builder countedQuantity(Integer v)    { this.countedQuantity = v; return this; }
        public Builder difference(Integer v)         { this.difference = v; return this; }
        public Builder adjustmentReason(String v)    { this.adjustmentReason = v; return this; }
        public Builder countedBy(String v)           { this.countedBy = v; return this; }
        public Builder countedAt(LocalDateTime v)    { this.countedAt = v; return this; }
        public Builder approvedBy(String v)          { this.approvedBy = v; return this; }
        public Builder approvedAt(LocalDateTime v)   { this.approvedAt = v; return this; }
        public StocktakeDetail build()               { return new StocktakeDetail(this); }
    }

    /** Công nhân nhập số đếm thực tế; tự tính chênh lệch. */
    public void submitCount(int counted, String countedBy) {
        if (counted < 0) throw new IllegalArgumentException("Số đếm thực tế không được âm");
        this.countedQuantity = counted;
        this.difference = counted - this.systemQuantity;
        this.countedBy = countedBy;
        this.countedAt = LocalDateTime.now();
    }

    /** Duyệt từng dòng (line-item approval). */
    public void approveLine(String approvedBy, String reason) {
        if (this.countedQuantity == null) {
            throw new IllegalStateException("Chưa kiểm đếm dòng này, không thể duyệt");
        }
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        if (reason != null && !reason.isBlank()) {
            this.adjustmentReason = reason;
        }
    }

    public boolean isCounted()     { return countedQuantity != null; }
    public boolean hasDifference() { return difference != null && difference != 0; }

    public String getId()                  { return id; }
    public String getSessionId()           { return sessionId; }
    public String getProductId()           { return productId; }
    public String getBinLocationId()       { return binLocationId; }
    public String getBatchId()             { return batchId; }
    public int getSystemQuantity()         { return systemQuantity; }
    public Integer getCountedQuantity()    { return countedQuantity; }
    public Integer getDifference()         { return difference; }
    public String getAdjustmentReason()    { return adjustmentReason; }
    public String getCountedBy()           { return countedBy; }
    public LocalDateTime getCountedAt()    { return countedAt; }
    public String getApprovedBy()          { return approvedBy; }
    public LocalDateTime getApprovedAt()   { return approvedAt; }
}
