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
    public org.lvtn.mws.domain.common.PageResult<Product> search(
            String keyword, org.lvtn.mws.domain.common.PageQuery pageQuery) {
        org.springframework.data.jpa.domain.Specification<org.lvtn.mws.infrastructure.persistence.entity.ProductEntity> spec =
                (root, q, cb) -> cb.isNull(root.get("deletedAt"));
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("sku")), like),
                    cb.like(cb.lower(root.get("barcode")), like)));
        }
        var pageable = org.springframework.data.domain.PageRequest.of(
                pageQuery.page(), pageQuery.size(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name"));
        var pageEntity = jpa.findAll(spec, pageable);
        return new org.lvtn.mws.domain.common.PageResult<>(
                pageEntity.getContent().stream().map(mapper::toDomain).collect(java.util.stream.Collectors.toList()),
                pageQuery.page(), pageQuery.size(), pageEntity.getTotalElements());
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