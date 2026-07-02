package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.NewTransferLine;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateTransferOrderUseCase {

    private final TransferOrderDomainService transferOrderDomainService;
    private final WarehouseAccessGuard warehouseAccessGuard;

    public TransferOrder execute(String fromWarehouseId,
                                 String toWarehouseId,
                                 String createdBy,
                                 List<NewTransferLine> lines) {
        // A2: người tạo điều chuyển phải có quyền ở kho NGUỒN (nơi hàng đi ra).
        warehouseAccessGuard.check(fromWarehouseId);
        return transferOrderDomainService.createTransferOrder(fromWarehouseId, toWarehouseId, createdBy, lines);
    }
}
