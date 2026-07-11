package org.lvtn.mws.application.usecases.transfer;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.TransferOrder;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllTransferOrdersUseCase {

    private final TransferOrderDomainService transferOrderDomainService;

    /** B4: tìm kiếm + phân trang phiếu điều chuyển, lọc theo kho user được phép (nguồn hoặc đích). */
    @WarehouseScoped
    public PageResult<TransferOrder> execute(String keyword, String status, int page, int size, String sortBy, String sortDir) {
        return transferOrderDomainService.search(keyword, status, new PageQuery(page, size, sortBy, sortDir));
    }
}
