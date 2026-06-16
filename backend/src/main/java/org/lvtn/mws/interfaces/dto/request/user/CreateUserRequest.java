package org.lvtn.mws.interfaces.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

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
