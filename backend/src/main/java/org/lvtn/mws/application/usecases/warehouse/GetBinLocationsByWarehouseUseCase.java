package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBinLocationsByWarehouseUseCase {

    private final WarehouseDomainService warehouseDomainService;

    public List<BinLocation> execute(String warehouseId) {
        return warehouseDomainService.findBinLocationsByWarehouse(warehouseId);
    }
}
