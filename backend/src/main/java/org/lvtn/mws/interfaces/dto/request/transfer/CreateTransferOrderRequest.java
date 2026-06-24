package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateTransferOrderRequest {

    @NotBlank(message = "fromWarehouseId không được trống")
    private String fromWarehouseId;

    @NotBlank(message = "toWarehouseId không được trống")
    private String toWarehouseId;

    @NotBlank(message = "createdBy không được trống")
    private String createdBy;

    @NotEmpty(message = "Phiếu phải có ít nhất 1 dòng hàng")
    @Valid
    private List<TransferLineItemRequest> lines;
}
