package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SalesOrderDetailEntity {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "so_id", nullable = false)
    private SalesOrderEntity salesOrder;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    /** [Bán theo NCC] NCC được chọn cho dòng (nullable = bất kỳ). */
    @Column(name = "supplier_id", length = 20)
    private String supplierId;

    @Column(name = "quantity_ordered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "quantity_picked", nullable = false)
    private Integer quantityPicked;

    /** [Bán theo NCC] số đã giữ chỗ được (0 nếu backorder toàn phần). */
    @Column(name = "quantity_allocated", nullable = false)
    private Integer quantityAllocated;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent;
}
