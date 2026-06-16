package org.lvtn.mws.application.usecases.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.service.ProductCategoryDomainService;
import org.lvtn.mws.infrastructure.service.IdGeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateProductCategoryUseCase {
    private final ProductCategoryDomainService domainService;
    private final IdGeneratorService idGenerator;

    @Transactional
    public ProductCategory execute(String code, String name, String description) {
        return domainService.create(idGenerator.generate(), code, name, description);
    }
}
