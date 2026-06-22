package org.lvtn.mws.infrastructure.event;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.event.ShipmentShippedEvent;
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
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * COMMIT OUTBOUND TRANSACTION — khấu trừ song tầng + ghi thẻ kho khi vận đơn SHIPPING.
 *
 * Dùng @EventListener (đồng bộ) nên chạy trong CÙNG transaction của ShipShipmentUseCase:
 * bất kỳ exception nào ở đây cũng rollback cả việc đổi trạng thái vận đơn.
 *
 * GIẢ ĐỊNH API model InventoryBatch (module Giai đoạn 2):
 *   - int  getQuantity()
 *   - void deduct(int qty)        // trừ số lượng lô (kiểm tra ACTIVE, thiếu thì ném InsufficientStockException)
 * Khi quantity về 0, lô hết hàng tự nhiên (isAvailable()=false), không có status EMPTY.
 */
@Component
@RequiredArgsConstructor
public class StockMovementListener {

    private final IInventoryRepository inventoryRepository;
    private final IInventoryBatchRepository inventoryBatchRepository;
    private final IStockMovementRepository stockMovementRepository;
    private final IPickingListRepository pickingListRepository;
    private final ISalesOrderRepository salesOrderRepository;
    private final IIdGenerator idGenerator;

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

            // 3) Ghi thẻ kho phục vụ truy vết (Audit Trail)
            StockMovement movement = StockMovement.outboundForSalesOrder(
                    idGenerator.generate(),
                    detail.getProductId(),
                    event.warehouseId(),
                    detail.getActualBatchId(),
                    picked,
                    quantityBefore,
                    event.salesOrderId(),
                    event.actorUserId());
            stockMovementRepository.save(movement);
        }

        // 4) Nâng cấp trạng thái đơn gốc: SHIPPED
        SalesOrder so = salesOrderRepository.findById(event.salesOrderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy đơn bán hàng: " + event.salesOrderId()));
        so.markShipped();
        salesOrderRepository.save(so);
    }
}
