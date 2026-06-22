package org.lvtn.mws.interfaces.dto.response.goodsreceipt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class GoodsReceiptResponse {
    private String id;
    private String grnNumber;
    private String poId;
    private String warehouseId;
    private String status;
    private String receivedBy;
    private LocalDateTime receivedAt;
    private String note;
    private List<GoodsReceiptDetailResponse> details;
}
