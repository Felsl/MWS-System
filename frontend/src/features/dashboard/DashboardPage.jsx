import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Typography, Card, Col, Row, Statistic, List, Tag, Empty, Grid, Skeleton, Button } from 'antd'
import {
  AppstoreOutlined, HomeOutlined, FileDoneOutlined, ShoppingCartOutlined,
  SwapOutlined, ReconciliationOutlined, RightOutlined, WarningOutlined, BarChartOutlined,
  LineChartOutlined, ClockCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import { useAuth } from '../../auth/AuthContext'
import { useNotifications } from '../../context/NotificationContext'
import { P } from '../../constants/permissions'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'
import { purchaseOrdersApi } from '../../api/purchaseOrders.api'
import { salesOrdersApi } from '../../api/salesOrders.api'
import { transferOrdersApi } from '../../api/transferOrders.api'
import { adjustmentsApi } from '../../api/adjustments.api'
import { useStockOverview } from './useStockOverview'
import { useProducts } from '../../hooks/useProducts'
import StockBarChart from '../../components/StockBarChart'
import StockLineChart from '../../components/StockLineChart'

/**
 * Thẻ KPI CLICK ĐƯỢC.
 * Trước đây các thẻ chỉ là con số chết: thấy "PO chờ duyệt: 7" nhưng không đi
 * đâu được, phải tự vào menu rồi tự lọc lại đúng trạng thái đó. Nay bấm vào thẻ
 * là sang thẳng trang danh sách ĐÃ LỌC SẴN (truyền qua location.state — đúng
 * quy ước sẵn có của dự án, xem InventoryPage -> StockMovementsPage).
 */
function KpiCard({ icon, title, q, to, hint }) {
  const navigate = useNavigate()
  // `to` đã chứa sẵn query lọc (vd '/purchase-orders?status=PENDING_APPROVAL').
  // Dùng query string thay vì location.state: state chết sau F5, query thì không,
  // và người duyệt có thể bookmark/gửi lại đúng danh sách đó.
  const go = () => navigate(to)
  const value = q.isError ? '—' : (q.data ?? (q.isLoading ? undefined : '—'))

  return (
    <Col xs={12} sm={12} md={8} lg={6}>
      <Card
        hoverable
        role="button"
        tabIndex={0}
        aria-label={`${title}: ${value ?? 'đang tải'}. Bấm để xem danh sách.`}
        onClick={go}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); go() } }}
        styles={{ body: { padding: 16 } }}>
        <Statistic
          title={<span>{icon} {title}</span>}
          value={value}
          loading={q.isLoading}
        />
        <Typography.Text type="secondary" style={{ fontSize: 12 }}>
          {hint || 'Xem danh sách'} <RightOutlined style={{ fontSize: 10 }} />
        </Typography.Text>
      </Card>
    </Col>
  )
}

export default function DashboardPage() {
  const { user, hasPermission } = useAuth()
  const { notifications } = useNotifications()
  const screens = Grid.useBreakpoint()

  const has = (p) => hasPermission(p)
  const totalOf = (r) => r?.totalElements ?? 0

  const products = useQuery({ queryKey: ['kpi-products'], enabled: has(P.MASTER_PRODUCT_VIEW),
    queryFn: () => productsApi.list({ size: 1 }).then(totalOf) })
  const warehouses = useQuery({ queryKey: ['kpi-warehouses'], enabled: has(P.WAREHOUSE_VIEW),
    queryFn: () => warehousesApi.list(false).then(a => a.length) })
  const poPending = useQuery({ queryKey: ['kpi-po'], enabled: has(P.INBOUND_VIEW_PO),
    queryFn: () => purchaseOrdersApi.list({ status: 'PENDING_APPROVAL', size: 1 }).then(totalOf) })
  const soDraft = useQuery({ queryKey: ['kpi-so'], enabled: has(P.OUTBOUND_VIEW_SO),
    queryFn: () => salesOrdersApi.list({ status: 'DRAFT', size: 1 }).then(totalOf) })
  const toPending = useQuery({ queryKey: ['kpi-to'], enabled: has(P.TRANSFER_VIEW),
    queryFn: () => transferOrdersApi.list({ status: 'PENDING_APPROVAL', size: 1 }).then(totalOf) })
  const avDraft = useQuery({ queryKey: ['kpi-av'], enabled: has(P.ADJUSTMENT_VIEW),
    queryFn: () => adjustmentsApi.list({ status: 'DRAFT', size: 1 }).then(totalOf) })

  const cards = [
    has(P.MASTER_PRODUCT_VIEW) && (
      <KpiCard key="p" icon={<AppstoreOutlined />} title="Sản phẩm" q={products}
        to="/products" hint="Quản lý sản phẩm" />
    ),
    has(P.WAREHOUSE_VIEW) && (
      <KpiCard key="w" icon={<HomeOutlined />} title="Kho" q={warehouses}
        to="/warehouses" hint="Kho & ô kệ" />
    ),
    has(P.INBOUND_VIEW_PO) && (
      <KpiCard key="po" icon={<FileDoneOutlined />} title="PO chờ duyệt" q={poPending}
        to="/purchase-orders?status=PENDING_APPROVAL" hint="Đơn chờ duyệt" />
    ),
    has(P.OUTBOUND_VIEW_SO) && (
      <KpiCard key="so" icon={<ShoppingCartOutlined />} title="Đơn bán nháp" q={soDraft}
        to="/sales-orders?status=DRAFT" hint="Đơn bán nháp" />
    ),
    has(P.TRANSFER_VIEW) && (
      <KpiCard key="to" icon={<SwapOutlined />} title="Điều chuyển chờ duyệt" q={toPending}
        to="/transfer-orders?status=PENDING_APPROVAL" hint="Phiếu chờ duyệt" />
    ),
    // NOTE: thẻ này trước đây ghi "chờ duyệt" nhưng lại đếm status=DRAFT.
    // Đã sửa nhãn cho khớp truy vấn (nhấn vào là lọc đúng DRAFT).
    has(P.ADJUSTMENT_VIEW) && (
      <KpiCard key="av" icon={<ReconciliationOutlined />} title="Điều chỉnh nháp" q={avDraft}
        to="/adjustment-vouchers?status=DRAFT" hint="Phiếu nháp" />
    ),
  ].filter(Boolean)

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 4 }}>
        Xin chào, {user?.fullName || user?.username}
      </Typography.Title>
      <Typography.Paragraph type="secondary">
        Vai trò: <b>{user?.role}</b> · Số quyền: {user?.permissions?.length || 0}
      </Typography.Paragraph>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        {cards.length ? cards : (
          <Col span={24}>
            <Card><Empty description="Không có số liệu phù hợp với quyền của bạn" /></Card>
          </Col>
        )}
      </Row>

      {/* Khu tồn kho: biểu đồ + cảnh báo, dùng CHUNG một lần lấy dữ liệu. */}
      {has(P.INVENTORY_VIEW) && has(P.WAREHOUSE_VIEW) && <StockSection />}

      <Card title="Thông báo gần đây" style={{ marginTop: 16 }}>
        {(!notifications || notifications.length === 0)
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có thông báo" />
          : (
            <List size="small" dataSource={notifications.slice(0, screens.lg ? 6 : 4)}
              renderItem={(n) => (
                <List.Item>
                  <List.Item.Meta
                    title={<span>{!n.isRead && <Tag color="blue">Mới</Tag>}{n.title || n.type || 'Thông báo'}</span>}
                    description={n.message} />
                  <Typography.Text type="secondary" style={{ fontSize: 12, whiteSpace: 'nowrap' }}>
                    {n.createdAt ? dayjs(n.createdAt).format('DD/MM HH:mm') : ''}
                  </Typography.Text>
                </List.Item>
              )} />
          )}
      </Card>
    </div>
  )
}


/**
 * Khu tồn kho trên Dashboard, 2 hàng dùng CHUNG một hook (useStockOverview):
 *   Hàng 1: tồn theo kho (cột) + cảnh báo dưới tồn an toàn.
 *   Hàng 2 [MỤC 6]: biểu đồ đường Xuất-Nhập-Tồn 30 ngày + lô sắp hết hạn.
 */
function StockSection() {
  const navigate = useNavigate()
  const { isLoading, isError, byWarehouse, lowStock, truncated, expiring, summary } = useStockOverview()
  const { map: productMap } = useProducts()
  const whName = (id) => byWarehouse.find(w => w.id === id)?.name || id

  return (
    <>
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card
            title={<span><BarChartOutlined /> Tồn kho theo kho</span>}
            extra={<Button type="link" size="small" style={{ padding: 0 }} onClick={() => navigate('/inventory')}>Chi tiết</Button>}>
            {isLoading
              ? <Skeleton active paragraph={{ rows: 4 }} title={false} />
              : isError
                ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không tải được tồn kho" />
                : <>
                    <StockBarChart data={byWarehouse} />
                    {truncated && (
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        Chỉ hiển thị 12 kho đầu tiên.
                      </Typography.Text>
                    )}
                  </>}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            title={<span><WarningOutlined /> Dưới mức tồn an toàn</span>}
            extra={!isLoading && lowStock.length > 0 && <Tag color="red">{lowStock.length}</Tag>}>
            {isLoading
              ? <Skeleton active paragraph={{ rows: 4 }} title={false} />
              : lowStock.length === 0
                ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không có sản phẩm nào dưới mức an toàn" />
                : (
                  <>
                    <List size="small" dataSource={lowStock.slice(0, 5)}
                      renderItem={(r) => (
                        <List.Item
                          className="mws-line-clickable"
                          onClick={() => navigate('/inventory')}
                          actions={[<Tag key="d" color="red">thiếu {r.deficit}</Tag>]}>
                          <List.Item.Meta
                            title={<span>{r.productName}{r.sku ? ` · ${r.sku}` : ''}</span>}
                            description={`${r.warehouseName} — còn ${r.available}, mức an toàn ${r.safety}`} />
                        </List.Item>
                      )} />
                    {lowStock.length > 5 && (
                      <Button type="link" style={{ paddingLeft: 0 }} onClick={() => navigate('/inventory')}>
                        Xem tất cả {lowStock.length} dòng <RightOutlined />
                      </Button>
                    )}
                  </>
                )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title={<span><LineChartOutlined /> Xuất-Nhập-Tồn ({summary.days} ngày)</span>}>
            {summary.isLoading
              ? <Skeleton active paragraph={{ rows: 4 }} title={false} />
              : summary.isError
                ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không tải được báo cáo" />
                : <StockLineChart data={summary.data} />}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            title={<span><ClockCircleOutlined /> Lô sắp hết hạn (≤ {expiring.days} ngày)</span>}
            extra={!expiring.isLoading && expiring.data.length > 0 && <Tag color="volcano">{expiring.data.length}</Tag>}>
            {expiring.isLoading
              ? <Skeleton active paragraph={{ rows: 4 }} title={false} />
              : expiring.isError
                ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không tải được danh sách lô" />
                : expiring.data.length === 0
                  ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không có lô nào sắp hết hạn" />
                  : (
                    <>
                      <List size="small" dataSource={expiring.data.slice(0, 5)}
                        renderItem={(b) => {
                          const dleft = dayjs(b.expiryDate).startOf('day').diff(dayjs().startOf('day'), 'day')
                          const color = dleft < 0 ? 'red' : dleft <= 7 ? 'volcano' : 'gold'
                          const text = dleft < 0 ? `Quá hạn ${-dleft}n` : `Còn ${dleft}n`
                          return (
                            <List.Item
                              className="mws-line-clickable"
                              onClick={() => navigate('/inventory')}
                              actions={[<Tag key="d" color={color}>{text}</Tag>]}>
                              <List.Item.Meta
                                title={<span>{productMap[b.productId]?.name || b.productId}{b.batchNumber ? ` · ${b.batchNumber}` : ''}</span>}
                                description={`${whName(b.warehouseId)} — còn ${b.quantity}, HSD ${dayjs(b.expiryDate).format('DD/MM/YYYY')}`} />
                            </List.Item>
                          )
                        }} />
                      {expiring.data.length > 5 && (
                        <Button type="link" style={{ paddingLeft: 0 }} onClick={() => navigate('/inventory')}>
                          Xem tất cả {expiring.data.length} lô <RightOutlined />
                        </Button>
                      )}
                    </>
                  )}
          </Card>
        </Col>
      </Row>
    </>
  )
}
