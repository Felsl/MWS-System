package org.lvtn.mws.interfaces.dto.response.purchaseorder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderResponse {
    private String id;
    private String poNumber;
    private String supplierId;
    private String warehouseId;
    private String status;
    private LocalDate expectedDate;
    private String createdBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PurchaseOrderDetailResponse> details;
}
