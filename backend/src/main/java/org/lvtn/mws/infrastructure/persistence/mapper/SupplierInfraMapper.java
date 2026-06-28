package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Supplier;
import org.lvtn.mws.infrastructure.persistence.entity.SupplierEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierInfraMapper {

    default SupplierEntity toEntity(Supplier s) {
        if (s == null) return null;
        SupplierEntity e = new SupplierEntity();
        e.setId(s.getId());
        e.setCode(s.getCode());
        e.setName(s.getName());
        e.setContactName(s.getContactName());
        e.setPhone(s.getPhone());
        e.setEmail(s.getEmail());
        e.setAddress(s.getAddress());
        e.setStatus(s.getStatus());
        e.setDeletedAt(s.getDeletedAt());
        return e;
    }

    default Supplier toDomain(SupplierEntity e) {
        if (e == null) return null;
        return new Supplier.Builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .contactName(e.getContactName())
                .phone(e.getPhone())
                .email(e.getEmail())
                .address(e.getAddress())
                .status(e.getStatus())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
