package org.lvtn.mws.interfaces.dto.response.goodsreceipt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor
public class GoodsReceiptDetailResponse {
    private String id;
    private String grnId;
    private String productId;
    private String poDetailId;
    private int quantity;
    private String batchNumber;
    private LocalDate expiryDate;
    private String binLocationId;
}
