package org.lvtn.mws.infrastructure.security.scope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lvtn.mws.domain.repository.IUserWarehouseAccessRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Chốt chặn Data Scope cho các thao tác GHI (create / update / dispatch...).
 *
 * <p>Khác với {@link WarehouseScopeSpecs} (lọc dữ liệu ĐỌC), guard này ngăn
 * một thủ kho được giao kho A tạo/sửa chứng từ thuộc kho B — điều mà
 * {@code @PreAuthorize} (chỉ xét role, không biết warehouse cụ thể) không làm được.
 *
 * <p>Guard tự truy vấn danh sách kho được phép trực tiếp từ
 * {@code user_warehouse_access} theo user đang đăng nhập, KHÔNG phụ thuộc vào
 * {@link WarehouseScopeContext} (vốn chỉ được nạp trong các method @WarehouseScoped),
 * nên dùng được ở bất kỳ usecase ghi nào.
 *
 * <p>Quy ước: user không có bản ghi access nào (ADMIN / role toàn cục) =
 * được thao tác mọi kho.
 *
 * <p>Cách dùng:
 * <pre>
 *   warehouseAccessGuard.check(warehouseId);          // ném 403 nếu ngoài scope
 *   warehouseAccessGuard.checkAll(List.of(from, to)); // transfer: cần quyền cả 2 kho
 * </pre>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WarehouseAccessGuard {

    private final IUserWarehouseAccessRepository accessRepository;

    /** Ném {@link AccessDeniedException} (→ HTTP 403) nếu warehouseId ngoài phạm vi cho phép. */
    public void check(String warehouseId) {
        if (warehouseId == null || warehouseId.isBlank()) {
            return; // không có kho để kiểm tra
        }
        List<String> allowed = currentAllowed();
        if (allowed.isEmpty()) {
            return; // user toàn cục → cho phép
        }
        if (!allowed.contains(warehouseId)) {
            String user = currentUsername();
            log.warn("Chặn thao tác chéo kho: user '{}' cố thao tác kho '{}' (được phép: {})",
                    user, warehouseId, allowed);
            throw new AccessDeniedException(
                    "Bạn không được phép thao tác trên kho " + warehouseId);
        }
    }

    /** Kiểm tra tất cả kho trong danh sách đều nằm trong phạm vi cho phép. */
    public void checkAll(Collection<String> warehouseIds) {
        if (warehouseIds == null) return;
        for (String id : warehouseIds) {
            check(id);
        }
    }

    /** Danh sách kho user hiện tại được phép; rỗng = không giới hạn (ADMIN). */
    private List<String> currentAllowed() {
        return accessRepository.findWarehouseIdsByUsername(currentUsername());
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : null;
    }
}
