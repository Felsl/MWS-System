import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

// Tạo STOMP client qua SockJS (/ws đã được Vite proxy sang :8080).
// CONNECT gửi header Authorization: Bearer <jwt> để BE xác thực khung kết nối.
export function createNotificationClient({ token, onMessage, onStatus }) {
  const client = new Client({
    webSocketFactory: () => new SockJS('/ws'),
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      onStatus?.(true)
      // Đích cá nhân: server route /user/{userId}/queue/notifications
      client.subscribe('/user/queue/notifications', (frame) => {
        let payload = frame.body
        try { payload = JSON.parse(frame.body) } catch { /* giữ nguyên chuỗi */ }
        onMessage?.(payload)
      })
    },
    onDisconnect: () => onStatus?.(false),
    onWebSocketClose: () => onStatus?.(false),
    onStompError: () => onStatus?.(false),
  })
  client.activate()
  return client
}
