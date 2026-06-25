package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.InventoryBatch.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_batches",
       uniqueConstraints = @UniqueConstraint(
               name = "uq_product_batch_location",
               columnNames = {"product_id", "warehouse_id", "bin_location_id", "batch_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryBatchEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "bin_location_id", nullable = false, length = 20)
    private String binLocationId;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "manufactured_date")
    private LocalDate manufacturedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Optimistic locking: Hibernate auto-increments on every UPDATE.
     * Bảo vệ batch khỏi tranh chấp khi putaway (nhập), picking (xuất) và
     * adjustment (kiểm kê) cùng cộng/trừ một lô tại cùng một ô kệ.
     */
    @Version
    @Column(name = "version", nullable = false)
    private int version;
}
