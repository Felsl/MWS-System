package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "adjustment_voucher_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AdjustmentVoucherDetailEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private AdjustmentVoucherEntity voucher;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "batch_id", length = 20)
    private String batchId;

    @Column(name = "bin_location_id", length = 20, nullable = false)
    private String binLocationId;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "before_quantity", nullable = false)
    private int beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private int afterQuantity;

    @Column(name = "stocktake_detail_id", length = 20)
    private String stocktakeDetailId;
}
