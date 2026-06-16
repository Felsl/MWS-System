package org.lvtn.mws.interfaces.dto.request.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreateBatchRequest {
    @NotBlank private String productId;
    @NotBlank private String warehouseId;
    @NotBlank private String binLocationId;
    @Min(1) private int quantity;
    private LocalDate expiryDate;
    private LocalDate manufacturedDate;
}
