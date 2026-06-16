package org.lvtn.mws.interfaces.dto.request.warehouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request cho API bulk generate bin locations.
 *
 * Cấu trúc nested:
 * [
 *   {
 *     "zone": "A",
 *     "aisles": [
 *       {
 *         "aisle": "01",
 *         "racks": [
 *           {
 *             "rack": "R1",
 *             "bins": ["B1", "B2", "B3", "B4", "B5"]
 *           }
 *         ]
 *       }
 *     ]
 *   }
 * ]
 */
@Data
public class BulkGenerateBinRequest {

    @NotEmpty(message = "At least one zone is required")
    @Valid
    private List<ZoneConfig> zones;

    @Data
    public static class ZoneConfig {
        @NotBlank(message = "Zone is required")
        private String zone;

        @NotEmpty(message = "At least one aisle is required")
        @Valid
        private List<AisleConfig> aisles;
    }

    @Data
    public static class AisleConfig {
        @NotBlank(message = "Aisle is required")
        private String aisle;

        @NotEmpty(message = "At least one rack is required")
        @Valid
        private List<RackConfig> racks;
    }

    @Data
    public static class RackConfig {
        @NotBlank(message = "Rack is required")
        private String rack;

        @NotEmpty(message = "At least one bin is required")
        private List<String> bins;
    }
}
