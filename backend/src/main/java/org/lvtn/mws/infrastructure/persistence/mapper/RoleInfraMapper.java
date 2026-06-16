package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Role;
import org.lvtn.mws.infrastructure.persistence.entity.RoleDbEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleInfraMapper {

    default RoleDbEntity toEntity(Role domain) {
        if (domain == null) return null;
        RoleDbEntity e = new RoleDbEntity();
        e.setId(domain.getId());
        e.setCode(domain.getCode());
        e.setName(domain.getName());
        e.setDescription(domain.getDescription());
        return e;
    }

    default Role toDomain(RoleDbEntity e, List<String> permissionIds) {
        if (e == null) return null;
        return new Role.RoleBuilder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .description(e.getDescription())
                .permissionIds(permissionIds)
                .build();
    }
}
