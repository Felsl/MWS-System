package org.lvtn.mws.application.usecases.shipment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.event.ShipmentShippedEvent;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.service.ShipmentDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Xác nhận xuất kho thành công (PACKED/HANDED_OVER -> SHIPPING).
 * Phát ShipmentShippedEvent để listener khấu trừ tồn kho + ghi thẻ kho + nâng SO -> SHIPPED,
 * tất cả trong cùng @Transactional này (listener đồng bộ) -> lỗi là rollback sạch.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ShipShipmentUseCase {

    private final ShipmentDomainService shipmentDomainService;
    private final ApplicationEventPublisher eventPublisher;

    public Shipment execute(String shipmentId, String actorUserId, String warehouseId) {
        Shipment shipment = shipmentDomainService.ship(shipmentId);
        if (shipment.isSalesShipment()) {
            eventPublisher.publishEvent(new ShipmentShippedEvent(
                    shipment.getId(),
                    shipment.getSalesOrderId(),
                    warehouseId,
                    actorUserId));
        }
        return shipment;
    }
}
