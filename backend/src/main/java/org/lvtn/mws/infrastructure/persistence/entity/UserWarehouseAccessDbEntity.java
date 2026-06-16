package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_warehouse_access")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserWarehouseAccessDbEntity {

    @EmbeddedId
    private UserWarehouseId id;

    @Embeddable
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @EqualsAndHashCode
    public static class UserWarehouseId implements java.io.Serializable {
        @Column(name = "user_id", length = 20)
        private String userId;

        @Column(name = "warehouse_id", length = 20)
        private String warehouseId;
    }
}
