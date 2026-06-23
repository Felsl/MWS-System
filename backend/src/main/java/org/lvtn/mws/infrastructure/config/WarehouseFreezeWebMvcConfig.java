package org.lvtn.mws.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.infrastructure.freeze.WarehouseFreezeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Đăng ký WarehouseFreezeInterceptor cho các nhóm API biến động tồn.
 * Lưu ý: KHÔNG áp cho /api/v1/stocktakes/** và /api/v1/adjustment-vouchers/**
 * để không tự chặn chính module kiểm kê.
 */
@Configuration
@RequiredArgsConstructor
public class WarehouseFreezeWebMvcConfig implements WebMvcConfigurer {

    private final WarehouseFreezeInterceptor warehouseFreezeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(warehouseFreezeInterceptor)
                .addPathPatterns(
                        "/api/v1/goods-receipts/**",
                        "/api/v1/sales-orders/**",
                        "/api/v1/shipments/**",
                        "/api/v1/picking-lists/**",
                        "/api/v1/transfer-orders/**");
    }
}
