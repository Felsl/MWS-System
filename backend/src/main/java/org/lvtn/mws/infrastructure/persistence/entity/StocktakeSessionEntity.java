package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocktake_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StocktakeSessionEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "warehouse_id", length = 20, nullable = false)
    private String warehouseId;

    @Column(name = "status", length = 30, nullable = false)
    private String status;

    @Column(name = "freeze_started_at")
    private LocalDateTime freezeStartedAt;

    @Column(name = "freeze_ended_at")
    private LocalDateTime freezeEndedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
