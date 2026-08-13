import { Badge, Button, Dropdown, List, Typography, Empty, Tooltip, theme } from 'antd'
import { BellOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useState } from 'react'
import dayjs from 'dayjs'
import { useNotifications } from '../context/NotificationContext'

export default function NotificationBell() {
  const navigate = useNavigate()
  const { notifications, unreadCount, connected, markRead, markAllRead, canRead } = useNotifications()
  const [open, setOpen] = useState(false)
  // Toàn bộ màu lấy từ design token của antd thay vì hardcode #fff/#f0f0f0/#e6f4ff.
  // Bản cũ hardcode nên khi bật dark mode là chữ trắng trên nền trắng.
  const { token } = theme.useToken()

  if (!canRead) return null

  const recent = notifications.slice(0, 8)

  // Điều hướng tới đối tượng của thông báo (PO -> phiếu PO, đơn bán/backorder -> đơn bán).
  const routeOf = (n) => {
    if (!n.referenceId) return null
    if (n.referenceType === 'PURCHASE_ORDER') return `/purchase-orders/${n.referenceId}`
    if (n.referenceType === 'SALES_ORDER') return `/sales-orders/${n.referenceId}`
    return null
  }

  const onItem = (n) => {
    if (!n.isRead) markRead(n.id)
    const to = routeOf(n)
    if (to) { setOpen(false); navigate(to) }
  }

  const dropdownContent = (
    <div style={{
      width: 340, background: token.colorBgElevated, borderRadius: token.borderRadiusLG,
      boxShadow: token.boxShadowSecondary, overflow: 'hidden',
    }}>
      <div style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        padding: '10px 12px', borderBottom: `1px solid ${token.colorBorderSecondary}`,
      }}>
        <b>Thông báo</b>
        {unreadCount > 0 && <Button type="link" size="small" style={{ padding: 0, height: 'auto' }} onClick={markAllRead}>Đánh dấu đã đọc hết</Button>}
      </div>
      {recent.length === 0
        ? <div style={{ padding: 24 }}><Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có thông báo" /></div>
        : (
          <List size="small" dataSource={recent}
            style={{ maxHeight: 360, overflow: 'auto' }}
            renderItem={(n) => (
              <List.Item
                style={{
                  padding: '10px 12px', cursor: 'pointer',
                  // chưa đọc: nền nhấn nhẹ theo token (tự đúng ở cả sáng lẫn tối)
                  background: n.isRead ? 'transparent' : token.controlItemBgActive,
                }}
                onClick={() => onItem(n)}>
                <List.Item.Meta
                  title={<span style={{ fontWeight: n.isRead ? 400 : 600 }}>{n.title || n.type || 'Thông báo'}</span>}
                  description={
                    <div>
                      <div style={{ color: token.colorText }}>{n.message}</div>
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {n.createdAt ? dayjs(n.createdAt).format('DD/MM HH:mm') : ''}
                      </Typography.Text>
                    </div>
                  } />
              </List.Item>
            )} />
        )}
      <div style={{ padding: 10, borderTop: `1px solid ${token.colorBorderSecondary}`, textAlign: 'center' }}>
        <Button type="link" size="small" style={{ padding: 0, height: 'auto' }} onClick={() => navigate('/notifications')}>Xem tất cả</Button>
      </div>
    </div>
  )

  return (
    <Dropdown popupRender={() => dropdownContent} trigger={['click']} placement="bottomRight"
      open={open} onOpenChange={setOpen}>
      <Tooltip title={connected ? 'Realtime đang kết nối' : 'Realtime chưa kết nối'}>
        <Badge count={unreadCount} size="small" offset={[-2, 2]}>
          <Button type="text" aria-label="Thông báo"
            icon={<BellOutlined style={{ color: connected ? undefined : token.colorTextDisabled }} />} />
        </Badge>
      </Tooltip>
    </Dropdown>
  )
}
