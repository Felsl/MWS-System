package org.lvtn.mws.infrastructure.persistence.repository.salesorder;

import org.lvtn.mws.infrastructure.persistence.entity.SalesOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaSalesOrderRepository extends JpaRepository<SalesOrderEntity, String> {
    List<SalesOrderEntity> findByStatus(String status);
    boolean existsBySoNumber(String soNumber);
    long countBySoNumberStartingWith(String prefix);
}
