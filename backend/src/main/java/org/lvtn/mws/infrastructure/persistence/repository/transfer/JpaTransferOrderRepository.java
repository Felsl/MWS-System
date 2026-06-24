package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTransferOrderRepository extends JpaRepository<TransferOrderEntity, String> {
    List<TransferOrderEntity> findByStatus(TransferOrder.Status status);
    boolean existsByTransferNumber(String transferNumber);
}
