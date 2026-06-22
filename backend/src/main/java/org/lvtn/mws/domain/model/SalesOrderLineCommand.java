package org.lvtn.mws.domain.model;

import java.math.BigDecimal;

/** Lệnh tạo 1 dòng đơn bán hàng (value object thuần Java, dùng chung application<->domain). */
public record SalesOrderLineCommand(
        String productId,
        int quantityOrdered,
        BigDecimal unitPrice,
        BigDecimal discountPercent) {
}
