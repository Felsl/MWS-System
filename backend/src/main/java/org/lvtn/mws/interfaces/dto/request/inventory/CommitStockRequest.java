package org.lvtn.mws.interfaces.dto.request.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CommitStockRequest {
    @NotBlank private String productId;
    @NotBlank private String warehouseId;
    @NotEmpty private List<BatchDeductionItemRequest> details;

    @Data
    public static class BatchDeductionItemRequest {
        @NotBlank private String batchId;
        @Min(1) private int quantity;
    }
}
