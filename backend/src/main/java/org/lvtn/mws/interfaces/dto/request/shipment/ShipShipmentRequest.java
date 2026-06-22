package org.lvtn.mws.interfaces.dto.request.shipment;

import jakarta.validation.constraints.NotNull;

/**
 * Xuất hàng lên đường (PACKED -> SHIPPING). Kích hoạt khấu trừ tồn kho vật lý.
 * warehouseId = kho nguồn xuất hàng; actorUserId = người thực hiện (ghi created_by thẻ kho).
 */
public record ShipShipmentRequest(
        @NotNull(message = "warehouseId không được rỗng") String warehouseId,
        @NotNull(message = "actorUserId không được rỗng") String actorUserId) {
}
