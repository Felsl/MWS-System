package org.lvtn.mws.interfaces.dto.request.salesorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Một dòng sản phẩm trong yêu cầu tạo đơn bán hàng. */
public record SalesOrderLineRequest(
        @NotNull(message = "productId không được rỗng") String productId,
        @Min(value = 1, message = "Số lượng đặt phải >= 1") int quantityOrdered,
        @NotNull(message = "unitPrice không được rỗng") BigDecimal unitPrice,
        BigDecimal discountPercent) {
}
