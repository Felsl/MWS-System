package org.lvtn.mws.interfaces.dto.response;

import java.time.LocalDateTime;

public record StockMovementResponse(
        String id,
        String productId,
        String warehouseId,
        String batchId,
        String movementType,
        int quantityChange,
        int quantityBefore,
        int quantityAfter,
        String referenceType,
        String referenceId,
        String note,
        String createdBy,
        LocalDateTime createdAt) {
}
