package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.StockMovement.MovementType;

import java.time.LocalDateTime;

/**
 * [GIAI ĐOẠN 6 — ĐÃ SỬA] Khớp đúng bảng stock_movements:
 * quantity_change / quantity_before / quantity_after / note / created_by.
 * Bỏ cột bin_location_id (DDL không có). reference_type lưu dạng VARCHAR (String).
 */
@Entity
@Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockMovementEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "warehouse_id", length = 20, nullable = false)
    private String warehouseId;

    @Column(name = "batch_id", length = 20)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 30, nullable = false)
    private MovementType movementType;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "quantity_before", nullable = false)
    private int quantityBefore;

    @Column(name = "quantity_after", nullable = false)
    private int quantityAfter;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id", length = 20)
    private String referenceId;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
