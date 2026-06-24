package org.lvtn.mws.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.application.ports.INotificationRecipientResolver;
import org.lvtn.mws.application.usecases.notification.CreateNotificationUseCase;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.model.Notification;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * [GIAI ĐOẠN 7] Cron quét lô SẮP hết hạn → cảnh báo người vận hành kho.
 *
 * Lịch mặc định 01:00 hằng ngày (cron cấu hình ở application.properties).
 * Ngưỡng cảnh báo: today <= expiry_date <= today + thresholdDays (mặc định 30), lô ACTIVE & còn hàng.
 *
 * Người nhận: user có quyền NOTIF_WH_RECEIVE VÀ có quyền truy cập đúng kho của lô.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryScanScheduler {

    private static final String PERMISSION = "NOTIF_WH_RECEIVE";
    private static final String REFERENCE_TYPE = "INVENTORY_BATCH";

    private final IInventoryBatchRepository batchRepository;
    private final IProductRepository productRepository;
    private final INotificationRecipientResolver recipientResolver;
    private final CreateNotificationUseCase createNotificationUseCase;

    @Value("${notification.expiry.threshold-days:30}")
    private int thresholdDays;

    @Scheduled(cron = "${notification.expiry.cron:0 0 1 * * ?}")
    @Transactional
    public void scanNearExpiryBatches() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(thresholdDays);

        List<InventoryBatch> batches = batchRepository.findNearExpiryActiveBatches(today, threshold);
        if (batches.isEmpty()) {
            log.info("[ExpiryScan] Không có lô nào sắp hết hạn trong {} ngày tới.", thresholdDays);
            return;
        }

        int alerted = 0;
        for (InventoryBatch batch : batches) {
            List<String> recipients = recipientResolver
                    .resolveByPermissionAndWarehouse(PERMISSION, batch.getWarehouseId());
            if (recipients.isEmpty()) continue;

            String productName = productRepository.findById(batch.getProductId())
                    .map(Product::getName).orElse(batch.getProductId());
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, batch.getExpiryDate());

            String title = "Lô hàng sắp hết hạn";
            String message = String.format(
                    "Sản phẩm '%s' (lô %s, ô %s) còn %d ngày là hết hạn (HSD %s, tồn %d).",
                    productName, batch.getBatchNumber(), batch.getBinLocationId(),
                    daysLeft, batch.getExpiryDate(), batch.getQuantity());

            createNotificationUseCase.createForUsers(
                    recipients, title, message, Notification.Type.ALERT, REFERENCE_TYPE, batch.getId());
            alerted++;
        }
        log.info("[ExpiryScan] Đã gửi cảnh báo cho {}/{} lô sắp hết hạn.", alerted, batches.size());
    }
}
