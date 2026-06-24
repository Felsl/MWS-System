package org.lvtn.mws.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.usecases.notification.GetMyNotificationsUseCase;
import org.lvtn.mws.application.usecases.notification.MarkAllNotificationsReadUseCase;
import org.lvtn.mws.application.usecases.notification.MarkNotificationReadUseCase;
import org.lvtn.mws.infrastructure.security.UserPrincipal;
import org.lvtn.mws.interfaces.dto.response.NotificationResponse;
import org.lvtn.mws.interfaces.mapper.NotificationWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * [GIAI ĐOẠN 7] Hộp thông báo của người dùng hiện tại. Yêu cầu quyền NOTIF_READ.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('NOTIF_READ')")
public class NotificationController {

    private final GetMyNotificationsUseCase getMyNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;
    private final NotificationWebMapper mapper;

    /** Danh sách thông báo (gồm thông báo chung), mới nhất trước. */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> inbox(Authentication authentication) {
        return ResponseEntity.ok(mapper.toResponseList(
                getMyNotificationsUseCase.inbox(currentUserId(authentication))));
    }

    /** Số thông báo chưa đọc. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        long count = getMyNotificationsUseCase.unreadCount(currentUserId(authentication));
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /** Đánh dấu một thông báo đã đọc. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable String id,
                                                         Authentication authentication) {
        return ResponseEntity.ok(mapper.toResponse(
                markNotificationReadUseCase.execute(id, currentUserId(authentication))));
    }

    /** Đánh dấu đã đọc toàn bộ thông báo cá nhân. */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(Authentication authentication) {
        int updated = markAllNotificationsReadUseCase.execute(currentUserId(authentication));
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    private String currentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        throw new IllegalStateException("Không xác định được người dùng hiện tại");
    }
}
