package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.notification.CreateNotificationUseCase;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.service.SalesOrderDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * [Bán vượt tồn] Khi HÀNG VỀ cho (sản phẩm, kho): bù các nhu cầu backorder OPEN theo FIFO,
 * rồi báo người tạo đơn "đã có hàng". Chạy trong transaction MỚI (REQUIRES_NEW) vì được gọi
 * từ listener AFTER_COMMIT của nghiệp vụ nhập hàng.
 */
@Service
@RequiredArgsConstructor
public class FulfillBackordersUseCase {

    private final SalesOrderDomainService salesOrderDomainService;
    private final CreateNotificationUseCase createNotificationUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(String productId, String warehouseId) {
        List<String> fulfilledSoIds = salesOrderDomainService.fulfillDemands(productId, warehouseId);
        for (String soId : fulfilledSoIds) {
            SalesOrder so = salesOrderDomainService.findById(soId);
            String label = so.getSoNumber() == null ? soId : so.getSoNumber();
            String title = "Đã có hàng cho đơn bán";
            String message = so.getStatus() == SalesOrder.Status.ALLOCATED
                    ? String.format("Đơn %s đã đủ hàng (được bù từ lô mới về) — sẵn sàng nhặt.", label)
                    : String.format("Đơn %s vừa được bù thêm hàng từ lô mới về.", label);
            if (so.getCreatedBy() != null) {
                createNotificationUseCase.createForUsers(
                        List.of(so.getCreatedBy()), title, message,
                        Notification.Type.INFO, "SALES_ORDER", soId);
            }
        }
    }
}
