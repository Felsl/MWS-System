package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IIdGenerator;
import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.lvtn.mws.interfaces.dto.request.warehouse.BulkGenerateBinRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BulkGenerateBinLocationsUseCase {

    private final WarehouseDomainService warehouseDomainService;
    private final IIdGenerator           idGenerator;

    /**
     * Thuật toán sinh hàng loạt ô kệ từ cấu trúc nested:
     * zones -> aisles -> racks -> bins
     *
     * Input ví dụ:
     * [{
     *   "zone": "A",
     *   "aisles": [{
     *     "aisle": "01",
     *     "racks": [{
     *       "rack": "R1",
     *       "bins": ["B1","B2","B3"]
     *     }]
     *   }]
     * }]
     *
     * Kết quả: tự động tạo tọa độ A-01-R1-B1, A-01-R1-B2, A-01-R1-B3
     */
    @Transactional
    public List<BinLocation> execute(String warehouseId,
                                     List<BulkGenerateBinRequest.ZoneConfig> zones) {
        List<BinLocation> candidates = new ArrayList<>();

        for (BulkGenerateBinRequest.ZoneConfig zoneConfig : zones) {
            String zone = zoneConfig.getZone();

            for (BulkGenerateBinRequest.AisleConfig aisleConfig : zoneConfig.getAisles()) {
                String aisle = aisleConfig.getAisle();

                for (BulkGenerateBinRequest.RackConfig rackConfig : aisleConfig.getRacks()) {
                    String rack = rackConfig.getRack();

                    for (String bin : rackConfig.getBins()) {
                        BinLocation bl = new BinLocation.BinLocationBuilder()
                                .id(idGenerator.generate())
                                .warehouseId(warehouseId)
                                .zone(zone)
                                .aisle(aisle)
                                .rack(rack)
                                .bin(bin)
                                .build();
                        candidates.add(bl);
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("No bin locations to generate");
        }

        return warehouseDomainService.bulkGenerateBinLocations(warehouseId, candidates);
    }
}
