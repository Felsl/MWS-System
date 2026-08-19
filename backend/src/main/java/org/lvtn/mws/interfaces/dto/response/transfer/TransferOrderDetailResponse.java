package org.lvtn.mws.interfaces.dto.response.transfer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferOrderDetailResponse {
    private String id;
    private String transferOrderId;
    private String productId;
    private String batchId;
    private String batchNumber;
    private String designatedBatchId;
    private String supplierId;
    private String batchMode;
    private int quantity;
    private int quantityReceived;
    private int lostQuantity;
    private String fromBinLocationId;
    private String fromBinLocationLabel;
    private String binLocationId;
    private String binLocationLabel;
}
