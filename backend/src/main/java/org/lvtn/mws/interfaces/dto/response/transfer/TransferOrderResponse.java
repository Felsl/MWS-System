package org.lvtn.mws.interfaces.dto.response.transfer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransferOrderResponse {
    private String id;
    private String fromWarehouseId;
    private String fromWarehouseName;
    private String toWarehouseId;
    private String toWarehouseName;
    private String transferNumber;
    private String status;
    private String createdBy;
    private String createdByName;
    private String approvedBy;
    private String approvedByName;
    private String pickingListId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferOrderDetailResponse> details;
}
