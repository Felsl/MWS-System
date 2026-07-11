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
import SalesOrdersPage from '../features/outbound/SalesOrdersPage'
import PickingListsPage from '../features/outbound/PickingListsPage'
import PickingScanPage from '../features/outbound/PickingScanPage'
import ShipmentsPage from '../features/outbound/ShipmentsPage'
import TransferOrdersPage from '../features/transfer/TransferOrdersPage'
import StocktakesPage from '../features/stocktake/StocktakesPage'
import AdjustmentsPage from '../features/stocktake/AdjustmentsPage'
import InventoryPage from '../features/inventory/InventoryPage'
import StockMovementsPage from '../features/inventory/StockMovementsPage'
import NotificationsPage from '../features/notifications/NotificationsPage'
import DataIOPage from '../features/data-io/DataIOPage'

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* Khu vực cần đăng nhập */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="notifications" element={<NotificationsPage />} />
          <Route element={<ProtectedRoute permission={P.MASTER_PRODUCT_MANAGE} />}>
            <Route path="data-io" element={<DataIOPage />} />
          </Route>

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
          <Route element={<ProtectedRoute permission={P.OUTBOUND_VIEW_SO} />}>
            <Route path="sales-orders" element={<SalesOrdersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.OUTBOUND_VIEW} />}>
            <Route path="picking-lists" element={<PickingListsPage />} />
            <Route path="shipments" element={<ShipmentsPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.OUTBOUND_PICK} />}>
            <Route path="picking-scan" element={<PickingScanPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.TRANSFER_VIEW} />}>
            <Route path="transfer-orders" element={<TransferOrdersPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.STOCKTAKE_VIEW} />}>
            <Route path="stocktakes" element={<StocktakesPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.ADJUSTMENT_VIEW} />}>
            <Route path="adjustment-vouchers" element={<AdjustmentsPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.INVENTORY_VIEW} />}>
            <Route path="inventory" element={<InventoryPage />} />
          </Route>
          <Route element={<ProtectedRoute permission={P.AUDIT_VIEW_MOVEMENTS} />}>
            <Route path="stock-movements" element={<StockMovementsPage />} />
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
