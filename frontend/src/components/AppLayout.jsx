import { useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Badge, Button, Typography, theme } from 'antd'
import {
  DashboardOutlined, UserOutlined, SafetyCertificateOutlined, KeyOutlined,
  HomeOutlined, AppstoreOutlined, TagsOutlined, TeamOutlined,
  BellOutlined, LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined,
  InboxOutlined, FileDoneOutlined,
} from '@ant-design/icons'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { P } from '../constants/permissions'

const { Header, Sider, Content } = Layout

// Mỗi mục gắn quyền tối thiểu để hiện. Không có quyền => ẩn khỏi menu.
const NAV = [
  { key: '/', icon: <DashboardOutlined />, label: 'Tổng quan' },
  {
    type: 'group', label: 'Danh mục gốc', children: [
      { key: '/products', icon: <AppstoreOutlined />, label: 'Sản phẩm', perm: P.MASTER_PRODUCT_VIEW },
      { key: '/categories', icon: <TagsOutlined />, label: 'Nhóm sản phẩm', perm: P.MASTER_PRODUCT_VIEW },
      { key: '/warehouses', icon: <HomeOutlined />, label: 'Kho & ô kệ', perm: P.WAREHOUSE_VIEW },
      { key: '/partners', icon: <TeamOutlined />, label: 'Đối tác', perm: P.MASTER_PARTNER_MANAGE },
    ],
  },
  {
    type: 'group', label: 'Nhập kho', children: [
      { key: '/purchase-orders', icon: <FileDoneOutlined />, label: 'Đơn mua hàng', perm: P.INBOUND_VIEW_PO },
      { key: '/goods-receipts', icon: <InboxOutlined />, label: 'Phiếu nhập kho', perm: P.INBOUND_VIEW_GRN },
    ],
  },
  {
    type: 'group', label: 'Quản trị', children: [
      { key: '/users', icon: <UserOutlined />, label: 'Người dùng', perm: P.USER_VIEW },
      { key: '/roles', icon: <SafetyCertificateOutlined />, label: 'Vai trò', perm: P.ROLE_VIEW },
      { key: '/permissions', icon: <KeyOutlined />, label: 'Quyền hạn', perm: P.PERMISSION_VIEW },
    ],
  },
]

function buildMenu(nav, hasPermission) {
  return nav
    .map((item) => {
      if (item.type === 'group') {
        const children = buildMenu(item.children, hasPermission)
        return children.length ? { ...item, children } : null
      }
      if (item.perm && !hasPermission(item.perm)) return null
      const { perm, ...rest } = item
      return rest
    })
    .filter(Boolean)
}

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const { user, logout, hasPermission } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { token } = theme.useToken()

  const items = buildMenu(NAV, hasPermission)
  const selectedKey = '/' + (location.pathname.split('/')[1] || '')

  const userMenu = {
    items: [
      { key: 'profile', icon: <UserOutlined />, label: user?.fullName || user?.username },
      { type: 'divider' },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
    ],
    onClick: ({ key }) => {
      if (key === 'logout') { logout(); navigate('/login', { replace: true }) }
    },
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider collapsible collapsed={collapsed} trigger={null} theme="light"
        style={{ borderRight: `1px solid ${token.colorBorderSecondary}` }}>
        <div style={{ height: 56, display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontWeight: 700, fontSize: 18, color: token.colorPrimary }}>
          {collapsed ? 'MWS' : 'MWS · Kho'}
        </div>
        <Menu mode="inline" selectedKeys={[selectedKey]} items={items}
          onClick={({ key }) => navigate(key)} />
      </Sider>

      <Layout>
        <Header style={{ background: token.colorBgContainer, padding: '0 16px',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          borderBottom: `1px solid ${token.colorBorderSecondary}` }}>
          <Button type="text" onClick={() => setCollapsed(v => !v)}
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />} />
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Badge count={0} size="small">
              <Button type="text" icon={<BellOutlined />} onClick={() => navigate('/notifications')} />
            </Badge>
            <Dropdown menu={userMenu} trigger={['click']}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                <Avatar size="small" icon={<UserOutlined />} />
                <Typography.Text>{user?.fullName || user?.username}</Typography.Text>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content style={{ margin: 16 }}>
          <div style={{ background: token.colorBgContainer, padding: 20, borderRadius: 8, minHeight: '100%' }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}
