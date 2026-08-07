package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinOccupancy;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * [PA1] Map id ô kệ -> mức đang chiếm (kg, thể tích) của một kho, tính sống từ tồn kho.
 * Dùng để hiển thị % đầy + cảnh báo mềm; KHÔNG lưu DB, KHÔNG chặn nghiệp vụ.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetBinOccupancyUseCase {

    private final IInventoryBatchRepository inventoryBatchRepository;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public Map<String, BinOccupancy> executeByWarehouse(String warehouseId) {
        // A2: chặn xem dữ liệu của kho ngoài phạm vi được giao (403).
        warehouseAccessGuard.check(warehouseId);
        Map<String, BinOccupancy> map = new HashMap<>();
        for (BinOccupancy o : inventoryBatchRepository.sumOccupancyByWarehouse(warehouseId)) {
            map.put(o.binLocationId(), o);
        }
        return map;
    }
}
