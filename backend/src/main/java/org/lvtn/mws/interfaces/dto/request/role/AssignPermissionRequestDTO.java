package org.lvtn.mws.interfaces.dto.request.role;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignPermissionRequestDTO {
    @NotEmpty(message = "Permission IDs cannot be empty")
    private List<String> permissionIds;

    public List<String> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<String> permissionIds) { this.permissionIds = permissionIds; }
}
