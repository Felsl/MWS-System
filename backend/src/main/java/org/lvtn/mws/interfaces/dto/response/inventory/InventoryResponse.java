package org.lvtn.mws.interfaces.dto.response.inventory;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class InventoryResponse {
    private String productId;
    private String warehouseId;
    private int quantity;
    private int reservedQuantity;
    private int availableQuantity;
    /** [PA1] NCC gộp cho sản phẩm này trong kho (suy từ GRN->PO); null nếu không truy được nguồn. */
    private String supplierName;
}
