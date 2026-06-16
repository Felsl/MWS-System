package org.lvtn.mws.application.usecases.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.service.ProductCategoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductCategoryUseCase {
    private final ProductCategoryDomainService domainService;

    @Transactional
    public ProductCategory execute(String id, String code, String name, String description) {
        return domainService.update(id, code, name, description);
    }
}
