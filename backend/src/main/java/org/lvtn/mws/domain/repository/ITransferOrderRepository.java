package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.TransferOrder;

import java.util.List;
import java.util.Optional;

public interface ITransferOrderRepository {

    /** Lưu phiếu kèm toàn bộ dòng chi tiết (insert/update + đồng bộ details). */
    TransferOrder save(TransferOrder transferOrder);

    Optional<TransferOrder> findById(String id);

    List<TransferOrder> findAll();

    List<TransferOrder> findByStatus(TransferOrder.Status status);

    boolean existsByTransferNumber(String transferNumber);
}
