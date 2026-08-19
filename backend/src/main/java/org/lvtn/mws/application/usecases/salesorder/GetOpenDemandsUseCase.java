package org.lvtn.mws.application.usecases.salesorder;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.StockDemand;
import org.lvtn.mws.domain.repository.IProductRepository;
import org.lvtn.mws.domain.repository.ISalesOrderRepository;
import org.lvtn.mws.domain.repository.IStockDemandRepository;
import org.lvtn.mws.domain.repository.ISupplierRepository;
import org.lvtn.mws.domain.repository.IWarehouseRepository;
import org.lvtn.mws.interfaces.dto.response.salesorder.StockDemandResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [Bán vượt tồn] Danh sách nhu cầu nhập OPEN (cho Dashboard bộ phận mua). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOpenDemandsUseCase {

    private final IStockDemandRepository demandRepository;
    private final ISalesOrderRepository salesOrderRepository;
    private final IProductRepository productRepository;
    private final IWarehouseRepository warehouseRepository;
    private final ISupplierRepository supplierRepository;

    public List<StockDemandResponse> execute() {
        Map<String, String> soNumberCache = new HashMap<>();
        Map<String, String> productCache = new HashMap<>();
        Map<String, String> warehouseCache = new HashMap<>();
        Map<String, String> supplierCache = new HashMap<>();
        return demandRepository.findAllOpen().stream().map(d -> {
            String soNumber = soNumberCache.computeIfAbsent(d.getSoId(), id ->
                    salesOrderRepository.findById(id).map(so -> so.getSoNumber()).orElse(null));
            String productName = productCache.computeIfAbsent(d.getProductId(), id ->
                    productRepository.findById(id).map(p -> p.getName()).orElse(null));
            String warehouseName = warehouseCache.computeIfAbsent(d.getWarehouseId(), id ->
                    warehouseRepository.findById(id).map(w -> w.getName()).orElse(null));
            String supplierName = d.getSupplierId() == null ? null
                    : supplierCache.computeIfAbsent(d.getSupplierId(), id ->
                        supplierRepository.findById(id).map(su -> su.getName()).orElse(null));
            return new StockDemandResponse(
                    d.getId(), d.getSoId(), soNumber,
                    d.getProductId(), productName,
                    d.getWarehouseId(), warehouseName,
                    d.getSupplierId(), supplierName,
                    d.getQuantityShort(), d.getCreatedAt());
        }).toList();
    }

}
