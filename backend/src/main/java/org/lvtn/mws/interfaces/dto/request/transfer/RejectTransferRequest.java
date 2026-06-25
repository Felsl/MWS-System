package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectTransferRequest {

    @NotBlank(message = "rejectedBy không được trống")
    private String rejectedBy;
}
