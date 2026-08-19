package org.lvtn.mws.infrastructure.persistence.repository.goodsreceipt;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.GoodsReceiptDetail;
import org.lvtn.mws.domain.repository.IGoodsReceiptDetailRepository;
import org.lvtn.mws.infrastructure.persistence.mapper.GoodsReceiptDetailInfraMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoodsReceiptDetailRepositoryImpl implements IGoodsReceiptDetailRepository {

    private final JpaGoodsReceiptDetailRepository jpa;
    private final GoodsReceiptDetailInfraMapper mapper;

    @Override
    public GoodsReceiptDetail save(GoodsReceiptDetail detail) {
        return mapper.toDomain(jpa.save(mapper.toEntity(detail)));
    }

    @Override
    public void saveAll(List<GoodsReceiptDetail> details) {
        jpa.saveAll(mapper.toEntityList(details));
    }

    @Override
    public List<GoodsReceiptDetail> findByGrnId(String grnId) {
        return mapper.toDomainList(jpa.findByGrnId(grnId));
    }

    @Override
    public Optional<GoodsReceiptDetail> findById(String id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Map<String, String> findBatchSuppliersByProductAndWarehouse(String productId, String warehouseId) {
        Map<String, String> result = new HashMap<>();
        Map<String, LocalDateTime> latest = new HashMap<>();
        for (Object[] row : jpa.findBatchSupplierRows(productId, warehouseId)) {
            String batchNumber = (String) row[0];
            if (batchNumber == null) continue;
            String supplierName = (String) row[1];
            LocalDateTime receivedAt = (LocalDateTime) row[2];
            LocalDateTime prev = latest.get(batchNumber);
            // Cùng batchNumber từ nhiều phiếu nhập: giữ nhà cung cấp của lần nhập gần nhất.
            boolean take = !result.containsKey(batchNumber)
                    || (receivedAt != null && (prev == null || receivedAt.isAfter(prev)));
            if (take) {
                result.put(batchNumber, supplierName);
                latest.put(batchNumber, receivedAt);
            }
        }
        return result;
    }

    @Override
    public Map<String, String> findProductSuppliersByWarehouse(String warehouseId) {
        Map<String, java.util.LinkedHashSet<String>> byProduct = new HashMap<>();
        for (Object[] row : jpa.findProductSupplierRows(warehouseId)) {
            String productId = (String) row[0];
            String supplierName = (String) row[1];
            if (productId == null || supplierName == null) continue;
            byProduct.computeIfAbsent(productId, k -> new java.util.LinkedHashSet<>()).add(supplierName);
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, java.util.LinkedHashSet<String>> e : byProduct.entrySet()) {
            result.put(e.getKey(), String.join(" / ", e.getValue()));
        }
        return result;
    }
}
