package org.lvtn.mws.interfaces.dto.request.purchaseorder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderLineRequest {
    @NotBlank(message = "productId không được trống")
    private String productId;

    @Min(value = 1, message = "quantityOrdered phải lớn hơn 0")
    private int quantityOrdered;

    private BigDecimal unitPrice;
}
