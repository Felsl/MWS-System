package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DispatchTransferRequest {

    @NotBlank(message = "carrierId không được trống")
    private String carrierId;
}
