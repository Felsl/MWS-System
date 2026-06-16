package org.lvtn.mws.infrastructure.persistence.repository.product;

import org.lvtn.mws.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {

    Optional<ProductEntity> findByIdAndDeletedAtIsNull(String id);
    Optional<ProductEntity> findBySkuAndDeletedAtIsNull(String sku);
    List<ProductEntity> findAllByDeletedAtIsNull();
    boolean existsBySkuAndDeletedAtIsNull(String sku);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p WHERE p.id <> :id AND p.sku = :sku AND p.deletedAt IS NULL")
    Boolean existsBySkuExcludingId(@Param("id") String id, @Param("sku") String sku);

    @Query("SELECT p FROM ProductEntity p WHERE p.deletedAt IS NULL AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(p.sku)  LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<ProductEntity> searchActive(@Param("kw") String keyword);
}