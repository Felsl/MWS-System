package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.ProductCategory;
import java.util.List;
import java.util.Optional;

public interface IProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findById(String id);
    Optional<ProductCategory> findByCode(String code);
    List<ProductCategory> findAllActive();
    boolean existsByCode(String code);
    boolean existsByCodeExcludingId(String id, String code);
}
