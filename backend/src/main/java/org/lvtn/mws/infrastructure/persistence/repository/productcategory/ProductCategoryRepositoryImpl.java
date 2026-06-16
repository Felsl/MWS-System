package org.lvtn.mws.infrastructure.persistence.repository.productcategory;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.repository.IProductCategoryRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.ProductCategoryInfraMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements IProductCategoryRepository {

    private final JpaProductCategoryRepository jpa;
    private final ProductCategoryInfraMapper mapper;

    @Override
    public ProductCategory save(ProductCategory category) {
        return mapper.toDomain(jpa.save(mapper.toEntity(category)));
    }

    @Override
    public Optional<ProductCategory> findById(String id) {
        return jpa.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductCategory> findByCode(String code) {
        return jpa.findByCodeAndDeletedAtIsNull(code).map(mapper::toDomain);
    }

    @Override
    public List<ProductCategory> findAllActive() {
        return jpa.findAllByDeletedAtIsNull().stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsByCodeAndDeletedAtIsNull(code);
    }

    @Override
    public boolean existsByCodeExcludingId(String id, String code) {
        return jpa.existsByCodeAndIdNotAndDeletedAtIsNull(code, id);
    }
}
