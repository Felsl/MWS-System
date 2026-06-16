package org.lvtn.mws.application.usecases.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {
    private final ProductDomainService domainService;

    @Transactional(readOnly = true)
    public Product execute(String id) { return domainService.findById(id); }
}
