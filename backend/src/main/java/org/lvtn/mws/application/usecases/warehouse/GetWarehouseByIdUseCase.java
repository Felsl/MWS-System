package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWarehouseByIdUseCase {

    private final WarehouseDomainService warehouseDomainService;

    public Warehouse execute(String id) {
        return warehouseDomainService.findById(id);
    }
}
