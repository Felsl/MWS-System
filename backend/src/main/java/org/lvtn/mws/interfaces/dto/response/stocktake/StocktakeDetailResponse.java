package org.lvtn.mws.interfaces.dto.response.stocktake;

import java.time.LocalDateTime;

public record StocktakeDetailResponse(
        String id,
        String sessionId,
        String productId,
        String binLocationId,
        String batchId,
        int systemQuantity,
        Integer countedQuantity,
        Integer difference,
        String adjustmentReason,
        String countedBy,
        LocalDateTime countedAt,
        String approvedBy,
        LocalDateTime approvedAt) {
}
