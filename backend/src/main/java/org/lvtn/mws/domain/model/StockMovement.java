package org.lvtn.mws.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Bản ghi thẻ kho (audit trail) cho một biến động tồn — đối tượng domain thuần.
 *
 * [GIAI ĐOẠN 6] Cấu trúc khớp bảng stock_movements: quantityChange (CÓ DẤU), quantityBefore,
 *   quantityAfter, note, createdBy; referenceType là String; MovementType có ADJUST_IN/ADJUST_OUT.
 *
 * [GIAI ĐOẠN 7] Bổ sung LẠI binLocationId để truy xuất chính xác ô kệ phát sinh biến động
 *   (phục vụ nhân công truy vết tại thời điểm di chuyển hàng). Yêu cầu migration:
 *   ALTER TABLE stock_movements ADD COLUMN bin_location_id VARCHAR(20) NULL ... (xem db/migration).
 */
public class StockMovement {

    public enum MovementType { IN, OUT, ADJUST, ADJUST_IN, ADJUST_OUT, TRANSFER_IN, TRANSFER_OUT }

    private final String id;
    private final String productId;
    private final String warehouseId;
    private final String batchId;        // nullable
    private final String binLocationId;  // [GĐ7] nullable — ô kệ phát sinh biến động
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
        this.binLocationId  = b.binLocationId;
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
        private String id, productId, warehouseId, batchId, binLocationId,
                referenceType, referenceId, note, createdBy;
        private MovementType movementType;
        private int quantityChange, quantityBefore, quantityAfter;
        private LocalDateTime createdAt;

        public Builder id(String v)                 { this.id = v; return this; }
        public Builder productId(String v)          { this.productId = v; return this; }
        public Builder warehouseId(String v)        { this.warehouseId = v; return this; }
        public Builder batchId(String v)            { this.batchId = v; return this; }
        public Builder binLocationId(String v)      { this.binLocationId = v; return this; }
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
     * Factory thẻ kho ADJUST khi báo thiếu lúc gom hàng (short-pick).
     * Ghi nhận phần lệch -shortfall trên lô; chưa khấu trừ tồn vật lý ở bước này.
     */
    public static StockMovement adjustmentForShortPick(String id, String productId, String warehouseId,
                                                       String batchId, String binLocationId,
                                                       int shortfall, int currentBatchQuantity,
                                                       String referenceId, String note, String createdBy) {
        return builder()
                .id(id)
                .productId(productId)
                .warehouseId(warehouseId)
                .batchId(batchId)
                .binLocationId(binLocationId)
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
     * Factory thẻ kho XUẤT theo đơn bán hàng (khi vận đơn SHIPPING).
     * quantityChange = -picked; before/after theo tồn tổng tại thời điểm khấu trừ.
     */
    public static StockMovement outboundForSalesOrder(String id, String productId, String warehouseId,
                                                      String batchId, String binLocationId,
                                                      int picked, int quantityBefore,
                                                      String referenceId, String createdBy) {
        return builder()
                .id(id)
                .productId(productId)
                .warehouseId(warehouseId)
                .batchId(batchId)
                .binLocationId(binLocationId)
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
    public String getBinLocationId()      { return binLocationId; }
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
