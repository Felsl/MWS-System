package org.lvtn.mws.interfaces.dto.request.role;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionsRequest {
    @NotNull(message = "Permission IDs are required")
    private List<String> permissionIds;
}
