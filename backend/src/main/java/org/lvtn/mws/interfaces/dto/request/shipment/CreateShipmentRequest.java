package org.lvtn.mws.interfaces.dto.request.shipment;

import jakarta.validation.constraints.NotNull;

/** Tạo vận đơn (PACKED) cho đơn bán hàng đã gom xong. */
public record CreateShipmentRequest(
        @NotNull(message = "salesOrderId không được rỗng") String salesOrderId,
        String carrierId) {
}
