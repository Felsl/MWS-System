package org.lvtn.mws.infrastructure.security.scope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AOP Aspect tự động inject Data Scope cho các method được đánh dấu @WarehouseScoped.
 *
 * Luồng xử lý:
 * 1. Lấy username từ SecurityContext
 * 2. Truy vấn danh sách warehouse_id user được phép từ user_warehouse_access
 * 3. Nạp vào WarehouseScopeContext (ThreadLocal)
 * 4. Thực thi method gốc — các query bên trong tự động đọc context để filter
 * 5. Clear context sau khi xong (finally)
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class WarehouseScopeAspect {

    private final IUserWarehouseAccessRepository warehouseAccessRepository;

    @Around("@annotation(org.lvtn.mws.infrastructure.security.scope.WarehouseScoped)")
    public Object applyWarehouseScope(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return joinPoint.proceed();
        }

        String username = authentication.getName();

        try {
            // Lấy userId từ UserDetails nếu cần — ở đây dùng username để tìm access
            // Trong thực tế nên cache lại để tránh hit DB mỗi request
            List<String> allowedWarehouseIds = warehouseAccessRepository
                    .findWarehouseIdsByUsername(username);

            WarehouseScopeContext.set(allowedWarehouseIds);
            log.debug("WarehouseScope applied for user '{}': {} warehouses",
                    username, allowedWarehouseIds.size());

            return joinPoint.proceed();

        } finally {
            WarehouseScopeContext.clear();
        }
    }
}
