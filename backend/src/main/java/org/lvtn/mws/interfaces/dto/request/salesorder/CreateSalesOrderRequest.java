package org.lvtn.mws.interfaces.dto.request.salesorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Payload tạo đơn bán hàng (DRAFT). */
public record CreateSalesOrderRequest(
        @NotNull(message = "warehouseId không được rỗng") String warehouseId,
        @NotNull(message = "customerId không được rỗng") String customerId,
        BigDecimal discountAmount,
        int priority,
        LocalDate requiredDate,
        @NotNull(message = "createdBy không được rỗng") String createdBy,
        @NotEmpty(message = "Đơn hàng phải có ít nhất 1 dòng") @Valid List<SalesOrderLineRequest> lines) {
}
