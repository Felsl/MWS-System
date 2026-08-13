package org.lvtn.mws.interfaces.dto.response.salesorder;

import java.time.LocalDateTime;

/** [Bán vượt tồn] Một dòng nhu cầu nhập (backorder) cho màn nhu cầu ở Dashboard. */
public record StockDemandResponse(
        String id,
        String soId,
        String soNumber,
        String productId,
        String productName,
        String warehouseId,
        String warehouseName,
        String supplierId,
        String supplierName,
        int quantityShort,
        LocalDateTime createdAt) {
}
