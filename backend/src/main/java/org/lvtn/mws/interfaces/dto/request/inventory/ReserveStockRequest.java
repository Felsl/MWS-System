package org.lvtn.mws.interfaces.dto.request.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReserveStockRequest {
    @NotBlank private String productId;
    @NotBlank private String warehouseId;
    @Min(1) private int quantity;
}
