package org.lvtn.mws.interfaces.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGenerateResultResponse {
    private int                    totalGenerated;
    private List<BinLocationResponse> binLocations;
}
