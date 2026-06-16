package org.lvtn.mws.interfaces.dto.request.productcategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCategoryRequest {

    @NotBlank(message = "Mã danh mục không được trống")
    @Size(max = 50, message = "Mã danh mục tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên danh mục không được trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;

    private String description;
}
