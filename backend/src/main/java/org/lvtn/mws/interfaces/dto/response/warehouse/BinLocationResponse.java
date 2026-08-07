package org.lvtn.mws.interfaces.dto.response.warehouse;

import lombok.AllArgsConstructor;
import java.math.BigDecimal;
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
    // [PA1] Sức chứa (từ cấu hình, dùng chung mọi ô; 0/null = không giới hạn) và mức đang chiếm (tính sống).
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;
    private BigDecimal occupiedWeight;
    private BigDecimal occupiedVolume;
}
