package org.lvtn.mws.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * Nightly cron: auto-expire batches whose expiry_date < today.
 * Runs at 00:00 every day.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryBatchScheduler {

    private final IInventoryBatchRepository batchRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireOutdatedBatches() {
        LocalDate today = LocalDate.now();
        List<InventoryBatch> expiredBatches = batchRepository.findExpiredActiveBatches(today);

        if (expiredBatches.isEmpty()) {
            log.info("[Scheduler] Không có lô hàng nào cần đánh dấu EXPIRED.");
            return;
        }

        expiredBatches.forEach(InventoryBatch::markExpired);
        batchRepository.saveAll(expiredBatches);
        log.info("[Scheduler] Đã cập nhật {} lô hàng sang trạng thái EXPIRED.", expiredBatches.size());
    }
}
