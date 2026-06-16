package org.lvtn.mws.interfaces.dto.response.warehouse;

import java.util.List;

public class UserWarehouseResponseDTO {
    private String userId;
    private List<String> warehouseIds;

    public UserWarehouseResponseDTO() {}

    public UserWarehouseResponseDTO(String userId, List<String> warehouseIds) {
        this.userId = userId;
        this.warehouseIds = warehouseIds;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<String> getWarehouseIds() { return warehouseIds; }
    public void setWarehouseIds(List<String> warehouseIds) { this.warehouseIds = warehouseIds; }
}
