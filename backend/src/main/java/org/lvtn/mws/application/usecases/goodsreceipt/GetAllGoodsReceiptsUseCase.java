package org.lvtn.mws.application.usecases.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.GoodsReceipt;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.lvtn.mws.infrastructure.security.scope.WarehouseScoped;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllGoodsReceiptsUseCase {

    private final GoodsReceiptDomainService goodsReceiptDomainService;

    /** B4: tìm kiếm + phân trang phiếu nhập, đã lọc theo kho user được phép (A2). */
    @WarehouseScoped
    public PageResult<GoodsReceipt> execute(String keyword, String status, int page, int size) {
        return goodsReceiptDomainService.search(keyword, status, new PageQuery(page, size));
    }
}
