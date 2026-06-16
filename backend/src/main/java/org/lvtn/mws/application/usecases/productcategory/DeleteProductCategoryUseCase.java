package org.lvtn.mws.application.usecases.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.ProductCategoryDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProductCategoryUseCase {
    private final ProductCategoryDomainService domainService;

    @Transactional
    public void execute(String id) {
        domainService.delete(id);
    }
}
