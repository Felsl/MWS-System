package org.lvtn.mws.interfaces.dto.response.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String       id;
    private String       code;
    private String       name;
    private String       description;
    private List<String> permissionIds;
}
