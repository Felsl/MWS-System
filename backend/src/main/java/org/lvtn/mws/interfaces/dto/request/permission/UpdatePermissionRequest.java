package org.lvtn.mws.interfaces.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotBlank(message = "Permission code is required")
    @Size(max = 100)
    private String code;

    @NotBlank(message = "Permission name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Module is required")
    @Size(max = 50)
    private String module;
}
