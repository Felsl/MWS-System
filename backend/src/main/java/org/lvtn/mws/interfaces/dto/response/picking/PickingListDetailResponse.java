package org.lvtn.mws.interfaces.dto.response.picking;

import java.time.LocalDateTime;

public record PickingListDetailResponse(
        String id,
        String pickingListId,
        String productId,
        String batchId,
        String actualBatchId,
        String binLocationId,
        int quantityToPick,
        int quantityPicked,
        boolean confirmed,
        String confirmedBy,
        LocalDateTime confirmedAt) {
}
