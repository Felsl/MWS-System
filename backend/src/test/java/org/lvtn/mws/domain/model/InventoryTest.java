package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit test thuần cho bất biến tồn kho (không Spring, không DB). */
class InventoryTest {

    private Inventory inv(int quantity, int reserved) {
        return new Inventory.Builder()
                .productId("P1").warehouseId("W1")
                .quantity(quantity).reservedQuantity(reserved).version(0)
                .build();
    }

    @Test @DisplayName("availableQuantity = quantity - reserved")
    void available() {
        assertThat(inv(10, 3).availableQuantity()).isEqualTo(7);
    }

    @Test @DisplayName("reserve giảm khả dụng, tăng reserved")
    void reserveOk() {
        Inventory i = inv(10, 0);
        i.reserve(4);
        assertThat(i.getReservedQuantity()).isEqualTo(4);
        assertThat(i.availableQuantity()).isEqualTo(6);
    }

    @Test @DisplayName("reserve vượt khả dụng -> InsufficientStockException (không oversell)")
    void reserveOversell() {
        Inventory i = inv(5, 3); // khả dụng = 2
        assertThatThrownBy(() -> i.reserve(3))
                .isInstanceOf(InsufficientStockException.class);
        assertThat(i.getReservedQuantity()).isEqualTo(3); // không đổi
    }

    @Test @DisplayName("reserve số lượng <= 0 -> IllegalArgumentException")
    void reserveNonPositive() {
        assertThatThrownBy(() -> inv(10, 0).reserve(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test @DisplayName("release giảm reserved; vượt -> IllegalStateException")
    void release() {
        Inventory i = inv(10, 5);
        i.release(2);
        assertThat(i.getReservedQuantity()).isEqualTo(3);
        assertThatThrownBy(() -> i.release(10)).isInstanceOf(IllegalStateException.class);
    }

    @Test @DisplayName("commitDeduction trừ cả quantity lẫn reserved")
    void commitOk() {
        Inventory i = inv(10, 4);
        i.commitDeduction(3);
        assertThat(i.getQuantity()).isEqualTo(7);
        assertThat(i.getReservedQuantity()).isEqualTo(1);
    }

    @Test @DisplayName("commitDeduction: quantity không đủ -> InsufficientStockException")
    void commitInsufficient() {
        assertThatThrownBy(() -> inv(2, 2).commitDeduction(3))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test @DisplayName("commitDeduction: reserved không đủ -> IllegalStateException")
    void commitReservedLow() {
        assertThatThrownBy(() -> inv(10, 1).commitDeduction(3))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test @DisplayName("decreaseQuantity không được xuống dưới phần reserved")
    void decreaseGuardsReserved() {
        Inventory i = inv(10, 8);
        assertThatThrownBy(() -> i.decreaseQuantity(5)).isInstanceOf(IllegalStateException.class);
        i.decreaseQuantity(2); // 10-2=8 == reserved: hợp lệ
        assertThat(i.getQuantity()).isEqualTo(8);
    }

    @Test @DisplayName("addStock / increaseQuantity yêu cầu qty > 0")
    void addGuards() {
        assertThatThrownBy(() -> inv(0, 0).addStock(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> inv(0, 0).increaseQuantity(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
