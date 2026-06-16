package org.lvtn.mws.application.usecases.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllProductsUseCase {
    private final ProductDomainService domainService;

    @Transactional(readOnly = true)
    public List<Product> execute() { return domainService.findAllActive(); }
}
