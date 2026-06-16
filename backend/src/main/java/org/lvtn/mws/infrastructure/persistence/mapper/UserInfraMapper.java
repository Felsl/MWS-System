package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.infrastructure.persistence.entity.UserDbEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserInfraMapper {

    default UserDbEntity toEntity(User domain) {
        if (domain == null) return null;
        UserDbEntity e = new UserDbEntity();
        e.setId(domain.getId());
        e.setUsername(domain.getUsername());
        e.setPassword(domain.getPassword());
        e.setFullName(domain.getFullName());
        e.setEmail(domain.getEmail());
        e.setPhone(domain.getPhone());
        e.setStatus(domain.getStatus());
        e.setRoleId(domain.getRoleId());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        e.setDeletedAt(domain.getDeletedAt());
        return e;
    }

    default User toDomain(UserDbEntity e) {
        if (e == null) return null;
        return new User.UserBuilder()
                .id(e.getId())
                .username(e.getUsername())
                .password(e.getPassword())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .status(e.getStatus())
                .roleId(e.getRoleId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
