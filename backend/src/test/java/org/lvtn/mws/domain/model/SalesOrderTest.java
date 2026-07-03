package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Máy trạng thái đơn xuất: DRAFT -> ALLOCATED -> PICKING -> SHIPPED (+ CANCELLED). */
class SalesOrderTest {

    private SalesOrderDetail line() {
        return new SalesOrderDetail.Builder()
                .id("D1").soId("SO1").productId("P1").quantityOrdered(5)
                .build();
    }

    private SalesOrder order(SalesOrder.Status status, boolean withLine) {
        SalesOrder.Builder b = new SalesOrder.Builder()
                .id("SO1").soNumber("SO-001").warehouseId("W1").customerId("C1")
                .status(status).createdBy("u1");
        if (withLine) b.details(java.util.List.of(line())); // dựng qua builder, bỏ qua guard addDetail chỉ-DRAFT
        return b.build();
    }

    @Test
    void allocateFromDraftWithLines() {
        SalesOrder so = order(SalesOrder.Status.DRAFT, true);
        so.allocate();
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.ALLOCATED);
    }

    @Test
    void allocateWithoutLinesThrows() {
        assertThatThrownBy(() -> order(SalesOrder.Status.DRAFT, false).allocate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allocateFromNonDraftThrows() {
        assertThatThrownBy(() -> order(SalesOrder.Status.ALLOCATED, true).allocate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pickingThenShipped() {
        SalesOrder so = order(SalesOrder.Status.ALLOCATED, true);
        so.markPicking();
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.PICKING);
        so.markShipped();
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.SHIPPED);
    }

    @Test
    void cannotCancelShipped() {
        assertThatThrownBy(() -> order(SalesOrder.Status.SHIPPED, true).cancel())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelAllocatedOk() {
        SalesOrder so = order(SalesOrder.Status.ALLOCATED, true);
        so.cancel();
        assertThat(so.getStatus()).isEqualTo(SalesOrder.Status.CANCELLED);
    }

    @Test
    void wasReservedForAllocatedAndPicking() {
        assertThat(order(SalesOrder.Status.ALLOCATED, true).wasReserved()).isTrue();
        assertThat(order(SalesOrder.Status.PICKING, true).wasReserved()).isTrue();
        assertThat(order(SalesOrder.Status.DRAFT, true).wasReserved()).isFalse();
    }
}
