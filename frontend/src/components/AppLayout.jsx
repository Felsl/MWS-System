import { useEffect, useState } from 'react'
import {
  Layout, Menu, Avatar, Dropdown, Button, Typography, Drawer, Grid, Tooltip, theme,
} from 'antd'
import {
  DashboardOutlined, UserOutlined, SafetyCertificateOutlined, KeyOutlined,
  HomeOutlined, AppstoreOutlined, TagsOutlined, TeamOutlined,
  LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined, MenuOutlined,
  BulbOutlined, BulbFilled,
  InboxOutlined, FileDoneOutlined,
  ShoppingCartOutlined, ProfileOutlined, CarOutlined, SwapOutlined,
  AuditOutlined, ReconciliationOutlined,
  DatabaseOutlined, HistoryOutlined, ScanOutlined, ImportOutlined, BarcodeOutlined,
} from '@ant-design/icons'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import NotificationBell from './NotificationBell'
import ErrorBoundary from './ErrorBoundary'
import { useThemeMode } from '../context/ThemeContext'
import { P } from '../constants/permissions'

const { Header, Sider, Content } = Layout

// Mỗi mục gắn quyền tối thiểu để hiện. Không có quyền => ẩn khỏi menu.
const NAV = [
  { key: '/', icon: <DashboardOutlined />, label: 'Tổng quan' },
  { key: '/data-io', icon: <ImportOutlined />, label: 'Nhập/Xuất Excel', perm: P.MASTER_PRODUCT_MANAGE },
  {
    type: 'group', label: 'Danh mục gốc', icon: <AppstoreOutlined />, children: [
      { key: '/products', icon: <AppstoreOutlined />, label: 'Sản phẩm', perm: P.MASTER_PRODUCT_VIEW },
      { key: '/categories', icon: <TagsOutlined />, label: 'Nhóm sản phẩm', perm: P.MASTER_PRODUCT_VIEW },
      { key: '/warehouses', icon: <HomeOutlined />, label: 'Kho & ô kệ', perm: P.WAREHOUSE_VIEW },
      { key: '/partners', icon: <TeamOutlined />, label: 'Đối tác', perm: P.MASTER_PARTNER_MANAGE },
    ],
  },
  {
    type: 'group', label: 'Tồn kho', icon: <DatabaseOutlined />, children: [
      { key: '/inventory', icon: <DatabaseOutlined />, label: 'Tồn kho & lô', perm: P.INVENTORY_VIEW },
      { key: '/barcode-labels', icon: <BarcodeOutlined />, label: 'In tem mã vạch', perm: P.INVENTORY_VIEW },
      { key: '/stock-movements', icon: <HistoryOutlined />, label: 'Thẻ kho (Kardex)', perm: P.AUDIT_VIEW_MOVEMENTS },
    ],
  },
  {
    type: 'group', label: 'Nhập kho', icon: <InboxOutlined />, children: [
      { key: '/purchase-orders', icon: <FileDoneOutlined />, label: 'Đơn mua hàng', perm: P.INBOUND_VIEW_PO },
      { key: '/goods-receipts', icon: <InboxOutlined />, label: 'Phiếu nhập kho', perm: P.INBOUND_VIEW_GRN },
    ],
  },
  {
    type: 'group', label: 'Xuất kho', icon: <ShoppingCartOutlined />, children: [
      { key: '/sales-orders', icon: <ShoppingCartOutlined />, label: 'Đơn bán hàng', perm: P.OUTBOUND_VIEW_SO },
      { key: '/picking-lists', icon: <ProfileOutlined />, label: 'Lệnh lấy hàng', perm: P.OUTBOUND_VIEW },
      { key: '/picking-scan', icon: <ScanOutlined />, label: 'Quét lấy hàng', perm: P.OUTBOUND_PICK },
      { key: '/shipments', icon: <CarOutlined />, label: 'Vận đơn', perm: P.OUTBOUND_VIEW },
    ],
  },
  {
    type: 'group', label: 'Điều chuyển', icon: <SwapOutlined />, children: [
      { key: '/transfer-orders', icon: <SwapOutlined />, label: 'Phiếu điều chuyển', perm: P.TRANSFER_VIEW },
    ],
  },
  {
    type: 'group', label: 'Kiểm kê', icon: <AuditOutlined />, children: [
      { key: '/stocktakes', icon: <AuditOutlined />, label: 'Phiên kiểm kê', perm: P.STOCKTAKE_VIEW },
      { key: '/adjustment-vouchers', icon: <ReconciliationOutlined />, label: 'Phiếu điều chỉnh', perm: P.ADJUSTMENT_VIEW },
    ],
  },
  {
    type: 'group', label: 'Quản trị', icon: <TeamOutlined />, children: [
      { key: '/users', icon: <UserOutlined />, label: 'Người dùng', perm: P.USER_VIEW },
      { key: '/roles', icon: <SafetyCertificateOutlined />, label: 'Vai trò', perm: P.ROLE_VIEW },
      { key: '/permissions', icon: <KeyOutlined />, label: 'Quyền hạn', perm: P.PERMISSION_VIEW },
    ],
  },
]

// path -> nhãn, dùng cho document.title. Các trang ngoài NAV bổ sung thủ công.
const TITLE_BY_PATH = (() => {
  const m = { '/notifications': 'Thông báo' }
  const walk = (nav) => nav.forEach((i) => (i.type === 'group' ? walk(i.children) : (m[i.key] = i.label)))
  walk(NAV)
  return m
})()

// Nhóm điều hướng -> SubMenu đóng/mở được.
// Bản cũ dùng type:'group' (nhãn tĩnh, luôn xổ). Hệ quả: 20 mục xổ hết cùng lúc
// phải cuộn nhiều, và khi THU GỌN sidebar thì nhãn nhóm biến mất => còn 20 icon
// rời rạc không biết mục nào thuộc nhóm nào. SubMenu vừa gập lại cho gọn, vừa
// hiện thành popup-có-tiêu-đề khi thu gọn.
//
// Nhóm chỉ có đúng 1 mục con (Điều chuyển) thì KHÔNG bọc submenu — thừa một lớp
// bấm cho một mục, đưa thẳng ra ngoài.
function buildMenu(nav, hasPermission) {
  return nav
    .map((item, i) => {
      if (item.type === 'group') {
        const children = buildMenu(item.children, hasPermission)
        if (!children.length) return null
        if (children.length === 1) return children[0]
        return {
          key: `grp-${item.label}-${i}`,
          icon: item.icon,
          label: item.label,
          children,
        }
      }
      if (item.perm && !hasPermission(item.perm)) return null
      // eslint-disable-next-line no-unused-vars -- bóc `perm` ra khỏi item menu
      const { perm, ...rest } = item
      return rest
    })
    .filter(Boolean)
}

// Với mỗi mục đang chọn, tìm key nhóm chứa nó để tự mở đúng submenu.
function findOpenKeys(items, selectedKey) {
  for (const it of items) {
    if (it.children) {
      if (it.children.some(c => c.key === selectedKey)) return [it.key]
    }
  }
  return []
}

export default function AppLayout() {
  const { user, logout, hasPermission } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const { token } = theme.useToken()
  const screens = Grid.useBreakpoint()
  const { isDark, toggle: toggleTheme } = useThemeMode()

  // lg trở xuống (<992px): sidebar cố định chiếm nửa màn hình điện thoại
  // => chuyển sang Drawer trượt. Trang /picking-scan chạy trên máy cầm tay.
  const isMobile = !screens.lg

  const [collapsed, setCollapsed] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)

  const items = buildMenu(NAV, hasPermission)
  const selectedKey = '/' + (location.pathname.split('/')[1] || '')

  // Tự mở nhóm chứa trang hiện tại, hợp nhất với các nhóm người dùng đã mở tay.
  // Dùng state phái sinh (gộp lúc render) thay cho setState-trong-effect —
  // effect kiểu đó gây render thừa và bị lint chặn (set-state-in-effect).
  const [userOpen, setUserOpen] = useState([])
  const autoOpen = findOpenKeys(items, selectedKey)
  const openKeys = Array.from(new Set([...userOpen, ...autoOpen]))

  // Tiêu đề tab theo trang: phục vụ mở nhiều tab, lịch sử và bookmark.
  useEffect(() => {
    const name = TITLE_BY_PATH[selectedKey]
    document.title = name ? `${name} · MWS` : 'MWS · Quản lý kho'
  }, [selectedKey])

  // Điều hướng + đóng drawer trong CÙNG một handler: đóng bằng useEffect theo
  // pathname sẽ gây thêm một lượt render thừa (react-hooks/set-state-in-effect).
  const go = (key) => { setDrawerOpen(false); navigate(key) }

  const menu = (
    <Menu
      mode="inline"
      selectedKeys={[selectedKey]}
      // Khi thu gọn, antd tự đổi submenu thành popup => không truyền openKeys
      // (nếu không sẽ vênh). Chỉ điều khiển openKeys ở trạng thái mở rộng.
      {...(collapsed ? {} : { openKeys, onOpenChange: setUserOpen })}
      items={items}
      onClick={({ key }) => go(key)} />
  )

  const logo = (short) => (
    <div
      role="button" tabIndex={0}
      onClick={() => go('/')}
      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') go('/') }}
      style={{
        height: 56, display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontWeight: 700, fontSize: 18, color: token.colorPrimary, cursor: 'pointer',
      }}>
      {short ? 'MWS' : 'MWS · Kho'}
    </div>
  )

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
    <Layout className="app-shell">
      {!isMobile && (
        <Sider collapsible collapsed={collapsed} trigger={null} theme="light"
          style={{ borderRight: `1px solid ${token.colorBorderSecondary}`, height: '100vh' }}>
          {logo(collapsed)}
          <div className="app-sider-menu">{menu}</div>
        </Sider>
      )}

      {isMobile && (
        <Drawer
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          placement="left"
          width={260}
          closable={false}
          styles={{ body: { padding: 0 }, header: { display: 'none' } }}>
          {logo(false)}
          <div className="app-drawer-menu">{menu}</div>
        </Drawer>
      )}

      <Layout style={{ height: '100vh' }}>
        <Header style={{
          background: token.colorBgContainer, padding: '0 12px', flex: '0 0 auto',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          borderBottom: `1px solid ${token.colorBorderSecondary}`,
        }}>
          <Button
            type="text"
            aria-label={isMobile ? 'Mở menu' : (collapsed ? 'Mở rộng menu' : 'Thu gọn menu')}
            onClick={() => (isMobile ? setDrawerOpen(true) : setCollapsed(v => !v))}
            icon={isMobile ? <MenuOutlined /> : (collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />)} />

          {/* Trên mobile không còn sidebar => hiện tên trang ở header cho khỏi lạc */}
          {isMobile && (
            <Typography.Text strong ellipsis style={{ flex: 1, margin: '0 8px' }}>
              {TITLE_BY_PATH[selectedKey] || 'MWS'}
            </Typography.Text>
          )}

          <div style={{ display: 'flex', alignItems: 'center', gap: isMobile ? 4 : 16 }}>
            {/* Kho có ca đêm => cho đổi sáng/tối. Lựa chọn lưu localStorage;
                mặc định theo cài đặt của hệ điều hành. */}
            <Tooltip title={isDark ? 'Chuyển sang nền sáng' : 'Chuyển sang nền tối'}>
              <Button type="text" onClick={toggleTheme}
                aria-label={isDark ? 'Chuyển sang nền sáng' : 'Chuyển sang nền tối'}
                icon={isDark ? <BulbFilled /> : <BulbOutlined />} />
            </Tooltip>
            <NotificationBell />
            <Dropdown menu={userMenu} trigger={['click']}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                <Avatar size="small" icon={<UserOutlined />} />
                {/* Ẩn tên trên màn hẹp để header không bị vỡ */}
                {!isMobile && <Typography.Text>{user?.fullName || user?.username}</Typography.Text>}
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content className="app-content" style={{ padding: isMobile ? 8 : 16 }}>
          <div style={{
            background: token.colorBgContainer, padding: isMobile ? 12 : 16,
            borderRadius: 8, minHeight: '100%',
          }}>
            {/* Lỗi render của 1 trang không được giết cả app: menu/header vẫn dùng được.
                key=pathname => tự reset boundary khi người dùng chuyển sang trang khác. */}
            <ErrorBoundary key={location.pathname} onGoHome={() => go('/')}>
              <Outlet />
            </ErrorBoundary>
          </div>
        </Content>
      </Layout>
    </Layout>
  )
}
