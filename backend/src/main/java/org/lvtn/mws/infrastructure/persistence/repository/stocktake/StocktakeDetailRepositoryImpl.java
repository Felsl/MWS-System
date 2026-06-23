package org.lvtn.mws.infrastructure.persistence.repository.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StocktakeDetail;
import org.lvtn.mws.domain.repository.IStocktakeDetailRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.StocktakeDetailInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StocktakeDetailRepositoryImpl implements IStocktakeDetailRepository {

    private final JpaStocktakeDetailRepository jpa;
    private final StocktakeDetailInfraMapper mapper;

    @Override
    public StocktakeDetail save(StocktakeDetail detail) {
        return mapper.toDomain(jpa.save(mapper.toEntity(detail)));
    }

    @Override
    public void saveAll(List<StocktakeDetail> details) {
        jpa.saveAll(details.stream().map(mapper::toEntity).collect(Collectors.toList()));
    }

    @Override
    public Optional<StocktakeDetail> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<StocktakeDetail> findBySessionId(String sessionId) {
        return jpa.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
    }
}
