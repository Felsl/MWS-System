package org.lvtn.mws.interfaces.dto.response.salesorder;

import java.math.BigDecimal;

public record SalesOrderDetailResponse(
        String id,
        String soId,
        String productId,
        String supplierId,
        int quantityOrdered,
        int quantityPicked,
        int quantityAllocated,
        BigDecimal unitPrice,
        BigDecimal discountPercent) {
}
