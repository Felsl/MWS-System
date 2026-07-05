import { Routes, Route, Navigate } from 'react-router-dom'
import { Result, Button } from 'antd'
import ProtectedRoute from '../components/ProtectedRoute'
import AppLayout from '../components/AppLayout'
import { P } from '../constants/permissions'

import LoginPage from '../features/auth/LoginPage'
import DashboardPage from '../features/dashboard/DashboardPage'
import UsersPage from '../features/users/UsersPage'
import RolesPage from '../features/roles/RolesPage'
import PermissionsPage from '../features/permissions/PermissionsPage'
import WarehousesPage from '../features/warehouses/WarehousesPage'
import ProductsPage from '../features/products/ProductsPage'
import CategoriesPage from '../features/categories/CategoriesPage'
import PartnersPage from '../features/partners/PartnersPage'
import PurchaseOrdersPage from '../features/inbound/PurchaseOrdersPage'
import GoodsReceiptsPage from '../features/inbound/GoodsReceiptsPage'

function NotificationsPlaceholder() {
  return <Result title="Thông báo" subTitle="Trung tâm thông báo realtime (WebSocket/STOMP) sẽ nối ở Giai đoạn 7." />
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* Khu vực cần đăng nhập */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="notifications" element={<NotificationsPlaceholder />} />

          <Route element={<ProtectedRoute permission={P.MASTER_PRODUCT_VIEW} />}>
            <Route path="products" element={<ProductsPage />} />
            <Route path="categories" element={<CategoriesPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.MASTER_PARTNER_MANAGE} />}>
            <Route path="partners" element={<PartnersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.INBOUND_VIEW_PO} />}>
            <Route path="purchase-orders" element={<PurchaseOrdersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.INBOUND_VIEW_GRN} />}>
            <Route path="goods-receipts" element={<GoodsReceiptsPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.WAREHOUSE_VIEW} />}>
            <Route path="warehouses" element={<WarehousesPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.USER_VIEW} />}>
            <Route path="users" element={<UsersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.ROLE_VIEW} />}>
            <Route path="roles" element={<RolesPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.PERMISSION_VIEW} />}>
            <Route path="permissions" element={<PermissionsPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={
        <Result status="404" title="404" subTitle="Không tìm thấy trang."
          extra={<Button type="primary" href="/">Về trang chủ</Button>} />
      } />
    </Routes>
  )
}
