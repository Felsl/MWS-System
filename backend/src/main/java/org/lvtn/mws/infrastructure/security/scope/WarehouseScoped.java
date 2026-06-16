package org.lvtn.mws.infrastructure.security.scope;

import java.lang.annotation.*;

/**
 * Đánh dấu một method UseCase/Service cần áp dụng Data Scope Restriction.
 * Khi method này được gọi, AOP sẽ tự động nạp danh sách warehouse_id
 * mà user hiện tại được phép truy cập vào WarehouseScopeContext.
 *
 * Cách dùng:
 *   @WarehouseScoped
 *   public List<Warehouse> execute() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WarehouseScoped {
}
