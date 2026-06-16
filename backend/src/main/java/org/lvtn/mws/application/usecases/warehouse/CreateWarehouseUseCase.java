package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.IIdGenerator;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWarehouseUseCase {

    private final WarehouseDomainService warehouseDomainService;
    private final IIdGenerator           idGenerator;

    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public Warehouse execute(String code, String name, String address) {
        return warehouseDomainService.create(idGenerator.generate(), code, name, address);
    }
}
