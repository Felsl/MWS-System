package org.lvtn.mws.domain.repository;

import org.lvtn.mws.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface IProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    Optional<Product> findBySku(String sku);
    List<Product> findAllActive();
    List<Product> searchActive(String keyword);
    /** [B4] Tìm kiếm (name/sku/barcode) + phân trang, chỉ sản phẩm chưa xoá mềm. */
    org.lvtn.mws.domain.common.PageResult<Product> search(
            String keyword, org.lvtn.mws.domain.common.PageQuery pageQuery);
    boolean existsBySku(String sku);
    boolean existsBySkuExcludingId(String id, String sku);
}
