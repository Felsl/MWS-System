package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * session_id để dạng cột String (không ánh xạ quan hệ) vì các dòng được nạp/cập nhật
 * độc lập theo từng vị trí; quản lý qua repository riêng (không cascade từ phiên).
 */
@Entity
@Table(name = "stocktake_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StocktakeDetailEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "session_id", length = 20, nullable = false)
    private String sessionId;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "bin_location_id", length = 20, nullable = false)
    private String binLocationId;

    @Column(name = "batch_id", length = 20, nullable = false)
    private String batchId;

    @Column(name = "system_quantity", nullable = false)
    private int systemQuantity;

    @Column(name = "counted_quantity")
    private Integer countedQuantity;

    @Column(name = "difference")
    private Integer difference;

    @Column(name = "adjustment_reason", length = 100)
    private String adjustmentReason;

    @Column(name = "counted_by", length = 20)
    private String countedBy;

    @Column(name = "counted_at")
    private LocalDateTime countedAt;

    @Column(name = "approved_by", length = 20)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
