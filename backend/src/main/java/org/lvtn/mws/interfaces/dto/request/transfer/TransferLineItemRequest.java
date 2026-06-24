package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransferLineItemRequest {

    @NotBlank(message = "productId không được trống")
    private String productId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
}
