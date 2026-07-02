package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.TransferOrder;

import java.util.List;
import java.util.Optional;

public interface ITransferOrderRepository {

    /** Lưu phiếu kèm toàn bộ dòng chi tiết (insert/update + đồng bộ details). */
    TransferOrder save(TransferOrder transferOrder);

    Optional<TransferOrder> findById(String id);

    List<TransferOrder> findAll();
    /** [A2] Chỉ phiếu mà user có quyền ở kho nguồn HOẶC kho đích (đọc từ WarehouseScopeContext). */
    List<TransferOrder> findAllScoped();
    /** [B4] Tìm kiếm + phân trang (lọc theo phạm vi kho: nguồn hoặc đích). */
    org.lvtn.mws.domain.common.PageResult<TransferOrder> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery);

    List<TransferOrder> findByStatus(TransferOrder.Status status);

    boolean existsByTransferNumber(String transferNumber);
}
