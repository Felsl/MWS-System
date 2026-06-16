package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteWarehouseUseCase {

    private final WarehouseDomainService warehouseDomainService;

    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public void execute(String id) {
        warehouseDomainService.delete(id);
    }
}
