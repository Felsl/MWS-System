package org.lvtn.mws.domain.repository;

import java.math.BigDecimal;

/**
 * PORT — đọc cấu hình JSON shipping_fee_rule của carrier và tính phí ship dự kiến.
 * Domain thuần Java không parse JSON nên đẩy việc này xuống tầng hạ tầng (Jackson).
 */
public interface IShippingFeeCalculator {

    /**
     * @param shippingFeeRuleJson chuỗi JSON cấu hình của carrier (có thể null)
     * @param totalQuantity       tổng số lượng kiện hàng (proxy cho khối lượng)
     * @param fromWarehouseId     kho nguồn (dùng để ước lượng khoảng cách - TODO)
     * @param toWarehouseId       kho đích
     * @return phí ship dự kiến
     */
    BigDecimal estimate(String shippingFeeRuleJson,
                        int totalQuantity,
                        String fromWarehouseId,
                        String toWarehouseId);
}
