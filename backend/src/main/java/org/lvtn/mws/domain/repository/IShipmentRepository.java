package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Shipment;

import java.util.List;
import java.util.Optional;

public interface IShipmentRepository {
    Shipment save(Shipment shipment);
    Optional<Shipment> findById(String id);
    Optional<Shipment> findBySalesOrderId(String salesOrderId);
    List<Shipment> findAll();
    boolean existsByShipmentNumber(String shipmentNumber);
}
