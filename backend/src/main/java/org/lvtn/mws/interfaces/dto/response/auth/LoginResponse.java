package org.lvtn.mws.interfaces.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String       accessToken;
    private String       userId;
    private String       username;
    private String       fullName;
    private String       role;
    private List<String> permissions;
}
