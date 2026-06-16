package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductCategoryEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
