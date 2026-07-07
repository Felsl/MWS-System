import { useQuery } from '@tanstack/react-query'
import { Typography, Card, Col, Row, Statistic, List, Tag, Empty } from 'antd'
import {
  AppstoreOutlined, HomeOutlined, FileDoneOutlined, ShoppingCartOutlined,
  SwapOutlined, ReconciliationOutlined,
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

function KpiCard({ icon, title, q, suffix }) {
  return (
    <Col xs={12} md={8} lg={6}>
      <Card>
        <Statistic
          title={<span>{icon} {title}</span>}
          value={q.isError ? '—' : (q.data ?? (q.isLoading ? undefined : '—'))}
          loading={q.isLoading}
          suffix={suffix}
        />
      </Card>
    </Col>
  )
}

export default function DashboardPage() {
  const { user, hasPermission } = useAuth()
  const { notifications } = useNotifications()

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
    has(P.MASTER_PRODUCT_VIEW) && <KpiCard key="p" icon={<AppstoreOutlined />} title="Sản phẩm" q={products} />,
    has(P.WAREHOUSE_VIEW) && <KpiCard key="w" icon={<HomeOutlined />} title="Kho" q={warehouses} />,
    has(P.INBOUND_VIEW_PO) && <KpiCard key="po" icon={<FileDoneOutlined />} title="PO chờ duyệt" q={poPending} />,
    has(P.OUTBOUND_VIEW_SO) && <KpiCard key="so" icon={<ShoppingCartOutlined />} title="Đơn bán nháp" q={soDraft} />,
    has(P.TRANSFER_VIEW) && <KpiCard key="to" icon={<SwapOutlined />} title="Điều chuyển chờ duyệt" q={toPending} />,
    has(P.ADJUSTMENT_VIEW) && <KpiCard key="av" icon={<ReconciliationOutlined />} title="Điều chỉnh chờ duyệt" q={avDraft} />,
  ].filter(Boolean)

  return (
    <div>
      <Typography.Title level={4} style={{ marginBottom: 4 }}>Xin chào, {user?.fullName || user?.username}</Typography.Title>
      <Typography.Paragraph type="secondary">
        Vai trò: <b>{user?.role}</b> · Số quyền: {user?.permissions?.length || 0}
      </Typography.Paragraph>

      <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
        {cards.length ? cards : <Col span={24}><Card><Empty description="Không có số liệu phù hợp với quyền của bạn" /></Card></Col>}
      </Row>

      <Card title="Thông báo gần đây" style={{ marginTop: 16 }}>
        {(!notifications || notifications.length === 0)
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có thông báo" />
          : (
            <List size="small" dataSource={notifications.slice(0, 6)}
              renderItem={(n) => (
                <List.Item>
                  <List.Item.Meta
                    title={<span>{!n.isRead && <Tag color="blue">Mới</Tag>}{n.title || n.type || 'Thông báo'}</span>}
                    description={n.message} />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {n.createdAt ? dayjs(n.createdAt).format('DD/MM HH:mm') : ''}
                  </Typography.Text>
                </List.Item>
              )} />
          )}
      </Card>
    </div>
  )
}
