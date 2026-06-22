package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "picking_list_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PickingListDetailEntity {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_list_id", nullable = false)
    private PickingListEntity pickingList;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    @Column(name = "batch_id", nullable = false, length = 20)
    private String batchId;

    @Column(name = "actual_batch_id", length = 20)
    private String actualBatchId;

    @Column(name = "bin_location_id", nullable = false, length = 20)
    private String binLocationId;

    @Column(name = "quantity_to_pick", nullable = false)
    private Integer quantityToPick;

    @Column(name = "quantity_picked", nullable = false)
    private Integer quantityPicked;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed;

    @Column(name = "confirmed_by", length = 50)
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
}
