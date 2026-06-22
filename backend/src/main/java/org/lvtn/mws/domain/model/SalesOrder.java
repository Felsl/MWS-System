package org.lvtn.mws.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Đơn bán hàng (sales_orders) + danh sách dòng (sales_order_details).
 * Thuần Java, không phụ thuộc framework. Mọi chuyển trạng thái đều có guard nghiệp vụ.
 */
public class SalesOrder {

    public enum Status { DRAFT, ALLOCATED, PICKING, SHIPPED, CANCELLED }

    private final String id;
    private String soNumber;
    private final String warehouseId;
    private final String customerId;
    private BigDecimal discountAmount;
    private Status status;
    private int priority;
    private LocalDate requiredDate;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private final List<SalesOrderDetail> details = new ArrayList<>();

    private SalesOrder(Builder b) {
        this.id            = Objects.requireNonNull(b.id, "id is required");
        this.soNumber      = b.soNumber;
        this.warehouseId   = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.customerId    = Objects.requireNonNull(b.customerId, "customerId is required");
        this.discountAmount = b.discountAmount == null ? BigDecimal.ZERO : b.discountAmount;
        this.status        = b.status == null ? Status.DRAFT : b.status;
        this.priority      = b.priority;
        this.requiredDate  = b.requiredDate;
        this.createdBy     = Objects.requireNonNull(b.createdBy, "createdBy is required");
        this.createdAt     = b.createdAt == null ? LocalDateTime.now() : b.createdAt;
        this.updatedAt     = b.updatedAt == null ? this.createdAt : b.updatedAt;
        if (b.details != null) this.details.addAll(b.details);
    }

    public static class Builder {
        private String id;
        private String soNumber;
        private String warehouseId;
        private String customerId;
        private BigDecimal discountAmount;
        private Status status;
        private int priority;
        private LocalDate requiredDate;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<SalesOrderDetail> details;

        public Builder id(String v)               { this.id = v; return this; }
        public Builder soNumber(String v)          { this.soNumber = v; return this; }
        public Builder warehouseId(String v)       { this.warehouseId = v; return this; }
        public Builder customerId(String v)        { this.customerId = v; return this; }
        public Builder discountAmount(BigDecimal v){ this.discountAmount = v; return this; }
        public Builder status(Status v)            { this.status = v; return this; }
        public Builder priority(int v)             { this.priority = v; return this; }
        public Builder requiredDate(LocalDate v)   { this.requiredDate = v; return this; }
        public Builder createdBy(String v)         { this.createdBy = v; return this; }
        public Builder createdAt(LocalDateTime v)  { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v)  { this.updatedAt = v; return this; }
        public Builder details(List<SalesOrderDetail> v) { this.details = v; return this; }
        public SalesOrder build()                  { return new SalesOrder(this); }
    }

    // ── Aggregate behaviour ──────────────────────────────────────────────────

    public void addDetail(SalesOrderDetail detail) {
        if (this.status != Status.DRAFT) {
            throw new IllegalStateException("Chỉ được thêm dòng khi đơn ở trạng thái DRAFT");
        }
        this.details.add(Objects.requireNonNull(detail, "detail không được null"));
        touch();
    }

    /** DRAFT -> ALLOCATED. Việc gọi reserveStock do DomainService thực hiện trước/sau bước này. */
    public void allocate() {
        requireStatus(Status.DRAFT, "Chỉ đơn DRAFT mới được phân bổ (ALLOCATED)");
        if (details.isEmpty()) {
            throw new IllegalStateException("Đơn chưa có dòng hàng nào, không thể phân bổ");
        }
        this.status = Status.ALLOCATED;
        touch();
    }

    /** ALLOCATED -> PICKING (khi sinh xong picking list). */
    public void markPicking() {
        requireStatus(Status.ALLOCATED, "Chỉ đơn ALLOCATED mới chuyển sang PICKING");
        this.status = Status.PICKING;
        touch();
    }

    /** PICKING -> SHIPPED (khi xác nhận xuất kho thành công). */
    public void markShipped() {
        requireStatus(Status.PICKING, "Chỉ đơn PICKING mới chuyển sang SHIPPED");
        this.status = Status.SHIPPED;
        touch();
    }

    /** Hủy đơn — không cho hủy đơn đã SHIPPED. Việc release reserved do DomainService xử lý. */
    public void cancel() {
        if (this.status == Status.SHIPPED) {
            throw new IllegalStateException("Không thể hủy đơn đã xuất kho (SHIPPED)");
        }
        this.status = Status.CANCELLED;
        touch();
    }

    public boolean wasReserved() {
        return this.status == Status.ALLOCATED || this.status == Status.PICKING;
    }

    private void requireStatus(Status expected, String message) {
        if (this.status != expected) throw new IllegalStateException(message);
    }

    private void touch() { this.updatedAt = LocalDateTime.now(); }

    public void assignSoNumber(String soNumber) {
        if (this.soNumber == null) this.soNumber = soNumber;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getId()                 { return id; }
    public String getSoNumber()           { return soNumber; }
    public String getWarehouseId()        { return warehouseId; }
    public String getCustomerId()         { return customerId; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public Status getStatus()             { return status; }
    public int getPriority()              { return priority; }
    public LocalDate getRequiredDate()    { return requiredDate; }
    public String getCreatedBy()          { return createdBy; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
    public List<SalesOrderDetail> getDetails() { return Collections.unmodifiableList(details); }
}
