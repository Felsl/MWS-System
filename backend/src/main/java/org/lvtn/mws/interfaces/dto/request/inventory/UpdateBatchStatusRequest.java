package org.lvtn.mws.interfaces.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.lvtn.mws.domain.model.InventoryBatch.Status;

@Data
public class UpdateBatchStatusRequest {
    @NotNull private Status status;
}
