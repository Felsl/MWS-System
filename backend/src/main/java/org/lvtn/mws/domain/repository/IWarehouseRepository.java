package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Warehouse;

import java.util.List;
import java.util.Optional;

public interface IWarehouseRepository {
    Warehouse save(Warehouse warehouse);
    Optional<Warehouse> findById(String id);
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findAllActive();
    List<Warehouse> findByIds(List<String> ids);
    boolean existsById(String id);
    boolean existsByCode(String code);
    boolean existsByCodeExcludingId(String id, String code);
}
