package org.lvtn.mws.application.ports;

import org.lvtn.mws.domain.model.Notification;

/**
 * [GIAI ĐOẠN 7] Cổng đẩy thông báo real-time (WebSocket/STOMP). Adapter ở infrastructure.
 * Việc push là best-effort: lỗi push KHÔNG được làm hỏng nghiệp vụ tạo thông báo.
 */
public interface INotificationPusher {
    void push(Notification notification);
}
