package org.lvtn.mws.application.usecases.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.service.ProductDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProductUseCase {
    private final ProductDomainService domainService;

    @Transactional
    public void execute(String id) { domainService.delete(id); }
}
