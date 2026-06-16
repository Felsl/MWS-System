package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.interfaces.dto.response.permission.PermissionResponse;
import org.springframework.stereotype.Component;

@Component
public class PermissionWebMapper {

    public PermissionResponse toResponse(Permission domain) {
        if (domain == null) return null;
        return PermissionResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .module(domain.getModule())
                .build();
    }
}
