import { List, Button, Typography, Tag, Space, Empty, Badge } from 'antd'
import { CheckOutlined, ReloadOutlined, WifiOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { useNotifications } from '../../context/NotificationContext'
import { useFitY } from '../../hooks/useFitY'

export default function NotificationsPage() {
  const { notifications, loading, unreadCount, connected, markRead, markAllRead, refetch } = useNotifications()
  const { ref: listRef, y: listH } = useFitY()

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
                style={{ background: n.isRead ? undefined : '#e6f4ff', padding: '12px 16px', borderRadius: 8, marginBottom: 6 }}
                actions={n.isRead ? [] : [<a key="r" onClick={() => markRead(n.id)}>Đánh dấu đã đọc</a>]}
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
                      <div style={{ color: 'rgba(0,0,0,.75)' }}>{n.message}</div>
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
