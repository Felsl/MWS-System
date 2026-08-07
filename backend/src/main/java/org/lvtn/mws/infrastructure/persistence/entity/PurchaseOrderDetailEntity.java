package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderDetailEntity {

    @Id
    @Column(name = "id", length = 20, nullable = false)
    private String id;

    @Column(name = "po_id", length = 20, nullable = false)
    private String poId;

    @Column(name = "product_id", length = 20, nullable = false)
    private String productId;

    @Column(name = "quantity_ordered", nullable = false)
    private int quantityOrdered;

    @Column(name = "quantity_received", nullable = false)
    private int quantityReceived;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // [lat6] NCC theo từng dòng (đơn mua nhiều NCC); nullable, tương thích ngược.
    @Column(name = "supplier_id", length = 20)
    private String supplierId;
}
