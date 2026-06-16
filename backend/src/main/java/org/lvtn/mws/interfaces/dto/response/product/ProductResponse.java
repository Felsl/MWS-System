package org.lvtn.mws.interfaces.dto.response.product;

import lombok.Builder;
import lombok.Data;
import org.lvtn.mws.domain.model.Product.Unit;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProductResponse {
    private String id;
    private String categoryId;
    private String categoryName;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Unit unit;
    private int safetyStock;
    private BigDecimal weight;
    private BigDecimal volume;
    private boolean hazardousFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
