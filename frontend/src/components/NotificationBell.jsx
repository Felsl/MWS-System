import { Badge, Button, Dropdown, List, Typography, Tag, Empty, Tooltip } from 'antd'
import { BellOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'
import { useNotifications } from '../context/NotificationContext'

export default function NotificationBell() {
  const navigate = useNavigate()
  const { notifications, unreadCount, connected, markRead, markAllRead, canRead } = useNotifications()

  if (!canRead) return null

  const recent = notifications.slice(0, 8)

  const onItem = (n) => {
    if (!n.isRead) markRead(n.id)
  }

  const dropdownContent = (
    <div style={{ width: 340, background: '#fff', borderRadius: 8, boxShadow: '0 6px 16px rgba(0,0,0,.12)', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', borderBottom: '1px solid #f0f0f0' }}>
        <b>Thông báo</b>
        {unreadCount > 0 && <a onClick={markAllRead}>Đánh dấu đã đọc hết</a>}
      </div>
      {recent.length === 0
        ? <div style={{ padding: 24 }}><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có thông báo" /></div>
        : (
          <List size="small" dataSource={recent}
            style={{ maxHeight: 360, overflow: 'auto' }}
            renderItem={(n) => (
              <List.Item style={{ padding: '10px 12px', cursor: 'pointer', background: n.isRead ? '#fff' : '#e6f4ff' }}
                onClick={() => onItem(n)}>
                <List.Item.Meta
                  title={<span style={{ fontWeight: n.isRead ? 400 : 600 }}>{n.title || n.type || 'Thông báo'}</span>}
                  description={
                    <div>
                      <div style={{ color: 'rgba(0,0,0,.65)' }}>{n.message}</div>
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {n.createdAt ? dayjs(n.createdAt).format('DD/MM HH:mm') : ''}
                      </Typography.Text>
                    </div>
                  } />
              </List.Item>
            )} />
        )}
      <div style={{ padding: 10, borderTop: '1px solid #f0f0f0', textAlign: 'center' }}>
        <a onClick={() => navigate('/notifications')}>Xem tất cả</a>
      </div>
    </div>
  )

  return (
    <Dropdown popupRender={() => dropdownContent} trigger={['click']} placement="bottomRight">
      <Tooltip title={connected ? 'Realtime đang kết nối' : 'Realtime chưa kết nối'}>
        <Badge count={unreadCount} size="small" offset={[-2, 2]}>
          <Button type="text" icon={<BellOutlined style={{ color: connected ? undefined : '#bbb' }} />} />
        </Badge>
      </Tooltip>
    </Dropdown>
  )
}
