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
    List<SalesOrder> findByStatus(Status status);
    boolean existsBySoNumber(String soNumber);
}
