package org.lvtn.mws.interfaces.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinLocationResponse {
    private String id;
    private String warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String bin;
    private String coordinateLabel; // vd: "A-01-R1-B1" cho hiển thị UI
}
