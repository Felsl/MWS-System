package org.lvtn.mws.domain.service;

import org.lvtn.mws.domain.model.ProductCategory;
import org.lvtn.mws.domain.repository.IProductCategoryRepository;
import java.util.List;

public class ProductCategoryDomainService {

    private final IProductCategoryRepository categoryRepository;

    public ProductCategoryDomainService(IProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ProductCategory create(String id, String code, String name, String description) {
        if (categoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Mã danh mục đã tồn tại: " + code);
        }
        ProductCategory category = new ProductCategory.Builder()
                .id(id).code(code).name(name).description(description).build();
        return categoryRepository.save(category);
    }

    public ProductCategory findById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với id: " + id));
    }

    public List<ProductCategory> findAllActive() {
        return categoryRepository.findAllActive();
    }

    public ProductCategory update(String id, String code, String name, String description) {
        if (categoryRepository.existsByCodeExcludingId(id, code)) {
            throw new IllegalArgumentException("Mã danh mục đã tồn tại: " + code);
        }
        ProductCategory category = findById(id);
        category.update(code, name, description);
        return categoryRepository.save(category);
    }

    public void delete(String id) {
        ProductCategory category = findById(id);
        category.delete();
        categoryRepository.save(category);
    }
}
