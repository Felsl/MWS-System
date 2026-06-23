package org.lvtn.mws.interfaces.dto.response.stocktake;

import java.time.LocalDateTime;

public record StocktakeSessionResponse(
        String id,
        String warehouseId,
        String status,
        LocalDateTime freezeStartedAt,
        LocalDateTime freezeEndedAt,
        String createdBy,
        LocalDateTime createdAt) {
}
