package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StocktakeSession;
import java.util.List;
import java.util.Optional;

public interface IStocktakeSessionRepository {
    StocktakeSession save(StocktakeSession session);
    Optional<StocktakeSession> findById(String id);
    List<StocktakeSession> findAll();
    /** [A2] Chỉ phiên kiểm kê thuộc kho user được phép (đọc từ WarehouseScopeContext). */
    List<StocktakeSession> findAllScoped();
    /** [B4] Tìm kiếm theo trạng thái + phân trang (đã lọc theo phạm vi kho). */
    org.lvtn.mws.domain.common.PageResult<StocktakeSession> search(
            String status, org.lvtn.mws.domain.common.PageQuery pageQuery);
    List<StocktakeSession> findByWarehouseId(String warehouseId);

    /** Phiên đang FREEZED của một kho (nếu có) — dùng cho cơ chế đóng băng kho. */
    Optional<StocktakeSession> findFrozenByWarehouseId(String warehouseId);

    /** Kho có đang bị đóng băng (có phiên FREEZED) hay không. */
    boolean isWarehouseFrozen(String warehouseId);
}
