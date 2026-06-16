package org.lvtn.mws.application.usecases.warehouse;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Warehouse;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScopeContext;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllWarehousesUseCase {

    private final IWarehouseRepository warehouseRepository;

    /**
     * @WarehouseScoped: AOP sẽ tự động nạp danh sách warehouse_id
     * user được phép vào WarehouseScopeContext trước khi method này chạy.
     *
     * Nếu user là ADMIN (không có record nào trong user_warehouse_access),
     * context trả về list rỗng → trả toàn bộ kho (không filter).
     *
     * Nếu user là STOREKEEPER → chỉ trả kho họ được giao.
     *
     * Lưu ý: @Cacheable không dùng ở đây vì kết quả phụ thuộc vào user,
     * cache theo key = userId nếu cần tối ưu thêm.
     */
    @WarehouseScoped
    public List<Warehouse> execute() {
        List<String> allowedIds = WarehouseScopeContext.get();

        if (allowedIds.isEmpty()) {
            // Admin hoặc role không bị giới hạn scope → trả tất cả
            return warehouseRepository.findAllActive();
        }

        // User bị giới hạn scope → chỉ trả kho trong danh sách được phép
        return warehouseRepository.findByIds(allowedIds);
    }
}
