package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CompleteReceiptRequest {

    @NotEmpty(message = "Danh sách nhận hàng không được trống")
    @Valid
    private List<TransferReceiptLineRequest> lines;
}
