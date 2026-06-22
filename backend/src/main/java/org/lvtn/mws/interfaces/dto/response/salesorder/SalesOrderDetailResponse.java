package org.lvtn.mws.interfaces.dto.response.salesorder;

import java.math.BigDecimal;

public record SalesOrderDetailResponse(
        String id,
        String soId,
        String productId,
        int quantityOrdered,
        int quantityPicked,
        BigDecimal unitPrice,
        BigDecimal discountPercent) {
}
