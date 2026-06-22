package org.lvtn.mws.infrastructure.persistence.repository.picking;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.PickingList;
import org.lvtn.mws.domain.repository.IPickingListRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.PickingListInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PickingListRepositoryImpl implements IPickingListRepository {

    private final JpaPickingListRepository jpa;
    private final PickingListInfraMapper mapper;

    @Override
    public PickingList save(PickingList pickingList) {
        return mapper.toDomain(jpa.save(mapper.toEntity(pickingList)));
    }

    @Override
    public Optional<PickingList> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<PickingList> findBySoId(String soId) {
        return jpa.findBySoId(soId).map(mapper::toDomain);
    }

    @Override
    public List<PickingList> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<PickingList> findByDetailId(String pickingListDetailId) {
        return jpa.findByDetailId(pickingListDetailId).map(mapper::toDomain);
    }
}
