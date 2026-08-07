package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import java.util.List;
import java.util.Map;

public interface IGoodsReceiptDetailRepository {
    GoodsReceiptDetail save(GoodsReceiptDetail detail);
    void saveAll(List<GoodsReceiptDetail> details);
    List<GoodsReceiptDetail> findByGrnId(String grnId);

    /** Map số lô -> tên nhà cung cấp cho (sản phẩm, kho). Lô không truy được nguồn thì vắng mặt. */
    Map<String, String> findBatchSuppliersByProductAndWarehouse(String productId, String warehouseId);

    /** [PA1] Map id sản phẩm -> tên NCC (gộp distinct, nối " / ") cho một kho — dùng ở màn tồn theo kho. */
    Map<String, String> findProductSuppliersByWarehouse(String warehouseId);
}
