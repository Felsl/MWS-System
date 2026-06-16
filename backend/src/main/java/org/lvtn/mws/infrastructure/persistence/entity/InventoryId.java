package org.lvtn.mws.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InventoryId implements Serializable {

    @Column(name = "product_id", length = 20)
    private String productId;

    @Column(name = "warehouse_id", length = 20)
    private String warehouseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryId)) return false;
        InventoryId that = (InventoryId) o;
        return Objects.equals(productId, that.productId) &&
               Objects.equals(warehouseId, that.warehouseId);
    }

    @Override
    public int hashCode() { return Objects.hash(productId, warehouseId); }
}
