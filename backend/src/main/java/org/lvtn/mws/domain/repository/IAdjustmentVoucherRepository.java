package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.AdjustmentVoucher;
import java.util.List;
import java.util.Optional;

public interface IAdjustmentVoucherRepository {
    AdjustmentVoucher save(AdjustmentVoucher voucher);
    Optional<AdjustmentVoucher> findById(String id);
    List<AdjustmentVoucher> findAll();
    /** [A2] Chỉ phiếu điều chỉnh thuộc kho user được phép (đọc từ WarehouseScopeContext). */
    List<AdjustmentVoucher> findAllScoped();
    /** [B4] Tìm kiếm (mã phiếu + trạng thái) + phân trang, lọc theo phạm vi kho. */
    org.lvtn.mws.domain.common.PageResult<AdjustmentVoucher> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery);
    List<AdjustmentVoucher> findBySessionId(String sessionId);
    boolean existsByVoucherNumber(String voucherNumber);
}
