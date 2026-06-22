package org.lvtn.mws.infrastructure.persistence.repository.picking;

import org.lvtn.mws.infrastructure.persistence.entity.PickingListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaPickingListRepository extends JpaRepository<PickingListEntity, String> {
    Optional<PickingListEntity> findBySoId(String soId);

    @Query("SELECT DISTINCT pl FROM PickingListEntity pl JOIN pl.details d WHERE d.id = :detailId")
    Optional<PickingListEntity> findByDetailId(@Param("detailId") String detailId);
}
