package org.lvtn.mws.infrastructure.persistence.repository.shipment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.repository.IShipmentRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.ShipmentInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryImpl implements IShipmentRepository {

    private final JpaShipmentRepository jpa;
    private final ShipmentInfraMapper mapper;

    @Override
    public Shipment save(Shipment shipment) {
        return mapper.toDomain(jpa.save(mapper.toEntity(shipment)));
    }

    @Override
    public Optional<Shipment> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findBySalesOrderId(String salesOrderId) {
        return jpa.findBySalesOrderId(salesOrderId).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByShipmentNumber(String shipmentNumber) {
        return jpa.existsByShipmentNumber(shipmentNumber);
    }
}
