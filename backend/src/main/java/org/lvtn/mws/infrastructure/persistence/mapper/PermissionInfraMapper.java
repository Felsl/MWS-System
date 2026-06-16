package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Permission;
import org.lvtn.mws.infrastructure.persistence.entity.PermissionDbEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionInfraMapper {

    default PermissionDbEntity toEntity(Permission domain) {
        if (domain == null) return null;
        PermissionDbEntity e = new PermissionDbEntity();
        e.setId(domain.getId());
        e.setCode(domain.getCode());
        e.setName(domain.getName());
        e.setModule(domain.getModule());
        return e;
    }

    default Permission toDomain(PermissionDbEntity e) {
        if (e == null) return null;
        return new Permission.PermissionBuilder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .module(e.getModule())
                .build();
    }
}
