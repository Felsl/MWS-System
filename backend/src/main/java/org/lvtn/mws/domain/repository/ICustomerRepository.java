package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Customer;
import java.util.List;
import java.util.Optional;

public interface ICustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(String id);
    List<Customer> findAll();
    boolean existsByCode(String code);
}
