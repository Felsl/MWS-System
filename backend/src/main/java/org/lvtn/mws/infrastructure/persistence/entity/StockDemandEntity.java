package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Bảng stock_demands — nhu cầu nhập phát sinh khi bán vượt tồn (backorder). */
@Entity
@Table(name = "stock_demands")
@Getter
@Setter
public class StockDemandEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "so_id", nullable = false, length = 20)
    private String soId;

    @Column(name = "so_detail_id", nullable = false, length = 20)
    private String soDetailId;

    @Column(name = "product_id", nullable = false, length = 20)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "supplier_id", length = 20)
    private String supplierId;

    @Column(name = "quantity_short", nullable = false)
    private int quantityShort;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
