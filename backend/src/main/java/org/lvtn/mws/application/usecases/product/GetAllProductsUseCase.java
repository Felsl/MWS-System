package org.lvtn.mws.application.usecases.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.common.PageQuery;
import org.lvtn.mws.domain.common.PageResult;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAllProductsUseCase {
    private final ProductDomainService domainService;

    /** B4: danh sách sản phẩm có tìm kiếm (name/sku/barcode) + phân trang. */
    @Transactional(readOnly = true)
    public PageResult<Product> execute(String keyword, int page, int size, String sortBy, String sortDir) {
        return domainService.searchPaged(keyword, new PageQuery(page, size, sortBy, sortDir));
    }
}
