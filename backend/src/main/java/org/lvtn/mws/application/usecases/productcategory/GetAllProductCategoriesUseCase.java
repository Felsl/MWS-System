package org.lvtn.mws.application.usecases.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.service.ProductCategoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllProductCategoriesUseCase {
    private final ProductCategoryDomainService domainService;

    @Transactional(readOnly = true)
    public List<ProductCategory> execute() {
        return domainService.findAllActive();
    }
}
