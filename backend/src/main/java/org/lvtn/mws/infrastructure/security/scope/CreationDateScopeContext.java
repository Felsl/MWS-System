package org.lvtn.mws.infrastructure.security.scope;

import java.time.LocalDateTime;

/**
 * ThreadLocal lưu MỐC NGÀY TẠO tài khoản của user hiện tại.
 *
 * <p>Ý nghĩa: user chỉ được xem dữ liệu (PO/SO/Transfer/GRN/Stocktake/Adjustment/
 * StockMovement) được tạo TỪ thời điểm tài khoản của họ được tạo trở về sau.
 * Ví dụ: tài khoản tạo năm 2026 → chỉ thấy dữ liệu từ 2026; tạo năm 2020 → từ 2020.
 *
 * <p>Giá trị {@code null} nghĩa là KHÔNG giới hạn (ADMIN hoặc chưa đăng nhập).
 * Được set bởi {@link WarehouseScopeAspect} trước khi method chạy và clear ở finally.
 * Repository đọc context này để tự chèn điều kiện {@code created_at >= cutoff}.
 */
public final class CreationDateScopeContext {

    private static final ThreadLocal<LocalDateTime> CUTOFF = new ThreadLocal<>();

    private CreationDateScopeContext() {}

    public static void set(LocalDateTime cutoff) { CUTOFF.set(cutoff); }

    public static LocalDateTime get() { return CUTOFF.get(); }

    public static boolean isActive() { return CUTOFF.get() != null; }

    public static void clear() { CUTOFF.remove(); }
}
