package org.lvtn.mws.infrastructure.freeze;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Lớp phòng vệ "tốt nhất có thể" (best-effort) ở tầng web: với các request ghi
 * (POST/PUT/PATCH/DELETE) trên các nhóm API biến động tồn, NẾU request có tham số
 * warehouseId thì chặn khi kho đang đóng băng.
 *
 * HẠN CHẾ: nhiều luồng (vd /{id}/complete) không mang warehouseId nên interceptor
 * không thể chặn — vì vậy điểm thực thi CHÍNH vẫn là StocktakeFreezeGuard gọi trong
 * use case. Interceptor này chỉ là lớp chặn sớm bổ sung.
 */
@Component
@RequiredArgsConstructor
public class WarehouseFreezeInterceptor implements HandlerInterceptor {

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final StocktakeFreezeGuard freezeGuard;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (WRITE_METHODS.contains(request.getMethod())) {
            String warehouseId = request.getParameter("warehouseId");
            if (warehouseId != null && !warehouseId.isBlank()) {
                freezeGuard.assertNotFrozen(warehouseId); // ném WarehouseFrozenException -> 409
            }
        }
        return true;
    }
}
