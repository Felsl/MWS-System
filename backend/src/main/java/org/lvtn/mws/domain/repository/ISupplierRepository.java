package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Supplier;
import java.util.List;
import java.util.Optional;

public interface ISupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(String id);
    List<Supplier> findAll();
    boolean existsByCode(String code);
}
