package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transfer_order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferOrderDetailEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "transfer_order_id", nullable = false, length = 20)
    private String transferOrderId;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    @Column(name = "batch_id", length = 20)
    private String batchId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "quantity_received")
    private Integer quantityReceived;

    // LƯU Ý: DB gốc đặt NOT NULL, nhưng FEFO điền sau bước tạo DRAFT.
    // Khuyến nghị ALTER cột này về NULL (xem README).
    @Column(name = "from_bin_location_id", length = 20)
    private String fromBinLocationId;

    @Column(name = "bin_location_id", length = 20)
    private String binLocationId;
}
