package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SalesOrderEntity {

    @Id
    @Column(length = 20)
    private String id;

    @Column(name = "so_number", nullable = false, unique = true, length = 50)
    private String soNumber;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SalesOrderDetailEntity> details = new ArrayList<>();
}
