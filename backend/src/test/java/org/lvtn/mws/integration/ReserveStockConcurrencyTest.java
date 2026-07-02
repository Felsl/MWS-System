package org.lvtn.mws.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.application.usecases.inventory.ReserveStockUseCase;
import org.lvtn.mws.domain.model.Inventory;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A3 — Chứng minh hệ thống KHÔNG bán lố (oversell) khi nhiều yêu cầu giữ chỗ
 * chạy song song, nhờ optimistic locking ({@code @Version}) trên bảng inventory.
 *
 * <p>Bất biến cốt lõi được kiểm chứng: dù bao nhiêu luồng cùng giành hàng,
 * tổng số lượng giữ chỗ (reserved) KHÔNG BAO GIỜ vượt quá tồn kho vật lý.
 */
class ReserveStockConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private IInventoryRepository inventoryRepository;

    @Test
    @DisplayName("2 luồng giành đơn vị cuối cùng → đúng 1 thành công, 1 thất bại, reserved = 1")
    void twoThreadsRaceForLastUnit_onlyOneWins() throws InterruptedException {
        // Arrange: tồn kho chỉ có ĐÚNG 1 đơn vị khả dụng.
        String productId = sid("P");
        String warehouseId = sid("W");
        seedInventory(productId, warehouseId, 1);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        // Act: 2 luồng cùng cố giữ chỗ 1 đơn vị, canh nổ súng cùng lúc bằng latch.
        int threads = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    reserveStockUseCase.execute(productId, warehouseId, 1);
                    success.incrementAndGet();
                } catch (Exception ex) {
                    failure.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }
        startGate.countDown();                       // bắn cả 2 luồng cùng lúc
        doneGate.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        // Assert: đúng 1 thắng, 1 thua — và tuyệt đối không bán lố.
        assertThat(success.get()).as("số luồng giữ chỗ thành công").isEqualTo(1);
        assertThat(failure.get()).as("số luồng bị từ chối").isEqualTo(1);

        Inventory finalInv = reload(productId, warehouseId);
        assertThat(finalInv.getReservedQuantity())
                .as("reserved KHÔNG được vượt quá tồn vật lý (chống oversell)")
                .isEqualTo(1);
        assertThat(finalInv.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("10 luồng giành 5 đơn vị → đúng 5 thành công, reserved = 5")
    void tenThreadsRaceForFiveUnits_exactlyFiveWin() throws InterruptedException {
        String productId = sid("P");
        String warehouseId = sid("W");
        seedInventory(productId, warehouseId, 5);

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    reserveStockUseCase.execute(productId, warehouseId, 1);
                    success.incrementAndGet();
                } catch (Exception ex) {
                    failure.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }
        startGate.countDown();
        doneGate.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertThat(success.get()).as("số luồng thành công").isEqualTo(5);
        assertThat(failure.get()).as("số luồng bị từ chối").isEqualTo(5);

        Inventory finalInv = reload(productId, warehouseId);
        assertThat(finalInv.getReservedQuantity())
                .as("reserved đúng bằng tồn, không dư một đơn vị nào")
                .isEqualTo(5);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Id ngắn (<= 20 ký tự) để khớp ràng buộc độ dài cột trong schema. */
    private static String sid(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    private void seedInventory(String productId, String warehouseId, int qty) {
        inventoryRepository.save(new Inventory.Builder()
                .productId(productId)
                .warehouseId(warehouseId)
                .quantity(qty)
                .reservedQuantity(0)
                .version(0)
                .build());
    }

    private Inventory reload(String productId, String warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new AssertionError("Không tìm thấy tồn kho vừa seed"));
    }
}
