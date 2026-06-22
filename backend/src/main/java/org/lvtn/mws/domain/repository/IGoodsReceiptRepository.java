package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.GoodsReceipt;
import java.util.List;
import java.util.Optional;

public interface IGoodsReceiptRepository {
    GoodsReceipt save(GoodsReceipt goodsReceipt);
    Optional<GoodsReceipt> findById(String id);
    Optional<GoodsReceipt> findByGrnNumber(String grnNumber);
    List<GoodsReceipt> findByPoId(String poId);
    boolean existsByGrnNumber(String grnNumber);
}
