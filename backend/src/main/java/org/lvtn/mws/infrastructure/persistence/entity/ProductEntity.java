package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lvtn.mws.domain.model.Product.Unit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "category_id", length = 20)
    private String categoryId;

    @Column(name = "sku", unique = true, nullable = false, length = 50)
    private String sku;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false, length = 20)
    private Unit unit;

    @Column(name = "safety_stock", nullable = false)
    private int safetyStock;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "volume", precision = 10, scale = 2)
    private BigDecimal volume;

    @Column(name = "hazardous_flag")
    private boolean hazardousFlag;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
