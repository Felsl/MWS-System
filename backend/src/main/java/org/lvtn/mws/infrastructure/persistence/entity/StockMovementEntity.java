package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.StockMovement.MovementType;
import org.lvtn.mws.domain.model.StockMovement.ReferenceType;

import java.time.LocalDateTime;

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

    @Column(name = "bin_location_id", length = 20)
    private String binLocationId;

    @Column(name = "batch_id", length = 20)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 20, nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 30, nullable = false)
    private ReferenceType referenceType;

    @Column(name = "reference_id", length = 20)
    private String referenceId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
