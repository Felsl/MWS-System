package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Carrier;

import java.util.List;
import java.util.Optional;

public interface ICarrierRepository {

    Carrier save(Carrier carrier);

    Optional<Carrier> findById(String id);

    List<Carrier> findAll();

    boolean existsByCode(String code);
}
