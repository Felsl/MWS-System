package org.lvtn.mws.interfaces.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignWarehouseRequest {
    @NotNull(message = "Warehouse IDs are required")
    private List<String> warehouseIds;
}
