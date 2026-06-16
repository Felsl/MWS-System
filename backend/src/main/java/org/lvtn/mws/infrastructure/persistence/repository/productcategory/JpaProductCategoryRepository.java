package org.lvtn.mws.infrastructure.persistence.repository.productcategory;

import org.lvtn.mws.infrastructure.persistence.entity.ProductCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaProductCategoryRepository extends JpaRepository<ProductCategoryEntity, String> {

    Optional<ProductCategoryEntity> findByIdAndDeletedAtIsNull(String id);
    Optional<ProductCategoryEntity> findByCodeAndDeletedAtIsNull(String code);
    List<ProductCategoryEntity> findAllByDeletedAtIsNull();
    boolean existsByCodeAndDeletedAtIsNull(String code);
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, String id);
}