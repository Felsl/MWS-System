package org.lvtn.mws.infrastructure.persistence.repository.warehouse;

import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.lvtn.mws.infrastructure.persistence.entity.UserWarehouseAccessDbEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserWarehouseRepositoryImpl implements IUserWarehouseAccessRepository {

    private final UserWarehouseJpaRepository jpaRepository;

    public UserWarehouseRepositoryImpl(UserWarehouseJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void assignWarehouses(String userId, List<String> warehouseIds) {
        List<UserWarehouseAccessDbEntity> entities = warehouseIds.stream()
                .map(wid -> new UserWarehouseAccessDbEntity(
                        new UserWarehouseAccessDbEntity.UserWarehouseId(userId, wid)))
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void revokeAllForUser(String userId) {
        jpaRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeWarehouse(String userId, String warehouseId) {
        jpaRepository.deleteByUserIdAndWarehouseId(userId, warehouseId);
    }

    @Override
    public List<String> findWarehouseIdsByUserId(String userId) {
        return jpaRepository.findWarehouseIdsByUserId(userId);
    }

    @Override
    public List<String> findWarehouseIdsByUsername(String username) {
        return jpaRepository.findWarehouseIdsByUsername(username);
    }
}