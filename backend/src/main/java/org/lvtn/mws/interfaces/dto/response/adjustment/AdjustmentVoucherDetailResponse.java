package org.lvtn.mws.interfaces.dto.response.adjustment;

public record AdjustmentVoucherDetailResponse(
        String id,
        String voucherId,
        String productId,
        String batchId,
        String binLocationId,
        int quantityChange,
        int beforeQuantity,
        int afterQuantity,
        String stocktakeDetailId) {
}
