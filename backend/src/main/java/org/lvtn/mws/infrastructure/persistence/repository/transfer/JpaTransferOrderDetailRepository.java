package org.lvtn.mws.infrastructure.persistence.repository.transfer;

import org.lvtn.mws.infrastructure.persistence.entity.TransferOrderDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaTransferOrderDetailRepository extends JpaRepository<TransferOrderDetailEntity, String> {
    List<TransferOrderDetailEntity> findByTransferOrderId(String transferOrderId);
    void deleteByTransferOrderId(String transferOrderId);
}
