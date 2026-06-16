package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.model.Product.Unit;
import org.lvtn.mws.domain.repository.IProductCategoryRepository;
import org.lvtn.mws.domain.repository.IProductRepository;
import java.math.BigDecimal;
import java.util.List;

public class ProductDomainService {

    private final IProductRepository productRepository;
    private final IProductCategoryRepository categoryRepository;

    public ProductDomainService(IProductRepository productRepository,
                                IProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Product create(String id, String categoryId, String sku, String barcode,
                          String name, String description, BigDecimal price,
                          BigDecimal costPrice, Unit unit, int safetyStock,
                          BigDecimal weight, BigDecimal volume, boolean hazardousFlag) {
        validateCategoryExists(categoryId);
        if (productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + sku);
        }
        Product product = new Product.Builder()
                .id(id).categoryId(categoryId).sku(sku).barcode(barcode)
                .name(name).description(description).price(price).costPrice(costPrice)
                .unit(unit).safetyStock(safetyStock).weight(weight).volume(volume)
                .hazardousFlag(hazardousFlag).build();
        return productRepository.save(product);
    }

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với id: " + id));
    }

    public List<Product> findAllActive() { return productRepository.findAllActive(); }

    public List<Product> search(String keyword) { return productRepository.searchActive(keyword); }

    public Product update(String id, String categoryId, String sku, String barcode,
                          String name, String description, BigDecimal price,
                          BigDecimal costPrice, Unit unit, int safetyStock,
                          BigDecimal weight, BigDecimal volume, boolean hazardousFlag) {
        validateCategoryExists(categoryId);
        if (productRepository.existsBySkuExcludingId(id, sku)) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + sku);
        }
        Product product = findById(id);
        product.update(categoryId, sku, barcode, name, description, price,
                costPrice, unit, safetyStock, weight, volume, hazardousFlag);
        return productRepository.save(product);
    }

    public void delete(String id) {
        Product product = findById(id);
        product.delete();
        productRepository.save(product);
    }

    private void validateCategoryExists(String categoryId) {
        if (categoryId == null) return;
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với id: " + categoryId));
    }
}
