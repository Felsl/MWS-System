package org.lvtn.mws.infrastructure.capacity;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.BinCapacityExceededException;
import org.lvtn.mws.domain.model.BinOccupancy;
import org.lvtn.mws.domain.model.Product;
import org.lvtn.mws.domain.repository.IInventoryBatchRepository;
import org.lvtn.mws.domain.repository.IProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [PA1] Chốt chặn sức chứa ô kệ: khi ĐƯA HÀNG VÀO ô kệ (nhập kho, tạo lô tay), kiểm tra
 * (đang chiếm + sắp thêm) không vượt giới hạn cấu hình (kg / thể tích). Giới hạn 0 = bỏ qua.
 * Occupied tính sống, KHÔNG lưu DB. Gọi trong USE CASE ghi tồn (đúng tinh thần guard).
 */
@Component
@RequiredArgsConstructor
public class BinCapacityGuard {

    private final IInventoryBatchRepository inventoryBatchRepository;
    private final IProductRepository productRepository;

    @Value("${warehouse.bin.max-weight-kg:0}") private BigDecimal maxWeightKg;
    @Value("${warehouse.bin.max-volume-m3:0}") private BigDecimal maxVolumeM3;

    /** Một dòng đưa vào một ô kệ. */
    public record Incoming(String productId, String binLocationId, int quantity) {}

    public void assertBatchFits(String warehouseId, String binLocationId, String productId, int quantity) {
        assertFits(warehouseId, List.of(new Incoming(productId, binLocationId, quantity)));
    }

    public void assertFits(String warehouseId, List<Incoming> incomings) {
        boolean limitW = maxWeightKg != null && maxWeightKg.signum() > 0;
        boolean limitV = maxVolumeM3 != null && maxVolumeM3.signum() > 0;
        if ((!limitW && !limitV) || incomings == null || incomings.isEmpty()) return;

        Map<String, BinOccupancy> occ = new HashMap<>();
        for (BinOccupancy o : inventoryBatchRepository.sumOccupancyByWarehouse(warehouseId)) {
            occ.put(o.binLocationId(), o);
        }
        // Cộng dồn lượng sắp thêm theo ô kệ (nhiều dòng cùng một ô).
        Map<String, BigDecimal[]> addByBin = new HashMap<>(); // [weight, volume]
        for (Incoming in : incomings) {
            if (in.binLocationId() == null) continue;
            BigDecimal w = BigDecimal.ZERO, v = BigDecimal.ZERO;
            Product p = productRepository.findById(in.productId()).orElse(null);
            if (p != null) {
                if (p.getWeight() != null) w = p.getWeight();
                if (p.getVolume() != null) v = p.getVolume();
            }
            BigDecimal qty = BigDecimal.valueOf(in.quantity());
            BigDecimal[] acc = addByBin.computeIfAbsent(in.binLocationId(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            acc[0] = acc[0].add(w.multiply(qty));
            acc[1] = acc[1].add(v.multiply(qty));
        }
        for (Map.Entry<String, BigDecimal[]> e : addByBin.entrySet()) {
            BinOccupancy cur = occ.get(e.getKey());
            BigDecimal projW = (cur != null ? cur.weight() : BigDecimal.ZERO).add(e.getValue()[0]);
            BigDecimal projV = (cur != null ? cur.volume() : BigDecimal.ZERO).add(e.getValue()[1]);
            if (limitW && projW.compareTo(maxWeightKg) > 0) {
                throw new BinCapacityExceededException(
                        "Ô kệ " + e.getKey() + " vượt tải trọng: cần " + projW + " kg / giới hạn " + maxWeightKg + " kg");
            }
            if (limitV && projV.compareTo(maxVolumeM3) > 0) {
                throw new BinCapacityExceededException(
                        "Ô kệ " + e.getKey() + " vượt thể tích: cần " + projV + " m³ / giới hạn " + maxVolumeM3 + " m³");
            }
        }
    }
}
