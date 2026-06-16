package org.lvtn.mws.infrastructure.persistence.mapper;

import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.infrastructure.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductInfraMapper {

    default ProductEntity toEntity(Product domain) {
        if (domain == null) return null;
        ProductEntity e = new ProductEntity();
        e.setId(domain.getId());
        e.setCategoryId(domain.getCategoryId());
        e.setSku(domain.getSku());
        e.setBarcode(domain.getBarcode());
        e.setName(domain.getName());
        e.setDescription(domain.getDescription());
        e.setPrice(domain.getPrice());
        e.setCostPrice(domain.getCostPrice());
        e.setUnit(domain.getUnit());
        e.setSafetyStock(domain.getSafetyStock());
        e.setWeight(domain.getWeight());
        e.setVolume(domain.getVolume());
        e.setHazardousFlag(domain.isHazardousFlag());
        e.setVersion(domain.getVersion());
        e.setCreatedAt(domain.getCreatedAt());
        e.setUpdatedAt(domain.getUpdatedAt());
        e.setDeletedAt(domain.getDeletedAt());
        return e;
    }

    default Product toDomain(ProductEntity e) {
        if (e == null) return null;
        return new Product.Builder()
                .id(e.getId())
                .categoryId(e.getCategoryId())
                .sku(e.getSku())
                .barcode(e.getBarcode())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .costPrice(e.getCostPrice())
                .unit(e.getUnit())
                .safetyStock(e.getSafetyStock())
                .weight(e.getWeight())
                .volume(e.getVolume())
                .hazardousFlag(e.isHazardousFlag())
                .version(e.getVersion())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }
}
