package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockMovement;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.infrastructure.persistence.entity.StockMovementEntity;
import org.lvtn.mws.infrastructure.persistence.mapper.StockMovementInfraMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * STUB append-only cho thẻ kho.
 * ⚠️ Nếu module "stock" thật đã sở hữu bảng stock_movements, hãy XOÁ class + entity
 * + JPA repo + mapper StockMovement trong module này và đổi impl của
 * IStockMovementRepository thành adapter trỏ về repository thật của module đó.
 */
@Repository
@RequiredArgsConstructor
public class StockMovementRepositoryImpl implements IStockMovementRepository {

    private final JpaStockMovementRepository jpaStockMovementRepository;
    private final StockMovementInfraMapper mapper;

    @Override
    public StockMovement append(StockMovement movement) {
        jpaStockMovementRepository.save(mapper.toEntity(movement));
        return movement;
    }

    @Override
    public void appendAll(List<StockMovement> movements) {
        if (movements == null || movements.isEmpty()) return;
        List<StockMovementEntity> entities = new ArrayList<>();
        for (StockMovement m : movements) {
            entities.add(mapper.toEntity(m));
        }
        jpaStockMovementRepository.saveAll(entities);
    }
}
