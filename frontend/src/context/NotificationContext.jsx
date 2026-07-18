import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { App as AntdApp } from 'antd'
import { useAuth } from '../auth/AuthContext'
import { tokenStore, getErrorMessage } from '../api/client'
import { notificationsApi, readUnread } from '../api/notifications.api'
import { createNotificationClient } from '../ws/notificationClient'

const NotificationContext = createContext(null)

export function NotificationProvider({ children }) {
  const { isAuthenticated, user, hasPermission } = useAuth()
  const { notification } = AntdApp.useApp()
  const qc = useQueryClient()
  const [connected, setConnected] = useState(false)

  const canRead = isAuthenticated && hasPermission('NOTIF_READ')

  const inbox = useQuery({
    queryKey: ['notifications'], queryFn: notificationsApi.inbox, enabled: canRead,
  })
  const unread = useQuery({
    queryKey: ['notif-unread'], queryFn: notificationsApi.unreadCount, enabled: canRead,
    refetchInterval: 60000, // dự phòng nếu WS rớt
  })

  // Kết nối WebSocket khi đã đăng nhập; nhận tin -> làm mới inbox + đếm.
  useEffect(() => {
    // Không setConnected(false) ở đây: state chỉ phản ánh TRẠNG THÁI WS.
    // Việc "chưa đăng nhập thì coi như mất kết nối" được suy ra lúc đọc
    // (xem `connected:` bên dưới) thay vì set trong thân effect.
    if (!canRead) return
    const token = tokenStore.access
    if (!token) return
    const client = createNotificationClient({
      token,
      onStatus: setConnected,
      onMessage: (msg) => {
        if (msg && typeof msg === 'object' && (msg.title || msg.message)) {
          notification.info({ message: msg.title || 'Thông báo mới', description: msg.message, placement: 'topRight' })
        }
        qc.invalidateQueries({ queryKey: ['notifications'] })
        qc.invalidateQueries({ queryKey: ['notif-unread'] })
      },
    })
    return () => { client.deactivate(); setConnected(false) }
  }, [canRead, user?.userId]) // eslint-disable-line react-hooks/exhaustive-deps

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['notifications'] })
    qc.invalidateQueries({ queryKey: ['notif-unread'] })
  }

  /**
   * Chụp lại cache hiện tại rồi sửa ngay tại chỗ — nếu API hỏng thì trả về
   * nguyên trạng. Dùng cho "đánh dấu đã đọc": thao tác này không thể hỏng theo
   * kiểu nguy hiểm, và bắt người dùng chờ round-trip chỉ để chữ đậm nhạt đi là
   * vô lý — nhất là qua Wi-Fi kho.
   *
   * `cancelQueries` bắt buộc: nếu có refetch đang bay, nó về sau và ghi đè bản
   * sửa lạc quan => giao diện nhấp về trạng thái cũ.
   */
  // Trả về object CÙNG HÌNH DẠNG với cái BE gửi, chỉ đổi con số bên trong.
  const withCount = (old, n) => (old && typeof old === 'object' ? { ...old, count: n } : { count: n })

  const optimistic = (patch) => ({
    onMutate: async (arg) => {
      await qc.cancelQueries({ queryKey: ['notifications'] })
      await qc.cancelQueries({ queryKey: ['notif-unread'] })
      const prevInbox = qc.getQueryData(['notifications'])
      const prevUnread = qc.getQueryData(['notif-unread'])
      qc.setQueryData(['notifications'], (old) => patch.inbox(old || [], arg))
      // Giữ nguyên HÌNH DẠNG cache: BE trả Map {count:n}, không phải số trần.
      // Ghi đè bằng số sẽ làm readUnread() không đọc được và badge rơi về 0.
      qc.setQueryData(['notif-unread'], (old) => patch.unread(old, arg))
      return { prevInbox, prevUnread }   // giữ để hoàn tác
    },
    onError: (e, _arg, ctx) => {
      // Trả cache về đúng ảnh chụp trước đó, rồi mới báo lỗi.
      if (ctx?.prevInbox !== undefined) qc.setQueryData(['notifications'], ctx.prevInbox)
      if (ctx?.prevUnread !== undefined) qc.setQueryData(['notif-unread'], ctx.prevUnread)
      notification.error({ message: getErrorMessage(e) })
    },
    // Dù thành công hay thất bại đều đồng bộ lại với server.
    onSettled: invalidate,
  })

  const markReadMut = useMutation({
    mutationFn: notificationsApi.markRead,
    ...optimistic({
      inbox: (list, id) => list.map(n => (n.id === id ? { ...n, isRead: true } : n)),
      unread: (old, id) => {
        const list = qc.getQueryData(['notifications']) || []
        const wasUnread = list.some(x => x.id === id && !x.isRead)
        return withCount(old, Math.max(0, readUnread(old) - (wasUnread ? 1 : 0)))
      },
    }),
  })

  const markAllMut = useMutation({
    mutationFn: notificationsApi.markAllRead,
    ...optimistic({
      inbox: (list) => list.map(n => ({ ...n, isRead: true })),
      unread: (old) => withCount(old, 0),
    }),
  })

  const value = useMemo(() => ({
    notifications: inbox.data || [],
    loading: inbox.isLoading,
    unreadCount: readUnread(unread.data),
    // WS nối được NHƯNG không còn quyền đọc => vẫn coi là chưa kết nối.
    connected: canRead && connected,
    canRead,
    refetch: () => { inbox.refetch(); unread.refetch() },
    markRead: (id) => markReadMut.mutate(id),
    markAllRead: () => markAllMut.mutate(),
  }), [inbox.data, inbox.isLoading, unread.data, connected, canRead]) // eslint-disable-line react-hooks/exhaustive-deps

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useNotifications() {
  return useContext(NotificationContext) || {
    notifications: [], unreadCount: 0, connected: false, canRead: false,
    refetch: () => {}, markRead: () => {}, markAllRead: () => {},
  }
}
