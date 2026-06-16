package org.lvtn.mws.interfaces.dto.response.inventory;

import lombok.Builder;
import lombok.Data;
import org.lvtn.mws.domain.model.InventoryBatch.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class InventoryBatchResponse {
    private String id;
    private String productId;
    private String warehouseId;
    private String binLocationId;
    private String batchNumber;
    private int quantity;
    private LocalDate expiryDate;
    private LocalDate manufacturedDate;
    private Status status;
    private LocalDateTime createdAt;
}
