package org.lvtn.mws.application.usecases.adjustment;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.AdjustmentVoucher;
import org.lvtn.mws.domain.service.AdjustmentDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllAdjustmentVouchersUseCase {

    private final AdjustmentDomainService adjustmentDomainService;

    /** B4: tìm kiếm + phân trang phiếu điều chỉnh, lọc theo kho user được phép. */
    @WarehouseScoped
    public PageResult<AdjustmentVoucher> execute(String keyword, String status, int page, int size, String sortBy, String sortDir) {
        return adjustmentDomainService.search(keyword, status, new PageQuery(page, size, sortBy, sortDir));
    }
}
