package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryEntity {

    @EmbeddedId
    private InventoryId id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    /**
     * Optimistic locking: Hibernate auto-increments on every UPDATE.
     * Prevents concurrent overselling.
     */
    @Version
    @Column(name = "version", nullable = false)
    private int version;
}
