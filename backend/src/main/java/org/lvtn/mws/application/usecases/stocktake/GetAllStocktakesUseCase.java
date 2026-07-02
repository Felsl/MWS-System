package org.lvtn.mws.application.usecases.stocktake;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.StocktakeSession;
import org.lvtn.mws.domain.service.StocktakeDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllStocktakesUseCase {

    private final StocktakeDomainService stocktakeDomainService;

    /** B4: tìm kiếm theo trạng thái + phân trang, lọc theo kho user được phép. */
    @WarehouseScoped
    public PageResult<StocktakeSession> execute(String status, int page, int size) {
        return stocktakeDomainService.search(status, new PageQuery(page, size));
    }
}
