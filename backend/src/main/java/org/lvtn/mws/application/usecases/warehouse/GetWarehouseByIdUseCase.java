package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWarehouseByIdUseCase {

    private final WarehouseDomainService warehouseDomainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public Warehouse execute(String id) {
        // A2: chặn xem chi tiết kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(id);
        return warehouseDomainService.findById(id);
    }
}
