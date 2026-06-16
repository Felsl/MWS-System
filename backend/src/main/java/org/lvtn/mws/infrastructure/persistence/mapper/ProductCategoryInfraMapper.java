package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.infrastructure.persistence.entity.ProductCategoryEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryInfraMapper {

    default ProductCategoryEntity toEntity(ProductCategory domain) {
        if (domain == null) return null;
        ProductCategoryEntity e = new ProductCategoryEntity();
        e.setId(domain.getId());
        e.setCode(domain.getCode());
        e.setName(domain.getName());
        e.setDescription(domain.getDescription());
        e.setDeletedAt(domain.getDeletedAt());
        return e;
    }

    default ProductCategory toDomain(ProductCategoryEntity e) {
        if (e == null) return null;
        return new ProductCategory.Builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .description(e.getDescription())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
