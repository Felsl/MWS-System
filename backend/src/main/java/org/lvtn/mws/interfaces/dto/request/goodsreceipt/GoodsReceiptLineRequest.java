package org.lvtn.mws.interfaces.dto.request.goodsreceipt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GoodsReceiptLineRequest {
    @NotBlank(message = "productId không được trống")
    private String productId;

    private String poDetailId; // nullable for off-PO receipts

    @Min(value = 1, message = "quantity phải lớn hơn 0")
    private int quantity;

    private String batchNumber;
    private LocalDate expiryDate;

    @NotBlank(message = "binLocationId không được trống")
    private String binLocationId;
}
