package org.lvtn.mws.infrastructure.security.scope;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.domain.model.User;
import org.lvtn.mws.domain.repository.IUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Giải quyết "phạm vi theo ngày tạo tài khoản": user chỉ thấy dữ liệu tạo từ khi
 * tài khoản của họ tồn tại. ADMIN (authority {@code ROLE_ADMIN}) được miễn trừ.
 *
 * <p>Dùng ở 2 chỗ:
 * <ul>
 *   <li>Danh sách: {@link WarehouseScopeAspect} gọi {@link #cutoffForCurrentUser()}
 *       và nạp vào {@link CreationDateScopeContext} để repository tự lọc.</li>
 *   <li>Chi tiết (get theo id): usecase gọi {@link #assertVisible(LocalDateTime)}
 *       để chặn 403 nếu bản ghi cũ hơn thời điểm tạo tài khoản.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CreationDateScope {

    private final IUserRepository userRepository;

    /** Mốc lọc theo ngày tạo tài khoản của user hiện tại; {@code null} nếu ADMIN / chưa đăng nhập. */
    public LocalDateTime cutoffForCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (admin) return null;

        return userRepository.findByUsername(auth.getName())
                .map(User::getCreatedAt)
                .orElse(null);
    }

    /** Ném {@link AccessDeniedException} (403) nếu bản ghi được tạo TRƯỚC khi tài khoản user tồn tại. */
    public void assertVisible(LocalDateTime recordCreatedAt) {
        LocalDateTime cutoff = cutoffForCurrentUser();
        if (cutoff != null && recordCreatedAt != null && recordCreatedAt.isBefore(cutoff)) {
            throw new AccessDeniedException("Dữ liệu nằm ngoài phạm vi thời gian tài khoản được phép xem");
        }
    }
}
