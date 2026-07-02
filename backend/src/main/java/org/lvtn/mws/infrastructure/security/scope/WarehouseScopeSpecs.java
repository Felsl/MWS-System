package org.lvtn.mws.infrastructure.security.scope;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Bộ Specification tái sử dụng để tự động ghép điều kiện Data Scope
 * ({@code warehouse_id IN (:allowed)}) vào mọi truy vấn list.
 *
 * <p>Nguồn dữ liệu scope: {@link WarehouseScopeContext} — được nạp bởi
 * {@link WarehouseScopeAspect} cho các method gắn {@code @WarehouseScoped}.
 *
 * <p>Quy ước: list rỗng = user không bị giới hạn (ADMIN / role toàn cục)
 * → trả về predicate luôn đúng (conjunction), tức không lọc.
 *
 * <p>Cách dùng trong UseCase:
 * <pre>
 *   Specification&lt;StockMovementEntity&gt; spec =
 *       Specification.where(filterSpec)
 *                    .and(WarehouseScopeSpecs.restrict("warehouseId"));
 * </pre>
 *
 * <p>Với entity dùng {@code @EmbeddedId}, truyền đường dẫn lồng bằng dấu chấm,
 * ví dụ {@code restrict("id.warehouseId")}.
 */
public final class WarehouseScopeSpecs {

    private WarehouseScopeSpecs() {}

    /**
     * Giới hạn theo MỘT trường warehouse (trường hợp phổ biến).
     *
     * @param warehouseField tên field trên entity (hỗ trợ đường dẫn lồng
     *                       "id.warehouseId" cho @EmbeddedId)
     */
    public static <T> Specification<T> restrict(String warehouseField) {
        return (root, query, cb) -> {
            List<String> allowed = WarehouseScopeContext.get();
            if (allowed == null || allowed.isEmpty()) {
                return cb.conjunction(); // không giới hạn scope
            }
            return resolvePath(root, warehouseField).in(allowed);
        };
    }

    /**
     * Giới hạn theo HAI trường warehouse, thoả mãn khi thuộc ÍT NHẤT một trường.
     * Dùng cho {@code TransferOrder} (fromWarehouseId / toWarehouseId): user
     * được thấy phiếu nếu có quyền ở kho nguồn HOẶC kho đích.
     */
    public static <T> Specification<T> restrictAny(String field1, String field2) {
        return (root, query, cb) -> {
            List<String> allowed = WarehouseScopeContext.get();
            if (allowed == null || allowed.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(
                    resolvePath(root, field1).in(allowed),
                    resolvePath(root, field2).in(allowed)
            );
        };
    }

    /** Duyệt đường dẫn "a.b.c" thành Path lồng nhau. */
    private static <T> Path<Object> resolvePath(jakarta.persistence.criteria.Root<T> root, String dotted) {
        Path<Object> path = null;
        for (String part : dotted.split("\\.")) {
            path = (path == null) ? root.get(part) : path.get(part);
        }
        return path;
    }
}
