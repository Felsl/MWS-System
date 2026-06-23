package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Phiếu điều chỉnh tồn kho (aggregate root: header + danh sách dòng).
 *
 * Vòng đời: DRAFT -> APPROVED (hoặc REJECTED).
 * Phiếu được sinh tự động (DRAFT) khi hoàn tất phiên kiểm kê; việc áp tồn kho
 * chỉ xảy ra khi APPROVED.
 */
public class AdjustmentVoucher {

    public enum Status { DRAFT, APPROVED, REJECTED }

    private final String id;
    private final String voucherNumber;
    private final String warehouseId;
    private final String sessionId;      // có thể null (điều chỉnh thủ công)
    private final String reason;
    private Status status;
    private final String createdBy;
    private String approvedBy;
    private final LocalDateTime createdAt;
    private final List<AdjustmentVoucherDetail> details = new ArrayList<>();

    private AdjustmentVoucher(Builder b) {
        this.id            = Objects.requireNonNull(b.id, "id is required");
        this.voucherNumber = Objects.requireNonNull(b.voucherNumber, "voucherNumber is required");
        this.warehouseId   = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.sessionId     = b.sessionId;
        this.reason        = Objects.requireNonNull(b.reason, "reason is required");
        this.status        = b.status != null ? b.status : Status.DRAFT;
        this.createdBy     = b.createdBy;
        this.approvedBy    = b.approvedBy;
        this.createdAt     = b.createdAt != null ? b.createdAt : LocalDateTime.now();
        if (b.details != null) {
            for (AdjustmentVoucherDetail d : b.details) addDetail(d);
        }
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, voucherNumber, warehouseId, sessionId, reason, createdBy, approvedBy;
        private Status status;
        private LocalDateTime createdAt;
        private List<AdjustmentVoucherDetail> details;

        public Builder id(String v)            { this.id = v; return this; }
        public Builder voucherNumber(String v) { this.voucherNumber = v; return this; }
        public Builder warehouseId(String v)   { this.warehouseId = v; return this; }
        public Builder sessionId(String v)     { this.sessionId = v; return this; }
        public Builder reason(String v)        { this.reason = v; return this; }
        public Builder status(Status v)        { this.status = v; return this; }
        public Builder createdBy(String v)     { this.createdBy = v; return this; }
        public Builder approvedBy(String v)    { this.approvedBy = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder details(List<AdjustmentVoucherDetail> v) { this.details = v; return this; }
        public AdjustmentVoucher build()       { return new AdjustmentVoucher(this); }
    }

    public void addDetail(AdjustmentVoucherDetail detail) {
        AdjustmentVoucherDetail linked = AdjustmentVoucherDetail.builder()
                .id(detail.getId())
                .voucherId(this.id)
                .productId(detail.getProductId())
                .batchId(detail.getBatchId())
                .binLocationId(detail.getBinLocationId())
                .quantityChange(detail.getQuantityChange())
                .beforeQuantity(detail.getBeforeQuantity())
                .afterQuantity(detail.getAfterQuantity())
                .stocktakeDetailId(detail.getStocktakeDetailId())
                .build();
        this.details.add(linked);
    }

    /** Duyệt phiếu: DRAFT -> APPROVED. */
    public void approve(String approvedBy) {
        if (this.status != Status.DRAFT) {
            throw new IllegalStateException("Chỉ duyệt được phiếu đang ở trạng thái DRAFT");
        }
        this.status = Status.APPROVED;
        this.approvedBy = approvedBy;
    }

    /** Mức chênh lệch lớn nhất (theo %) trong toàn phiếu — dùng cho phân tầng duyệt. */
    public double maxVariancePercent() {
        return details.stream().mapToDouble(AdjustmentVoucherDetail::variancePercent).max().orElse(0.0);
    }

    public boolean isEmpty() { return details.isEmpty(); }

    public String getId()                              { return id; }
    public String getVoucherNumber()                   { return voucherNumber; }
    public String getWarehouseId()                     { return warehouseId; }
    public String getSessionId()                       { return sessionId; }
    public String getReason()                          { return reason; }
    public Status getStatus()                          { return status; }
    public String getCreatedBy()                       { return createdBy; }
    public String getApprovedBy()                      { return approvedBy; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public List<AdjustmentVoucherDetail> getDetails()  { return Collections.unmodifiableList(details); }
}
