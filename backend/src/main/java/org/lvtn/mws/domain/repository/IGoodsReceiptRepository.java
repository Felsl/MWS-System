package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.GoodsReceipt;
import java.util.List;
import java.util.Optional;

public interface IGoodsReceiptRepository {
    GoodsReceipt save(GoodsReceipt goodsReceipt);
    Optional<GoodsReceipt> findById(String id);
    Optional<GoodsReceipt> findByGrnNumber(String grnNumber);
    List<GoodsReceipt> findByPoId(String poId);
    /** [B4] Tìm kiếm (mã GRN + trạng thái) + phân trang, đã lọc theo phạm vi kho. */
    org.lvtn.mws.domain.common.PageResult<GoodsReceipt> search(
            String keyword, String status, org.lvtn.mws.domain.common.PageQuery pageQuery);
    boolean existsByGrnNumber(String grnNumber);
}
