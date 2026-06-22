package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import java.util.List;

public interface IGoodsReceiptDetailRepository {
    GoodsReceiptDetail save(GoodsReceiptDetail detail);
    void saveAll(List<GoodsReceiptDetail> details);
    List<GoodsReceiptDetail> findByGrnId(String grnId);
}
