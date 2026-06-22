package org.lvtn.mws.infrastructure.persistence.repository.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.SalesOrder;
import org.lvtn.mws.domain.model.SalesOrder.Status;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.SalesOrderInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SalesOrderRepositoryImpl implements ISalesOrderRepository {

    private final JpaSalesOrderRepository jpa;
    private final SalesOrderInfraMapper mapper;

    @Override
    public SalesOrder save(SalesOrder salesOrder) {
        return mapper.toDomain(jpa.save(mapper.toEntity(salesOrder)));
    }

    @Override
    public Optional<SalesOrder> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<SalesOrder> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SalesOrder> findByStatus(Status status) {
        return jpa.findByStatus(status.name()).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySoNumber(String soNumber) {
        return jpa.existsBySoNumber(soNumber);
    }
}
