package org.lvtn.mws.interfaces.dto.request.warehouse;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AssignWarehouseRequestDTO {
    @NotEmpty(message = "Warehouse IDs cannot be empty")
    private List<String> warehouseIds;

    public List<String> getWarehouseIds() { return warehouseIds; }
    public void setWarehouseIds(List<String> warehouseIds) { this.warehouseIds = warehouseIds; }
}
