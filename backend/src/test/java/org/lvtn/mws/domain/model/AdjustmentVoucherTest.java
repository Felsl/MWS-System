package org.lvtn.mws.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdjustmentVoucherTest {

    private AdjustmentVoucher draft() {
        return AdjustmentVoucher.builder()
                .id("AV1").voucherNumber("ADJ-001").warehouseId("W1")
                .reason("Kiểm kê").status(AdjustmentVoucher.Status.DRAFT)
                .createdBy("u1")
                .build();
    }

    @Test
    void approveFromDraft() {
        AdjustmentVoucher v = draft();
        v.approve("boss");
        assertThat(v.getStatus()).isEqualTo(AdjustmentVoucher.Status.APPROVED);
        assertThat(v.getApprovedBy()).isEqualTo("boss");
    }

    @Test
    void approveNonDraftThrows() {
        AdjustmentVoucher v = draft();
        v.approve("boss");
        assertThatThrownBy(() -> v.approve("again")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addDetailLinksVoucherIdAndNotEmpty() {
        AdjustmentVoucher v = draft();
        assertThat(v.isEmpty()).isTrue();
        v.addDetail(AdjustmentVoucherDetail.builder()
                .id("DET1").productId("P1").binLocationId("BIN1").quantityChange(-2)
                .beforeQuantity(10).afterQuantity(8)
                .build());
        assertThat(v.isEmpty()).isFalse();
        assertThat(v.getDetails().get(0).getVoucherId()).isEqualTo("AV1");
    }
}
