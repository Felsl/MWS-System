package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinLocation;
import org.lvtn.mws.domain.service.WarehouseDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Trả về map id ô kệ -> mã vị trí thân thiện (zone-aisle-rack-bin) cho một kho,
 * dùng để resolve binLocationId thành mã hiển thị khi truy suất lô hàng.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBinLocationCodesUseCase {

    private final WarehouseDomainService warehouseDomainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public Map<String, String> executeByWarehouse(String warehouseId) {
        // A2: chặn resolve mã ô kệ của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        return warehouseDomainService.findBinLocationsByWarehouse(warehouseId).stream()
                .collect(Collectors.toMap(
                        BinLocation::getId,
                        BinLocation::locationCode,
                        (a, b) -> a));
    }
}
