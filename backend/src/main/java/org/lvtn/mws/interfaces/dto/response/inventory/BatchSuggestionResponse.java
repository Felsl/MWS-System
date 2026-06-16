package org.lvtn.mws.interfaces.dto.response.inventory;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class BatchSuggestionResponse {
    private String batchId;
    private String batchNumber;
    private String binLocationId;
    private int suggestedQuantity;
}
