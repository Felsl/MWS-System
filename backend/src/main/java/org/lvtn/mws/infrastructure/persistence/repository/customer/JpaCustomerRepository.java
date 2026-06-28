package org.lvtn.mws.infrastructure.persistence.repository.customer;

import org.lvtn.mws.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, String> {
    boolean existsByCode(String code);
    List<CustomerEntity> findByDeletedAtIsNull();
}
