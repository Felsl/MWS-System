package org.lvtn.mws.application.usecases.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.service.ProductCategoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductCategoryByIdUseCase {
    private final ProductCategoryDomainService domainService;

    @Transactional(readOnly = true)
    public ProductCategory execute(String id) {
        return domainService.findById(id);
    }
}
