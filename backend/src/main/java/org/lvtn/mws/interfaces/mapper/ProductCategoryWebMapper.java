package org.lvtn.mws.interfaces.mapper;

import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.interfaces.dto.response.productcategory.ProductCategoryResponse;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductCategoryWebMapper {

    default ProductCategoryResponse toResponse(ProductCategory domain) {
        if (domain == null) return null;
        return ProductCategoryResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }

    default List<ProductCategoryResponse> toResponseList(List<ProductCategory> domains) {
        if (domains == null) return List.of();
        return domains.stream().map(this::toResponse).toList();
    }
}
