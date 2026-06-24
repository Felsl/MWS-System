package org.lvtn.mws.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.application.event.StockMovementEvent;
import org.lvtn.mws.application.ports.INotificationRecipientResolver;
import org.lvtn.mws.application.usecases.notification.CreateNotificationUseCase;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * [GIAI ĐOẠN 7] Cảnh báo TỒN AN TOÀN — chạy SAU COMMIT của nghiệp vụ làm giảm tồn.
 *
 * Chỉ xét biến động GIẢM tồn thực (OUT / ADJUST_OUT / TRANSFER_OUT). Đọc tồn tổng ĐÃ commit
 * và so với products.safety_stock; nếu chạm/ dưới ngưỡng → cảnh báo.
 *
 * Người nhận = (INBOUND_CREATE_PO, toàn cục — bộ phận mua hàng)
 *            ∪ (NOTIF_WH_RECEIVE ∩ quyền truy cập kho đó — quản lý kho).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyStockAlertListener {

    private static final Set<StockMovement.MovementType> DECREASING = EnumSet.of(
            StockMovement.MovementType.OUT,
            StockMovement.MovementType.ADJUST_OUT,
            StockMovement.MovementType.TRANSFER_OUT);

    private static final String PERM_PURCHASER = "INBOUND_CREATE_PO";
    private static final String PERM_WH = "NOTIF_WH_RECEIVE";
    private static final String REFERENCE_TYPE = "PRODUCT";

    private final IInventoryRepository inventoryRepository;
    private final IProductRepository productRepository;
    private final INotificationRecipientResolver recipientResolver;
    private final CreateNotificationUseCase createNotificationUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockMovement(StockMovementEvent event) {
        StockMovement m = event.movement();
        if (m.getMovementType() == null || !DECREASING.contains(m.getMovementType())) return;

        try {
            int safetyStock = productRepository.findById(m.getProductId())
                    .map(Product::getSafetyStock).orElse(0);
            if (safetyStock <= 0) return;

            int onHand = inventoryRepository
                    .findByProductIdAndWarehouseId(m.getProductId(), m.getWarehouseId())
                    .map(inv -> inv.getQuantity()).orElse(0);
            if (onHand > safetyStock) return;

            String productName = productRepository.findById(m.getProductId())
                    .map(Product::getName).orElse(m.getProductId());

            Set<String> recipients = new LinkedHashSet<>();
            recipients.addAll(recipientResolver.resolveByPermission(PERM_PURCHASER));
            recipients.addAll(recipientResolver.resolveByPermissionAndWarehouse(PERM_WH, m.getWarehouseId()));
            if (recipients.isEmpty()) {
                log.info("[SafetyStock] Sản phẩm {} dưới ngưỡng nhưng không có người nhận.", m.getProductId());
                return;
            }

            String title = "Tồn dưới mức an toàn";
            String message = String.format(
                    "Sản phẩm '%s' tại kho %s còn %d (≤ tồn an toàn %d). Cân nhắc tạo PO bổ sung.",
                    productName, m.getWarehouseId(), onHand, safetyStock);

            createNotificationUseCase.createForUsers(
                    List.copyOf(recipients), title, message,
                    Notification.Type.WARNING, REFERENCE_TYPE, m.getProductId());
        } catch (Exception ex) {
            log.error("[SafetyStock] Lỗi xử lý cảnh báo tồn an toàn cho {}: {}",
                    m.getProductId(), ex.getMessage(), ex);
        }
    }
}
