package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.interfaces.dto.response.role.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RoleWebMapper {

    public RoleResponse toResponse(Role domain) {
        if (domain == null) return null;
        return RoleResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .permissionIds(domain.getPermissionIds())
                .build();
    }
}
