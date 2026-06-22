package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "goods_receipt_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GoodsReceiptDetailEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "grn_id", length = 20, nullable = false)
    private String grnId;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "po_detail_id", length = 20)
    private String poDetailId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "bin_location_id", length = 20, nullable = false)
    private String binLocationId;
}
