package org.lvtn.mws.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lvtn.mws.domain.model.BatchSuggestion;
import org.lvtn.mws.domain.model.InventoryBatch;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A3 — Chứng minh thuật toán FEFO (First-Expired-First-Out): khi gợi ý lấy hàng,
 * lô có hạn sử dụng gần nhất phải được đề xuất TRƯỚC, và chỉ tràn sang lô kế tiếp
 * khi lô trước đã hết.
 */
class FefoAllocationTest extends AbstractIntegrationTest {

    @Autowired
    private InventoryDomainService inventoryDomainService;

    @Autowired
    private IInventoryBatchRepository batchRepository;

    /** Id ngắn (<= 20 ký tự) để khớp ràng buộc độ dài cột trong schema. */
    private static String sid(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    @DisplayName("Ưu tiên lô cận hết hạn trước, tràn sang lô hạn xa hơn khi thiếu")
    void allocatesNearestExpiryFirst() {
        String productId = sid("P");
        String warehouseId = sid("W");

        // Lô LATE hết hạn muộn (2026-12-31), tạo TRƯỚC.
        seedBatch(productId, warehouseId, "BATCH-LATE",
                LocalDate.of(2026, 12, 31), 5, LocalDateTime.now().minusDays(2));
        // Lô EARLY hết hạn sớm hơn (2026-06-30), tạo SAU — FEFO vẫn phải chọn nó trước.
        seedBatch(productId, warehouseId, "BATCH-EARLY",
                LocalDate.of(2026, 6, 30), 5, LocalDateTime.now().minusDays(1));

        // Cần 7 → lấy hết 5 của EARLY rồi 2 của LATE.
        List<BatchSuggestion> suggestions =
                inventoryDomainService.allocateBatchesForPicking(productId, warehouseId, 7);

        assertThat(suggestions).hasSize(2);
        assertThat(suggestions.get(0).getBatchNumber())
                .as("lô đầu tiên phải là lô hết hạn SỚM nhất")
                .isEqualTo("BATCH-EARLY");
        assertThat(suggestions.get(0).getSuggestedQuantity()).isEqualTo(5);
        assertThat(suggestions.get(1).getBatchNumber()).isEqualTo("BATCH-LATE");
        assertThat(suggestions.get(1).getSuggestedQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("Không đủ hàng trong các lô ACTIVE → ném InsufficientStockException")
    void throwsWhenNotEnoughAcrossBatches() {
        String productId = sid("P");
        String warehouseId = sid("W");
        seedBatch(productId, warehouseId, "BATCH-ONLY",
                LocalDate.of(2026, 9, 30), 3, LocalDateTime.now());

        assertThatThrownBy(() ->
                inventoryDomainService.allocateBatchesForPicking(productId, warehouseId, 10))
                .isInstanceOf(org.lvtn.mws.domain.model.InsufficientStockException.class);
    }

    private void seedBatch(String productId, String warehouseId, String batchNumber,
                           LocalDate expiry, int qty, LocalDateTime createdAt) {
        batchRepository.save(new InventoryBatch.Builder()
                .id(sid("B"))
                .productId(productId)
                .warehouseId(warehouseId)
                .binLocationId(sid("BIN"))
                .batchNumber(batchNumber)
                .quantity(qty)
                .expiryDate(expiry)
                .status(InventoryBatch.Status.ACTIVE)
                .createdAt(createdAt)
                .version(0)
                .build());
    }
}
