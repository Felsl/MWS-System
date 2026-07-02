package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface JpaTransferOrderRepository
        extends JpaRepository<TransferOrderEntity, String>,
                JpaSpecificationExecutor<TransferOrderEntity> {
    List<TransferOrderEntity> findByStatus(TransferOrder.Status status);
    boolean existsByTransferNumber(String transferNumber);
}
