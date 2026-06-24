package org.lvtn.mws.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.event.ShipmentShippedEvent;
import org.lvtn.mws.application.event.StockMovementEvent;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.model.PickingListDetail;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * COMMIT OUTBOUND TRANSACTION — khấu trừ tồn song tầng khi vận đơn SHIPPING.
 *
 * Dùng @EventListener (ĐỒNG BỘ) nên chạy trong CÙNG transaction của ShipShipmentUseCase:
 * bất kỳ exception nào ở khâu KHẤU TRỪ đều rollback cả việc đổi trạng thái vận đơn.
 *
 * [GIAI ĐOẠN 7] Việc GHI THẺ KHO không còn thực hiện đồng bộ tại đây nữa. Thay vào đó, sau khi
 * khấu trừ xong (trong transaction), phát {@link StockMovementEvent} mang bản ghi đã dựng sẵn;
 * StockMovementAuditListener (AFTER_COMMIT) sẽ ghi thẻ kho khi transaction commit 100%
 * và SafetyStockAlertListener kiểm tra cảnh báo tồn an toàn.
 */
@Component
@RequiredArgsConstructor
public class StockMovementListener {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository inventoryBatchRepository;
    private final IPickingListRepository pickingListRepository;
    private final ISalesOrderRepository salesOrderRepository;
    private final IIdGenerator idGenerator;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onShipmentShipped(ShipmentShippedEvent event) {
        PickingList pickingList = pickingListRepository.findBySoId(event.salesOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy lệnh gom hàng cho đơn " + event.salesOrderId()));

        for (PickingListDetail detail : pickingList.getDetails()) {
            if (!detail.isConfirmed()) continue;
            int picked = detail.getQuantityPicked();

            // 1) Khấu trừ tồn kho tổng (quantity & reserved_quantity), @Version chống xung đột
            Inventory inv = inventoryRepository
                    .findByProductIdAndWarehouseId(detail.getProductId(), event.warehouseId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy tồn kho tổng cho sản phẩm " + detail.getProductId()));
            int quantityBefore = inv.getQuantity();
            inv.commitDeduction(picked);
            inventoryRepository.save(inv);

            // 2) Khấu trừ tồn theo lô vật lý dựa trên actual_batch_id
            InventoryBatch batch = inventoryBatchRepository.findById(detail.getActualBatchId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Không tìm thấy lô vật lý: " + detail.getActualBatchId()));
            batch.deduct(picked); // giảm tồn lô; hết hàng -> isAvailable()=false, FEFO sau bỏ qua
            inventoryBatchRepository.save(batch);

            // 3) Dựng thẻ kho và phát sự kiện (ghi thật ở AFTER_COMMIT). Kèm binLocationId của lô.
            StockMovement movement = StockMovement.outboundForSalesOrder(
                    idGenerator.generate(),
                    detail.getProductId(),
                    event.warehouseId(),
                    detail.getActualBatchId(),
                    batch.getBinLocationId(),
                    picked,
                    quantityBefore,
                    event.salesOrderId(),
                    event.actorUserId());
            eventPublisher.publishEvent(new StockMovementEvent(movement));
        }

        // 4) Nâng cấp trạng thái đơn gốc: SHIPPED
        SalesOrder so = salesOrderRepository.findById(event.salesOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy đơn bán hàng: " + event.salesOrderId()));
        so.markShipped();
        salesOrderRepository.save(so);
    }
}
