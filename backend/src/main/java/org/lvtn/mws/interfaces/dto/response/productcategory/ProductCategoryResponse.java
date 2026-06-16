package org.lvtn.mws.interfaces.dto.response.productcategory;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProductCategoryResponse {
    private String id;
    private String code;
    private String name;
    private String description;
}
