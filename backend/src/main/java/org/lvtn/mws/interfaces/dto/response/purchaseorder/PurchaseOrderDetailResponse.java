package org.lvtn.mws.interfaces.dto.response.purchaseorder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderDetailResponse {
    private String id;
    private String poId;
    private String productId;
    private int quantityOrdered;
    private int quantityReceived;
    private BigDecimal unitPrice;
}
