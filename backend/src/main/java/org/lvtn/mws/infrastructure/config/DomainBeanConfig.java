package org.lvtn.mws.infrastructure.config;

import org.lvtn.mws.domain.repository.ICarrierRepository;
import org.lvtn.mws.domain.repository.ICustomerRepository;
import org.lvtn.mws.domain.repository.ISupplierRepository;
import org.lvtn.mws.domain.repository.IIdGenerator;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IInventoryRepository;
import org.lvtn.mws.domain.repository.INotificationPort;
import org.lvtn.mws.domain.repository.IShipmentRepository;
import org.lvtn.mws.domain.repository.IShippingFeeCalculator;
import org.lvtn.mws.domain.repository.IStockMovementRepository;
import org.lvtn.mws.domain.repository.ITransferOrderRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.domain.service.CarrierDomainService;
import org.lvtn.mws.domain.service.CustomerDomainService;
import org.lvtn.mws.domain.service.SupplierDomainService;
import org.lvtn.mws.domain.service.TransferOrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain service là POJO thuần Java (không có annotation Spring) nên được khai báo
 * thành bean tại đây. Ranh giới @Transactional vẫn nằm ở tầng UseCase.
 */
@Configuration
public class DomainBeanConfig {

    @Bean
    public TransferOrderDomainService transferOrderDomainService(
            ITransferOrderRepository transferRepository,
            IShipmentRepository shipmentRepository,
            ICarrierRepository carrierRepository,
            IInventoryRepository inventoryRepository,
            IInventoryBatchRepository batchRepository,
            IWarehouseRepository warehouseRepository,
            IStockMovementRepository stockMovementRepository,
            INotificationPort notificationPort,
            IShippingFeeCalculator shippingFeeCalculator,
            IIdGenerator idGenerator) {
        return new TransferOrderDomainService(
                transferRepository, shipmentRepository, carrierRepository,
                inventoryRepository, batchRepository, warehouseRepository,
                stockMovementRepository, notificationPort, shippingFeeCalculator, idGenerator);
    }

    @Bean
    public CarrierDomainService carrierDomainService(ICarrierRepository carrierRepository,
                                                     IIdGenerator idGenerator) {
        return new CarrierDomainService(carrierRepository, idGenerator);
    }

    @Bean
    public SupplierDomainService supplierDomainService(ISupplierRepository supplierRepository,
                                                       IIdGenerator idGenerator) {
        return new SupplierDomainService(supplierRepository, idGenerator);
    }

    @Bean
    public CustomerDomainService customerDomainService(ICustomerRepository customerRepository,
                                                       IIdGenerator idGenerator) {
        return new CustomerDomainService(customerRepository, idGenerator);
    }
}
