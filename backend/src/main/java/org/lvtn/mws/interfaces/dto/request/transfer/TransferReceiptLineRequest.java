package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransferReceiptLineRequest {

    @NotBlank(message = "detailId không được trống")
    private String detailId;

    @Min(value = 0, message = "Số lượng nhận không được âm")
    private int quantityReceived;

    /** Ô kệ đích do thủ kho quét chọn khi putaway. */
    @NotBlank(message = "binLocationId (ô kệ đích) không được trống")
    private String binLocationId;
}
