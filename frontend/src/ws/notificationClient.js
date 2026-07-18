/**
 * STOMP client qua SockJS (/ws đã được Vite proxy sang :8080).
 * CONNECT gửi header Authorization: Bearer <jwt> để BE xác thực khung kết nối.
 *
 * @stomp/stompjs + sockjs-client nặng ~150KB và CHỈ cần sau khi đăng nhập
 * thành công => dynamic import, không nằm trong bundle của màn hình login.
 *
 * Hàm trả về một "handle" đồng bộ (có sẵn .deactivate()) dù bên trong nạp thư
 * viện bất đồng bộ — nhờ vậy phía gọi (useEffect trong NotificationContext)
 * không phải đổi kiểu cleanup, và nếu component unmount trước khi thư viện nạp
 * xong thì cờ `disposed` chặn việc kết nối mồ côi.
 */
export function createNotificationClient({ token, onMessage, onStatus }) {
  let client = null
  let disposed = false

  ;(async () => {
    try {
      const [{ Client }, { default: SockJS }] = await Promise.all([
        import('@stomp/stompjs'),
        import('sockjs-client'),
      ])
      if (disposed) return

      client = new Client({
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
    } catch (e) {
      console.error('[MWS] Không nạp được thư viện WebSocket:', e)
      onStatus?.(false)
    }
  })()

  return {
    deactivate() {
      disposed = true
      try { client?.deactivate() } catch { /* noop */ }
      client = null
    },
  }
}
