package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.interfaces.dto.response.product.ProductResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    default ProductResponse toResponse(Product domain) {
        if (domain == null) return null;
        return ProductResponse.builder()
                .id(domain.getId())
                .categoryId(domain.getCategoryId())
                .sku(domain.getSku())
                .barcode(domain.getBarcode())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .costPrice(domain.getCostPrice())
                .unit(domain.getUnit())
                .safetyStock(domain.getSafetyStock())
                .weight(domain.getWeight())
                .volume(domain.getVolume())
                .hazardousFlag(domain.isHazardousFlag())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    default List<ProductResponse> toResponseList(List<Product> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toResponse).toList();
    }
}
