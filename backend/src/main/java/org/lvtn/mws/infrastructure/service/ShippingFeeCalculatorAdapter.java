package org.lvtn.mws.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.repository.IShippingFeeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Đọc cấu hình JSON shipping_fee_rule và tính phí ship dự kiến.
 *
 * Quy ước JSON ví dụ:
 *   { "baseFee": 20000, "perUnitFee": 1500 }
 * => fee = baseFee + perUnitFee * totalQuantity
 *
 * GHI CHÚ: việc tính theo trọng lượng/khoảng cách thật cần dữ liệu cân nặng sản phẩm
 * và toạ độ kho (chưa wire trong phạm vi module này) -> hiện dùng số lượng làm proxy.
 */
@Component
@RequiredArgsConstructor
public class ShippingFeeCalculatorAdapter implements IShippingFeeCalculator {

    private static final Logger log = LoggerFactory.getLogger(ShippingFeeCalculatorAdapter.class);
    private final ObjectMapper objectMapper;

    @Override
    public BigDecimal estimate(String shippingFeeRuleJson,
                               int totalQuantity,
                               String fromWarehouseId,
                               String toWarehouseId) {
        BigDecimal baseFee = BigDecimal.ZERO;
        BigDecimal perUnitFee = BigDecimal.ZERO;

        if (shippingFeeRuleJson != null && !shippingFeeRuleJson.isBlank()) {
            try {
                JsonNode node = objectMapper.readTree(shippingFeeRuleJson);
                if (node.has("baseFee"))    baseFee = node.get("baseFee").decimalValue();
                if (node.has("perUnitFee")) perUnitFee = node.get("perUnitFee").decimalValue();
            } catch (Exception e) {
                log.warn("shipping_fee_rule không hợp lệ, dùng phí 0. rule={}", shippingFeeRuleJson, e);
            }
        }

        BigDecimal fee = baseFee.add(perUnitFee.multiply(BigDecimal.valueOf(totalQuantity)));
        log.info("Ước tính phí ship: {} (base={}, perUnit={}, qty={}, {} -> {})",
                fee, baseFee, perUnitFee, totalQuantity, fromWarehouseId, toWarehouseId);
        return fee;
    }
}
