package org.lvtn.mws.application.event;

/**
 * Phát ra khi vận đơn (Sales Order) chuyển sang SHIPPING.
 * Listener (infrastructure) sẽ hứng để khấu trừ tồn kho vật lý + ghi thẻ kho,
 * và nâng cấp sales_orders.status = SHIPPED — trong cùng transaction.
 */
public record ShipmentShippedEvent(
        String shipmentId,
        String salesOrderId,
        String warehouseId,
        String actorUserId) {
}
