package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Máy trạng thái điều chuyển: DRAFT -> PENDING_APPROVAL -> APPROVED -> IN_TRANSIT -> COMPLETED. */
class TransferOrderTest {

    private TransferOrderDetail line(String id) {
        return new TransferOrderDetail.Builder()
                .id(id).transferOrderId("T1").productId("P1").quantity(5)
                .build();
    }

    private TransferOrder order(TransferOrder.Status status) {
        return new TransferOrder.Builder()
                .id("T1").fromWarehouseId("W1").toWarehouseId("W2").transferNumber("TR-001")
                .status(status).createdBy("u1").details(List.of(line("D1")))
                .build();
    }

    @Test
    void requestApprovalFromDraft() {
        TransferOrder t = order(TransferOrder.Status.DRAFT);
        t.requestApproval();
        assertThat(t.getStatus()).isEqualTo(TransferOrder.Status.PENDING_APPROVAL);
    }

    @Test
    void requestApprovalFromNonDraftThrows() {
        assertThatThrownBy(() -> order(TransferOrder.Status.APPROVED).requestApproval())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void approveReplacesDetailsAndSetsApprover() {
        TransferOrder t = order(TransferOrder.Status.PENDING_APPROVAL);
        t.approve("boss", List.of(line("D1"), line("D2")));
        assertThat(t.getStatus()).isEqualTo(TransferOrder.Status.APPROVED);
        assertThat(t.getApprovedBy()).isEqualTo("boss");
    }

    @Test
    void approveNullApproverThrows() {
        assertThatThrownBy(() -> order(TransferOrder.Status.PENDING_APPROVAL)
                .approve(null, List.of(line("D1"))))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void approveEmptyAllocationThrows() {
        assertThatThrownBy(() -> order(TransferOrder.Status.PENDING_APPROVAL)
                .approve("boss", List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void inTransitThenComplete() {
        TransferOrder t = order(TransferOrder.Status.APPROVED);
        t.markInTransit();
        assertThat(t.getStatus()).isEqualTo(TransferOrder.Status.IN_TRANSIT);
        t.complete();
        assertThat(t.getStatus()).isEqualTo(TransferOrder.Status.COMPLETED);
    }

    @Test
    void cannotCancelInTransit() {
        assertThatThrownBy(() -> order(TransferOrder.Status.IN_TRANSIT).cancel())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void receiveUnknownLineThrows() {
        assertThatThrownBy(() -> order(TransferOrder.Status.IN_TRANSIT)
                .receiveLine("NOPE", 1, "BIN1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
