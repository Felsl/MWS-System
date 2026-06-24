package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Shipment;
import org.lvtn.mws.domain.repository.IShipmentRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.ShipmentInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ShipmentRepositoryImpl implements IShipmentRepository {

    private final JpaShipmentRepository jpaShipmentRepository;
    private final ShipmentInfraMapper mapper;

    @Override
    public Shipment save(Shipment shipment) {
        jpaShipmentRepository.save(mapper.toEntity(shipment));
        return shipment;
    }

    @Override
    public Optional<Shipment> findById(String id) {
        return jpaShipmentRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Shipment> findByTransferOrderId(String transferOrderId) {
        return jpaShipmentRepository.findByTransferOrderId(transferOrderId).map(mapper::toDomain);
    }

    @Override
    public List<Shipment> findAll() {
        List<Shipment> result = new ArrayList<>();
        jpaShipmentRepository.findAll().forEach(e -> result.add(mapper.toDomain(e)));
        return result;
    }
}
