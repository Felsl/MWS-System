package org.lvtn.mws.infrastructure.config;

import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.domain.repository.IGoodsReceiptRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.IPurchaseOrderDetailRepository;
import org.lvtn.mws.domain.repository.IPurchaseOrderRepository;
import org.lvtn.mws.domain.service.GoodsReceiptDomainService;
import org.lvtn.mws.domain.service.InventoryDomainService;
import org.lvtn.mws.domain.service.PurchaseOrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the framework-agnostic domain services as Spring beans by wiring
 * their repository/port dependencies. {@link InventoryDomainService} is expected
 * to be provided as a bean by the Stage-2 inventory configuration.
 */
@Configuration
public class InboundDomainServiceConfig {

    @Bean
    public PurchaseOrderDomainService purchaseOrderDomainService(
            IPurchaseOrderRepository poRepository,
            IPurchaseOrderDetailRepository poDetailRepository,
            IIdGenerator idGenerator) {
        return new PurchaseOrderDomainService(poRepository, poDetailRepository, idGenerator);
    }

    @Bean
    public GoodsReceiptDomainService goodsReceiptDomainService(
            IGoodsReceiptRepository grnRepository,
            IGoodsReceiptDetailRepository grnDetailRepository,
            IPurchaseOrderRepository poRepository,
            IPurchaseOrderDetailRepository poDetailRepository,
            InventoryDomainService inventoryDomainService,
            IInventoryRepository inventoryRepository,
            IInventoryBatchRepository batchRepository,
            IIdGenerator idGenerator) {
        return new GoodsReceiptDomainService(
                grnRepository, grnDetailRepository, poRepository, poDetailRepository,
                inventoryDomainService, inventoryRepository,
                batchRepository, idGenerator);
    }
}
