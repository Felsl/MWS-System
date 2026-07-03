package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryBatchTest {

    private InventoryBatch batch(int qty, InventoryBatch.Status status) {
        return new InventoryBatch.Builder()
                .id("B1").productId("P1").warehouseId("W1").binLocationId("BIN1")
                .batchNumber("LOT-1").quantity(qty).status(status).version(0)
                .build();
    }

    @Test
    void deductReducesQuantity() {
        InventoryBatch b = batch(10, InventoryBatch.Status.ACTIVE);
        b.deduct(4);
        assertThat(b.getQuantity()).isEqualTo(6);
    }

    @Test
    void deductNonActiveThrows() {
        assertThatThrownBy(() -> batch(10, InventoryBatch.Status.HOLD).deduct(1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deductInsufficientThrows() {
        assertThatThrownBy(() -> batch(2, InventoryBatch.Status.ACTIVE).deduct(5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void isAvailableOnlyWhenActiveAndPositive() {
        assertThat(batch(1, InventoryBatch.Status.ACTIVE).isAvailable()).isTrue();
        assertThat(batch(0, InventoryBatch.Status.ACTIVE).isAvailable()).isFalse();
        assertThat(batch(5, InventoryBatch.Status.EXPIRED).isAvailable()).isFalse();
    }
}
