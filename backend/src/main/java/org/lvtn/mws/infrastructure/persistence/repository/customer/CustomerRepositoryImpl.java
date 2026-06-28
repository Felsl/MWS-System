package org.lvtn.mws.infrastructure.persistence.repository.customer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Customer;
import org.lvtn.mws.domain.repository.ICustomerRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.CustomerInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements ICustomerRepository {

    private final JpaCustomerRepository jpa;
    private final CustomerInfraMapper mapper;

    @Override
    public Customer save(Customer customer) {
        jpa.save(mapper.toEntity(customer));
        return customer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> result = new ArrayList<>();
        jpa.findByDeletedAtIsNull().forEach(e -> result.add(mapper.toDomain(e)));
        return result;
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCode(code);
    }
}
