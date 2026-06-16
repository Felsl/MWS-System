package org.lvtn.mws.interfaces.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String        id;
    private String        username;
    private String        fullName;
    private String        email;
    private String        phone;
    private String        status;
    private String        roleId;
    private String        roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
