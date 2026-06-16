package org.lvtn.mws.infrastructure.config;

import org.lvtn.mws.domain.repository.*;
import org.lvtn.mws.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public UserDomainService userDomainService(IUserRepository userRepository,
                                               IRoleRepository roleRepository,
                                               IWarehouseRepository warehouseRepository) {
        return new UserDomainService(userRepository, roleRepository, warehouseRepository);
    }

    @Bean
    public RoleDomainService roleDomainService(IRoleRepository roleRepository,
                                               IPermissionRepository permissionRepository) {
        return new RoleDomainService(roleRepository, permissionRepository);
    }

    @Bean
    public PermissionDomainService permissionDomainService(IPermissionRepository permissionRepository) {
        return new PermissionDomainService(permissionRepository);
    }

    @Bean
    public WarehouseDomainService warehouseDomainService(IWarehouseRepository warehouseRepository,
                                                         IBinLocationRepository binLocationRepository) {
        return new WarehouseDomainService(warehouseRepository, binLocationRepository);
    }

    @Bean
    public ProductCategoryDomainService productCategoryDomainService(
            IProductCategoryRepository categoryRepository) {
        return new ProductCategoryDomainService(categoryRepository);
    }

    @Bean
    public ProductDomainService productDomainService(
            IProductRepository productRepository,
            IProductCategoryRepository categoryRepository) {
        return new ProductDomainService(productRepository, categoryRepository);
    }

    @Bean
    public InventoryDomainService inventoryDomainService(
            IInventoryRepository inventoryRepository,
            IInventoryBatchRepository batchRepository,
            IProductRepository productRepository) {
        return new InventoryDomainService(inventoryRepository, batchRepository, productRepository);
    }
}