package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.StockDemand;

import java.util.List;

public interface IStockDemandRepository {

    StockDemand save(StockDemand demand);

    /** Nhu cầu OPEN của một (sản phẩm, kho), cũ trước (FIFO) — để bù khi hàng về. */
    List<StockDemand> findOpenByProductAndWarehouse(String productId, String warehouseId);

    /** Nhu cầu OPEN của một đơn bán. */
    List<StockDemand> findOpenBySoId(String soId);

    /** Toàn bộ nhu cầu OPEN (mới nhất trước) — cho màn nhu cầu ở Dashboard. */
    List<StockDemand> findAllOpen();

    List<StockDemand> findBySoId(String soId);
}
