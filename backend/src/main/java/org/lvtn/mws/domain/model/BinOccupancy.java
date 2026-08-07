package org.lvtn.mws.domain.model;

import java.math.BigDecimal;

/**
 * Mức đang chiếm của một ô kệ, tính sống từ tồn kho (KHÔNG lưu DB):
 * weight = Σ(số lượng × products.weight), volume = Σ(số lượng × products.volume).
 * Đơn vị theo đúng đơn vị của products.weight / products.volume.
 */
public record BinOccupancy(String binLocationId, BigDecimal weight, BigDecimal volume) {}
