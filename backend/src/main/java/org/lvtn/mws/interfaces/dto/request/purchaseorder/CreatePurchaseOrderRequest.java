package org.lvtn.mws.interfaces.dto.request.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePurchaseOrderRequest {
    @NotBlank(message = "supplierId không được trống")
    private String supplierId;

    @NotBlank(message = "warehouseId không được trống")
    private String warehouseId;

    private LocalDate expectedDate;

    @Valid
    @NotEmpty(message = "Đơn mua phải có ít nhất một dòng hàng")
    private List<PurchaseOrderLineRequest> lines;
}
