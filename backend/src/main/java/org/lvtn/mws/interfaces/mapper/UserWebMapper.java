package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.interfaces.dto.response.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserWebMapper {

    public UserResponse toResponse(User domain) {
        if (domain == null) return null;
        return UserResponse.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .fullName(domain.getFullName())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .roleId(domain.getRoleId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
