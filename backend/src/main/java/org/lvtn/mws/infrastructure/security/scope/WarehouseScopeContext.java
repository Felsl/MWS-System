package org.lvtn.mws.infrastructure.security.scope;

import java.util.Collections;
import java.util.List;

/**
 * ThreadLocal context lưu danh sách warehouse_id user hiện tại được phép truy cập.
 *
 * Vòng đời: được set bởi WarehouseScopeAspect trước khi method thực thi,
 * và clear ngay sau khi method kết thúc (cả trường hợp exception).
 *
 * Các tầng Repository/Service đọc context này để tự động inject
 * điều kiện AND warehouse_id IN (...) vào query.
 */
public final class WarehouseScopeContext {

    private static final ThreadLocal<List<String>> ALLOWED_IDS =
            ThreadLocal.withInitial(Collections::emptyList);

    private WarehouseScopeContext() {}

    public static void set(List<String> warehouseIds) {
        ALLOWED_IDS.set(warehouseIds != null ? warehouseIds : Collections.emptyList());
    }

    public static List<String> get() {
        return ALLOWED_IDS.get();
    }

    public static boolean isActive() {
        return !ALLOWED_IDS.get().isEmpty();
    }

    public static void clear() {
        ALLOWED_IDS.remove();
    }
}
