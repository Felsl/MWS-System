package org.lvtn.mws.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.lvtn.mws.application.ports.INotificationPusher;
import org.lvtn.mws.domain.model.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * [GIAI ĐOẠN 7] Đẩy thông báo real-time qua STOMP.
 *   - Thông báo cá nhân (userId != null): gửi tới /user/{userId}/queue/notifications.
 *   - Thông báo chung (userId == null): broadcast tới /topic/notifications.
 * Principal name của phiên STOMP được set = userId trong WebSocketConfig (xem interceptor),
 * nên convertAndSendToUser dùng userId là đúng đích.
 *
 * Ghi chú: payload kiểu Map khiến convertAndSend bị "ambiguous" do trùng nhiều overload
 * (payload vs headers). Ép kiểu payload về Object để chốt đúng overload (destination, Object payload).
 */
@Component
@RequiredArgsConstructor
public class WebSocketNotificationPusher implements INotificationPusher {

    static final String USER_QUEUE = "/queue/notifications";
    static final String BROADCAST_TOPIC = "/topic/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void push(Notification n) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", n.getId());
        data.put("title", n.getTitle());
        data.put("message", n.getMessage());
        data.put("type", n.getType() == null ? null : n.getType().name());
        data.put("referenceType", n.getReferenceType());
        data.put("referenceId", n.getReferenceId());
        data.put("isRead", n.isRead());
        data.put("createdAt", n.getCreatedAt());

        Object payload = data; // ép về Object để tránh ambiguous overload

        if (n.getUserId() != null) {
            messagingTemplate.convertAndSendToUser(n.getUserId(), USER_QUEUE, payload);
        } else {
            messagingTemplate.convertAndSend(BROADCAST_TOPIC, payload);
        }
    }
}