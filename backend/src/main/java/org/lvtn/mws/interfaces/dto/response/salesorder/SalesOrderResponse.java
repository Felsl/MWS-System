package org.lvtn.mws.interfaces.dto.response.salesorder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderResponse(
        String id,
        String soNumber,
        String warehouseId,
        String customerId,
        BigDecimal discountAmount,
        String status,
        int priority,
        LocalDate requiredDate,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<SalesOrderDetailResponse> details) {
}
