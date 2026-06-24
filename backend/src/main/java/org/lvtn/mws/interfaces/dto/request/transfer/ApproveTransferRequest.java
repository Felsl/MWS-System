package org.lvtn.mws.interfaces.dto.request.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApproveTransferRequest {

    @NotBlank(message = "approvedBy không được trống")
    private String approvedBy;
}
