import { List, Button, Typography, Tag, Space, Empty, Badge , theme } from 'antd'
import { CheckOutlined, ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { useNotifications } from '../../context/NotificationContext'
import { useFitY } from '../../hooks/useFitY'

export default function NotificationsPage() {
  const { notifications, loading, unreadCount, connected, markRead, markAllRead, refetch } = useNotifications()
  const { ref: listRef, y: listH } = useFitY()
  const { token } = theme.useToken()
  const navigate = useNavigate()

  const routeOf = (n) => {
    if (!n.referenceId) return null
    if (n.referenceType === 'PURCHASE_ORDER') return `/purchase-orders/${n.referenceId}`
    if (n.referenceType === 'SALES_ORDER') return `/sales-orders/${n.referenceId}`
    return null
  }
  const onItem = (n) => {
    if (!n.isRead) markRead(n.id)
    const to = routeOf(n)
    if (to) navigate(to)
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Space>
          <Typography.Title level={4} style={{ margin: 0 }}>Thông báo</Typography.Title>
          <Badge status={connected ? 'success' : 'default'}
            text={<Typography.Text type="secondary">{connected ? 'Realtime' : 'Offline'}</Typography.Text>} />
        </Space>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={refetch}>Làm mới</Button>
          <Button type="primary" icon={<CheckOutlined />} disabled={!unreadCount} onClick={markAllRead}>
            Đánh dấu đã đọc hết{unreadCount ? ` (${unreadCount})` : ''}
          </Button>
        </Space>
      </div>

      {(!notifications || notifications.length === 0)
        ? <Empty description="Chưa có thông báo" />
        : (
          <div ref={listRef} style={{ height: listH, overflowY: 'auto', paddingRight: 4 }}>
          <List
            loading={loading}
            itemLayout="horizontal"
            dataSource={notifications}
            renderItem={(n) => (
              <List.Item
                onClick={() => onItem(n)}
                style={{ cursor: routeOf(n) ? 'pointer' : 'default', background: n.isRead ? undefined : token.controlItemBgActive, padding: '12px 16px', borderRadius: token.borderRadiusLG, marginBottom: 6 }}
                actions={n.isRead ? [] : [<a key="r" onClick={(e) => { e.stopPropagation(); markRead(n.id) }}>Đánh dấu đã đọc</a>]}
              >
                <List.Item.Meta
                  title={
                    <Space>
                      <span style={{ fontWeight: n.isRead ? 400 : 600 }}>{n.title || 'Thông báo'}</span>
                      {n.type && <Tag>{n.type}</Tag>}
                      {!n.isRead && <Tag color="blue">Mới</Tag>}
                    </Space>
                  }
                  description={
                    <div>
                      <div style={{ color: token.colorText }}>{n.message}</div>
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {n.referenceType ? `${n.referenceType}: ${n.referenceId} · ` : ''}
                        {n.createdAt ? dayjs(n.createdAt).format('DD/MM/YYYY HH:mm') : ''}
                      </Typography.Text>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
          </div>
        )}
    </div>
  )
}
