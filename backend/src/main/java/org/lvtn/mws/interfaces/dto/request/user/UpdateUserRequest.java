package org.lvtn.mws.interfaces.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @Email(message = "Email is invalid")
    private String email;

    @Size(max = 20)
    private String phone;

    @NotBlank(message = "Role is required")
    private String roleId;
}
