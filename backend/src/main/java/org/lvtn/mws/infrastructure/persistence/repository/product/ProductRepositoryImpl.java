package org.lvtn.mws.infrastructure.persistence.repository.product;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.repository.IProductRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.ProductInfraMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements IProductRepository {

    private final JpaProductRepository jpa;
    private final ProductInfraMapper mapper;

    @Override
    public Product save(Product product) {
        return mapper.toDomain(jpa.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(String id) {
        return jpa.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpa.findBySkuAndDeletedAtIsNull(sku).map(mapper::toDomain);
    }

    @Override
    public List<Product> findAllActive() {
        return jpa.findAllByDeletedAtIsNull().stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Product> searchActive(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();
        return jpa.searchActive(keyword.trim()).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpa.existsBySkuAndDeletedAtIsNull(sku);
    }

    @Override
    public boolean existsBySkuExcludingId(String id, String sku) {
        Boolean result = jpa.existsBySkuExcludingId(id, sku);
        return Boolean.TRUE.equals(result);
    }
}