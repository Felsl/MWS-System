package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBinLocationsByWarehouseUseCase {

    private final WarehouseDomainService warehouseDomainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public List<BinLocation> execute(String warehouseId) {
        // A2: chặn xem ô kệ của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return warehouseDomainService.findBinLocationsByWarehouse(warehouseId);
    }
}
