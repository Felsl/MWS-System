package org.lvtn.mws.application.usecases.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.model.Product.Unit;
import org.lvtn.mws.domain.service.ProductDomainService;
import org.lvtn.mws.infrastructure.service.IdGeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    private final ProductDomainService domainService;
    private final IdGeneratorService idGenerator;

    @Transactional
    public Product execute(String categoryId, String sku, String barcode, String name,
                           String description, BigDecimal price, BigDecimal costPrice,
                           Unit unit, int safetyStock, BigDecimal weight,
                           BigDecimal volume, boolean hazardousFlag) {
        return domainService.create(idGenerator.generate(), categoryId, sku, barcode,
                name, description, price, costPrice, unit, safetyStock, weight, volume, hazardousFlag);
    }
}
