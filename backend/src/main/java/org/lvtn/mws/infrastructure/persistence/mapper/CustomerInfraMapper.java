package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.infrastructure.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerInfraMapper {

    default CustomerEntity toEntity(Customer c) {
        if (c == null) return null;
        CustomerEntity e = new CustomerEntity();
        e.setId(c.getId());
        e.setCode(c.getCode());
        e.setName(c.getName());
        e.setTaxCode(c.getTaxCode());
        e.setPhone(c.getPhone());
        e.setEmail(c.getEmail());
        e.setAddress(c.getAddress());
        e.setStatus(c.getStatus());
        e.setCreatedAt(c.getCreatedAt());
        e.setDeletedAt(c.getDeletedAt());
        return e;
    }

    default Customer toDomain(CustomerEntity e) {
        if (e == null) return null;
        return new Customer.Builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .taxCode(e.getTaxCode())
                .phone(e.getPhone())
                .email(e.getEmail())
                .address(e.getAddress())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
