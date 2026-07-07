import api from './client'
const base = '/api/v1/notifications'
export const notificationsApi = {
  inbox: () => api.get(base).then(r => r.data),                       // List<NotificationResponse>
  unreadCount: () => api.get(`${base}/unread-count`).then(r => r.data), // Map { count: n }
  markRead: (id) => api.patch(`${base}/${id}/read`).then(r => r.data),
  markAllRead: () => api.patch(`${base}/read-all`).then(r => r.data),
}
// Đọc số chưa đọc từ Map trả về (không rõ key chính xác -> lấy linh hoạt).
export function readUnread(map) {
  if (!map) return 0
  return map.count ?? map.unreadCount ?? map.unread ?? Object.values(map)[0] ?? 0
}
