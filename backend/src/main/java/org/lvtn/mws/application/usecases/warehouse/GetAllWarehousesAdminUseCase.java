package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Dành riêng cho ADMIN — trả toàn bộ kho, có cache Caffeine.
 * Không áp dụng WarehouseScope vì admin xem được tất cả.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllWarehousesAdminUseCase {

    private final WarehouseDomainService warehouseDomainService;

    @Cacheable(value = "warehouses-all")
    public List<Warehouse> execute() {
        return warehouseDomainService.findAllActive();
    }
}
