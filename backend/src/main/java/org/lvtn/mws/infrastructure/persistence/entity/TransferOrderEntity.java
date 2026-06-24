package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.TransferOrder;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "from_warehouse_id", nullable = false, length = 20)
    private String fromWarehouseId;

    @Column(name = "to_warehouse_id", nullable = false, length = 20)
    private String toWarehouseId;

    @Column(name = "transfer_number", nullable = false, unique = true, length = 50)
    private String transferNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransferOrder.Status status;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
