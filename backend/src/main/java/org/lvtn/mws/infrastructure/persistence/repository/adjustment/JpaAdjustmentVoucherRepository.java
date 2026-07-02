package org.lvtn.mws.infrastructure.persistence.repository.adjustment;

import org.lvtn.mws.infrastructure.persistence.entity.AdjustmentVoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JpaAdjustmentVoucherRepository
        extends JpaRepository<AdjustmentVoucherEntity, String>,
                JpaSpecificationExecutor<AdjustmentVoucherEntity> {
    List<AdjustmentVoucherEntity> findBySessionId(String sessionId);
    boolean existsByVoucherNumber(String voucherNumber);
    long countByVoucherNumberStartingWith(String prefix);
}
