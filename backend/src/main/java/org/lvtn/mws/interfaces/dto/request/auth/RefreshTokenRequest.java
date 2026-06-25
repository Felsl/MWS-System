package org.lvtn.mws.interfaces.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken không được trống")
    private String refreshToken;
}
