package org.lvtn.mws.interfaces.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.lvtn.mws.domain.model.Product.Unit;
import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String categoryId;

    @NotBlank(message = "SKU không được trống")
    @Size(max = 50, message = "SKU tối đa 50 ký tự")
    private String sku;

    @Size(max = 50)
    private String barcode;

    @NotBlank(message = "Tên sản phẩm không được trống")
    @Size(max = 255)
    private String name;

    private String description;

    @Min(value = 0, message = "Giá bán không được âm")
    private BigDecimal price;

    @Min(value = 0, message = "Giá vốn không được âm")
    private BigDecimal costPrice;

    @NotNull(message = "Đơn vị tính không được trống")
    private Unit unit;

    @Min(value = 0, message = "Tồn kho an toàn không được âm")
    private int safetyStock = 10;

    private BigDecimal weight;
    private BigDecimal volume;
    private boolean hazardousFlag = false;
}
