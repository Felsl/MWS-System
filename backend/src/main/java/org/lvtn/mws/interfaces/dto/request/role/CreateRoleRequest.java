package org.lvtn.mws.interfaces.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Role code is required")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "Role name is required")
    @Size(max = 100)
    private String name;

    private String description;
}
