import { lazy, Suspense } from 'react'
import { Routes, Route } from 'react-router-dom'
import { Result, Button, Spin } from 'antd'
import ProtectedRoute from '../components/ProtectedRoute'
import AppLayout from '../components/AppLayout'
import { P } from '../constants/permissions'

/**
 * MỌI trang đều nạp động (React.lazy).
 *
 * Trước đây 24 trang + antd + xlsx + zxing + sockjs gói chung 1 file 2,66MB:
 * người dùng chỉ vào xem tồn kho vẫn phải tải cả mã của trang quét mã vạch và
 * trang nhập Excel. Nay mỗi trang là 1 chunk riêng, chỉ tải khi thật sự mở.
 *
 * Riêng LoginPage cũng lazy: người CHƯA đăng nhập không cần mã của 23 trang kia.
 */
const LoginPage = lazy(() => import('../features/auth/LoginPage'))
const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage'))
const UsersPage = lazy(() => import('../features/users/UsersPage'))
const RolesPage = lazy(() => import('../features/roles/RolesPage'))
const PermissionsPage = lazy(() => import('../features/permissions/PermissionsPage'))
const WarehousesPage = lazy(() => import('../features/warehouses/WarehousesPage'))
const ProductsPage = lazy(() => import('../features/products/ProductsPage'))
const CategoriesPage = lazy(() => import('../features/categories/CategoriesPage'))
const PartnersPage = lazy(() => import('../features/partners/PartnersPage'))
const PurchaseOrdersPage = lazy(() => import('../features/inbound/PurchaseOrdersPage'))
const GoodsReceiptsPage = lazy(() => import('../features/inbound/GoodsReceiptsPage'))
const SalesOrdersPage = lazy(() => import('../features/outbound/SalesOrdersPage'))
const PickingListsPage = lazy(() => import('../features/outbound/PickingListsPage'))
const PickingScanPage = lazy(() => import('../features/outbound/PickingScanPage'))
const ShipmentsPage = lazy(() => import('../features/outbound/ShipmentsPage'))
const TransferOrdersPage = lazy(() => import('../features/transfer/TransferOrdersPage'))
const StocktakesPage = lazy(() => import('../features/stocktake/StocktakesPage'))
const AdjustmentsPage = lazy(() => import('../features/stocktake/AdjustmentsPage'))
const InventoryPage = lazy(() => import('../features/inventory/InventoryPage'))
const BarcodeLabelsPage = lazy(() => import('../features/inventory/BarcodeLabelsPage'))
const StockMovementsPage = lazy(() => import('../features/inventory/StockMovementsPage'))
const NotificationsPage = lazy(() => import('../features/notifications/NotificationsPage'))
const DataIOPage = lazy(() => import('../features/data-io/DataIOPage'))
const DebugPage = lazy(() => import('../features/debug/DebugPage'))

function PageLoading() {
  return <div style={{ display: 'grid', placeItems: 'center', minHeight: 240 }}><Spin size="large" /></div>
}

/**
 * Trang phiếu có 3 chế độ (danh sách / tạo mới / chi tiết) — nay là 3 URL thật
 * thay vì useState, để F5 không mất phiếu đang mở và Back hoạt động đúng.
 * Route tĩnh 'new' đặt trước ':id' (React Router tự ưu tiên tĩnh, liệt kê rõ
 * ra đây cho dễ đọc).
 */
function recordRoutes(path, Element, { create = true } = {}) {
  return [
    <Route key={path} path={path} element={<Element />} />,
    // Stocktake tạo phiên qua modal, Adjustment không có màn tạo => không có /new.
    create && <Route key={`${path}/new`} path={`${path}/new`} element={<Element />} />,
    <Route key={`${path}/:id`} path={`${path}/:id`} element={<Element />} />,
  ].filter(Boolean)
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<PageLoading />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        {/* Khu vực cần đăng nhập */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="debug" element={<DebugPage />} />
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
              {recordRoutes('purchase-orders', PurchaseOrdersPage)}
            </Route>
            <Route element={<ProtectedRoute permission={P.INBOUND_VIEW_GRN} />}>
              <Route path="goods-receipts" element={<GoodsReceiptsPage />} />
            </Route>
            <Route element={<ProtectedRoute permission={P.OUTBOUND_VIEW_SO} />}>
              {recordRoutes('sales-orders', SalesOrdersPage)}
            </Route>
            <Route element={<ProtectedRoute permission={P.OUTBOUND_VIEW} />}>
              {recordRoutes('picking-lists', PickingListsPage)}
              {recordRoutes('shipments', ShipmentsPage)}
            </Route>
            <Route element={<ProtectedRoute permission={P.OUTBOUND_PICK} />}>
              <Route path="picking-scan" element={<PickingScanPage />} />
            </Route>
            <Route element={<ProtectedRoute permission={P.TRANSFER_VIEW} />}>
              {recordRoutes('transfer-orders', TransferOrdersPage)}
            </Route>
            <Route element={<ProtectedRoute permission={P.STOCKTAKE_VIEW} />}>
              {recordRoutes('stocktakes', StocktakesPage, { create: false })}
            </Route>
            <Route element={<ProtectedRoute permission={P.ADJUSTMENT_VIEW} />}>
              {recordRoutes('adjustment-vouchers', AdjustmentsPage, { create: false })}
            </Route>
            <Route element={<ProtectedRoute permission={P.INVENTORY_VIEW} />}>
              <Route path="inventory" element={<InventoryPage />} />
              <Route path="barcode-labels" element={<BarcodeLabelsPage />} />
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
    </Suspense>
  )
}
