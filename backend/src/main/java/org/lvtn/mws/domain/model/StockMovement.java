package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bản ghi thẻ kho (audit trail) cho một biến động tồn — đối tượng domain thuần.
 *
 * [GIAI ĐOẠN 6 — ĐÃ SỬA] Cấu trúc khớp đúng bảng stock_movements:
 *   quantityChange (CÓ DẤU: + tăng / - giảm), quantityBefore, quantityAfter, note, createdBy.
 *   Bỏ trường binLocationId (DDL stock_movements không có cột này).
 *   referenceType là String (khớp VARCHAR), không còn enum.
 *   MovementType bổ sung ADJUST_IN / ADJUST_OUT cho nghiệp vụ điều chỉnh.
 */
public class StockMovement {

    public enum MovementType { IN, OUT, ADJUST, ADJUST_IN, ADJUST_OUT }

    private final String id;
    private final String productId;
    private final String warehouseId;
    private final String batchId;        // nullable
    private final MovementType movementType;
    private final int quantityChange;    // có dấu
    private final int quantityBefore;
    private final int quantityAfter;
    private final String referenceType;  // 'GOODS_RECEIPT', 'SALES_ORDER', 'TRANSFER_ORDER', 'ADJUSTMENT', 'PICKING'...
    private final String referenceId;
    private final String note;
    private final String createdBy;
    private final LocalDateTime createdAt;

    private StockMovement(Builder b) {
        this.id             = Objects.requireNonNull(b.id, "StockMovement id is required");
        this.productId      = Objects.requireNonNull(b.productId, "productId is required");
        this.warehouseId    = Objects.requireNonNull(b.warehouseId, "warehouseId is required");
        this.batchId        = b.batchId;
        this.movementType   = Objects.requireNonNull(b.movementType, "movementType is required");
        this.quantityChange = b.quantityChange;
        this.quantityBefore = b.quantityBefore;
        this.quantityAfter  = b.quantityAfter;
        this.referenceType  = b.referenceType;
        this.referenceId    = b.referenceId;
        this.note           = b.note;
        this.createdBy      = Objects.requireNonNull(b.createdBy, "createdBy is required");
        this.createdAt      = b.createdAt != null ? b.createdAt : LocalDateTime.now();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, productId, warehouseId, batchId, referenceType, referenceId, note, createdBy;
        private MovementType movementType;
        private int quantityChange, quantityBefore, quantityAfter;
        private LocalDateTime createdAt;

        public Builder id(String v)                 { this.id = v; return this; }
        public Builder productId(String v)          { this.productId = v; return this; }
        public Builder warehouseId(String v)        { this.warehouseId = v; return this; }
        public Builder batchId(String v)            { this.batchId = v; return this; }
        public Builder movementType(MovementType v) { this.movementType = v; return this; }
        public Builder quantityChange(int v)        { this.quantityChange = v; return this; }
        public Builder quantityBefore(int v)        { this.quantityBefore = v; return this; }
        public Builder quantityAfter(int v)         { this.quantityAfter = v; return this; }
        public Builder referenceType(String v)      { this.referenceType = v; return this; }
        public Builder referenceId(String v)        { this.referenceId = v; return this; }
        public Builder note(String v)               { this.note = v; return this; }
        public Builder createdBy(String v)          { this.createdBy = v; return this; }
        public Builder createdAt(LocalDateTime v)   { this.createdAt = v; return this; }
        public StockMovement build()                { return new StockMovement(this); }
    }

    /**
     * Factory tiện ích cho thẻ kho ADJUST khi báo thiếu trong lúc gom hàng (short-pick).
     * Ghi nhận phần lệch -shortfall trên lô; chưa khấu trừ tồn vật lý ở bước này.
     */
    public static StockMovement adjustmentForShortPick(String id, String productId, String warehouseId,
                                                       String batchId, int shortfall, int currentBatchQuantity,
                                                       String referenceId, String note, String createdBy) {
        return builder()
                .id(id)
                .productId(productId)
                .warehouseId(warehouseId)
                .batchId(batchId)
                .movementType(MovementType.ADJUST)
                .quantityChange(-shortfall)
                .quantityBefore(currentBatchQuantity)
                .quantityAfter(currentBatchQuantity - shortfall)
                .referenceType("PICKING")
                .referenceId(referenceId)
                .note(note)
                .createdBy(createdBy)
                .build();
    }

    /**
     * Factory cho thẻ kho XUẤT theo đơn bán hàng (khi vận đơn SHIPPING).
     * quantityChange = -picked; before/after theo tồn tổng tại thời điểm khấu trừ.
     */
    public static StockMovement outboundForSalesOrder(String id, String productId, String warehouseId,
                                                      String batchId, int picked, int quantityBefore,
                                                      String referenceId, String createdBy) {
        return builder()
                .id(id)
                .productId(productId)
                .warehouseId(warehouseId)
                .batchId(batchId)
                .movementType(MovementType.OUT)
                .quantityChange(-picked)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityBefore - picked)
                .referenceType("SALES_ORDER")
                .referenceId(referenceId)
                .note("Xuất kho theo đơn " + referenceId)
                .createdBy(createdBy)
                .build();
    }

    public String getId()                 { return id; }
    public String getProductId()          { return productId; }
    public String getWarehouseId()        { return warehouseId; }
    public String getBatchId()            { return batchId; }
    public MovementType getMovementType() { return movementType; }
    public int getQuantityChange()        { return quantityChange; }
    public int getQuantityBefore()        { return quantityBefore; }
    public int getQuantityAfter()         { return quantityAfter; }
    public String getReferenceType()      { return referenceType; }
    public String getReferenceId()        { return referenceId; }
    public String getNote()               { return note; }
    public String getCreatedBy()          { return createdBy; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
}
