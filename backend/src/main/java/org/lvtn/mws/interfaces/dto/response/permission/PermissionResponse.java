package org.lvtn.mws.interfaces.dto.response.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private String id;
    private String code;
    private String name;
    private String module;
}
