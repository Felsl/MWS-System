package org.lvtn.mws.interfaces.dto.request.permission;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignPermissionsRequestDTO {
    @NotNull(message = "Permission IDs are required")
    private List<String> permissionIds;

    public List<String> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<String> permissionIds) { this.permissionIds = permissionIds; }
}
