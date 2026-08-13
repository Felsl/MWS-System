package org.lvtn.mws.application.usecases.inventory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.lvtn.mws.interfaces.dto.response.inventory.SellableProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * [Bán theo NCC] Danh sách sản phẩm CÓ THỂ BÁN trong một kho: chỉ tính lô ACTIVE và CHƯA hết hạn
 * (quantity - reserved > 0). Sản phẩm chỉ còn lô hết hạn / không ACTIVE sẽ KHÔNG xuất hiện.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSellableByWarehouseUseCase {

    private final IInventoryBatchRepository batchRepository;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public List<SellableProductResponse> execute(String warehouseId) {
        warehouseAccessGuard.check(warehouseId);
        Map<String, Integer> map = batchRepository.sumSellableByWarehouse(warehouseId);
        return map.entrySet().stream()
                .map(e -> new SellableProductResponse(e.getKey(), e.getValue()))
                .toList();
    }
}
