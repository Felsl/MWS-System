package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrder.Status;

import java.util.List;
import java.util.Optional;

public interface ISalesOrderRepository {
    /** Lưu aggregate (đơn + dòng) trong một lần. */
    SalesOrder save(SalesOrder salesOrder);
    Optional<SalesOrder> findById(String id);
    List<SalesOrder> findAll();
    /** [A2] Chỉ đơn thuộc kho user được phép (đọc từ WarehouseScopeContext). */
    List<SalesOrder> findAllScoped();
    /** [B4] Tìm kiếm + phân trang, đã lọc theo phạm vi kho (WarehouseScopeContext). */
    org.lvtn.mws.domain.common.PageResult<SalesOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery);
    List<SalesOrder> findByStatus(Status status);
    boolean existsBySoNumber(String soNumber);
}
