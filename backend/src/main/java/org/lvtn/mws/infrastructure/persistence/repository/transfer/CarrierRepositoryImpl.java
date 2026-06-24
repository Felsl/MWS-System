package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Carrier;
import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.CarrierInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CarrierRepositoryImpl implements ICarrierRepository {

    private final JpaCarrierRepository jpaCarrierRepository;
    private final CarrierInfraMapper mapper;

    @Override
    public Carrier save(Carrier carrier) {
        jpaCarrierRepository.save(mapper.toEntity(carrier));
        return carrier;
    }

    @Override
    public Optional<Carrier> findById(String id) {
        return jpaCarrierRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Carrier> findAll() {
        List<Carrier> result = new ArrayList<>();
        jpaCarrierRepository.findAll().forEach(e -> result.add(mapper.toDomain(e)));
        return result;
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCarrierRepository.existsByCode(code);
    }
}
