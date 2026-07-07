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
    if (!canRead) { setConnected(false); return }
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
    return () => { client.deactivate() }
  }, [canRead, user?.userId]) // eslint-disable-line react-hooks/exhaustive-deps

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['notifications'] })
    qc.invalidateQueries({ queryKey: ['notif-unread'] })
  }
  const markReadMut = useMutation({
    mutationFn: notificationsApi.markRead, onSuccess: invalidate,
    onError: (e) => notification.error({ message: getErrorMessage(e) }),
  })
  const markAllMut = useMutation({
    mutationFn: notificationsApi.markAllRead, onSuccess: invalidate,
    onError: (e) => notification.error({ message: getErrorMessage(e) }),
  })

  const value = useMemo(() => ({
    notifications: inbox.data || [],
    loading: inbox.isLoading,
    unreadCount: readUnread(unread.data),
    connected,
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
