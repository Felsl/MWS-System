import FitTable from '../../components/FitTable'
import { useState } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Tabs, Select, Space, Typography, Tag, Empty, Button, Tooltip, theme, App as AntdApp,
} from 'antd'
import { ReloadOutlined, HistoryOutlined, WarningOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { inventoryApi } from '../../api/inventory.api'
import { warehousesApi } from '../../api/warehouses.api'

const BATCH_STATUS = {
  ACTIVE: { color: 'green', label: 'Hoạt động' },
  HOLD: { color: 'orange', label: 'Niêm phong' },
  EXPIRED: { color: 'red', label: 'Hết hạn' },
}

function useLookups() {
  const { query: products, list: productList, map: productMap } = useProducts()
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  return { products, warehouses, productList, productMap }
}

export default function InventoryPage() {
  return (
    <div>
      <Typography.Title level={4} style={{ margin: '0 0 16px' }}>Tồn kho</Typography.Title>
      <Tabs
        items={[
          { key: 'stock', label: 'Tồn theo kho', children: <StockByWarehouse /> },
          { key: 'batch', label: 'Lô hàng', children: <BatchView /> },
        ]}
      />
    </div>
  )
}

function StockByWarehouse() {
  const { warehouses, productMap } = useLookups()
  const [warehouseId, setWarehouseId] = useState()

  const inv = useQuery({
    queryKey: ['inv-wh', warehouseId],
    queryFn: () => inventoryApi.getByWarehouse(warehouseId),
    enabled: !!warehouseId,
  })

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SKU', dataIndex: 'productId', width: 130, render: (pid) => productMap[pid]?.sku || '—' },
    // NCC gộp (suy ở BE từ GRN->PO); một SP có thể từ nhiều NCC -> "A / B". '—' nếu không truy được nguồn.
    { title: 'Nhà cung cấp', dataIndex: 'supplierName', width: 180, ellipsis: true, render: (v) => v || '—' },
    { title: 'Tồn', dataIndex: 'quantity', width: 100, align: 'right' },
    { title: 'Đang giữ', dataIndex: 'reservedQuantity', width: 110, align: 'right' },
    {
      title: 'Khả dụng', dataIndex: 'availableQuantity', width: 130, align: 'right',
      render: (v, r) => {
        const safety = productMap[r.productId]?.safetyStock
        const low = safety != null && v < safety
        if (!low) return <Tag color="blue">{v}</Tag>
        // TRƯỚC: chỉ dán ký tự ' ⚠' vào sau số. Người dùng thấy cảnh báo nhưng
        // không biết ngưỡng là bao nhiêu, thiếu bao nhiêu, hay phải làm gì.
        return (
          <Tooltip title={`Dưới tồn an toàn: còn ${v}, mức an toàn là ${safety} (thiếu ${safety - v})`}>
            <Tag color="red" icon={<WarningOutlined />}>{v}</Tag>
          </Tooltip>
        )
      },
    },
    {
      title: '', key: '_a', width: 90,
      render: (_, r) => (
        <Can permission={P.AUDIT_VIEW_MOVEMENTS}>
          <Link to={`/stock-movements?productId=${r.productId}${warehouseId ? `&warehouseId=${warehouseId}` : ''}`}>
            <HistoryOutlined /> Thẻ kho
          </Link>
        </Can>
      ),
    },
  ]

  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <Select showSearch optionFilterProp="label" placeholder="Chọn kho" style={{ width: 260 }}
          loading={warehouses.isLoading} value={warehouseId} onChange={setWarehouseId}
          options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
        {warehouseId && <Button onClick={() => inv.refetch()} loading={inv.isFetching}>Làm mới</Button>}
      </Space>
      {!warehouseId
        ? <Empty description="Chọn kho để xem tồn" />
        : <FitTable rowKey="productId" loading={inv.isLoading} dataSource={inv.data || []} columns={columns}
            scroll={{ x: 'max-content' }} pagination={{ pageSize: 20, showSizeChanger: true }} />}
    </>
  )
}

function BatchView() {
  const { message } = AntdApp.useApp()
  const { token } = theme.useToken()   // màu cảnh báo theo token => đúng ở cả nền tối
  const qc = useQueryClient()
  const { hasPermission } = useAuth()
  const { products, warehouses, productList } = useLookups()
  const [productId, setProductId] = useState()
  const [warehouseId, setWarehouseId] = useState()

  const enabled = !!productId && !!warehouseId
  const batches = useQuery({
    queryKey: ['batches', productId, warehouseId],
    queryFn: () => inventoryApi.getBatches(productId, warehouseId),
    enabled,
  })

  const statusMut = useMutation({
    mutationFn: ({ batchId, status }) => inventoryApi.updateBatchStatus(batchId, status),
    onSuccess: () => { message.success('Đã đổi trạng thái lô'); qc.invalidateQueries({ queryKey: ['batches', productId, warehouseId] }) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const canAdjust = hasPermission(P.INVENTORY_ADJUST)
  // Ngưỡng cảnh báo cận hạn. Đặt tên hằng để con số 30 xuất hiện đúng 1 chỗ và
  // hiện được ra tooltip cho người dùng, thay vì nằm ẩn trong code.
  const EXPIRY_WARN_DAYS = 30
  const daysLeft = (d) => dayjs(d).diff(dayjs(), 'day')
  const nearExpiry = (d) => d && daysLeft(d) <= EXPIRY_WARN_DAYS

  const columns = [
    { title: 'Số lô', dataIndex: 'batchNumber', width: 150 },
    // Nhà cung cấp suy ra ở BE lúc query (GRN -> PO -> supplier); '—' khi lô không
    // truy được nguồn (tạo tay / điều chuyển / điều chỉnh, không qua phiếu nhập).
    { title: 'Nhà cung cấp', dataIndex: 'supplierName', width: 200, ellipsis: true, render: (v) => v || '—' },
    { title: 'Ô kệ', dataIndex: 'binLocation', width: 150, render: (v, r) => v || r.binLocationId || '—' },
    { title: 'SL', dataIndex: 'quantity', width: 90, align: 'right' },
    { title: 'NSX', dataIndex: 'manufacturedDate', width: 120, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    {
      title: 'HSD', dataIndex: 'expiryDate', width: 130,
      render: (v) => {
        if (!v) return '—'
        const text = dayjs(v).format('DD/MM/YYYY')
        if (!nearExpiry(v)) return <span>{text}</span>
        const d = daysLeft(v)
        const tip = d < 0 ? `Đã quá hạn ${Math.abs(d)} ngày`
          : d === 0 ? 'Hết hạn hôm nay'
            : `Còn ${d} ngày là hết hạn (cảnh báo khi dưới ${EXPIRY_WARN_DAYS} ngày)`
        return (
          <Tooltip title={tip}>
            <span style={{ color: token.colorError, whiteSpace: 'nowrap' }}>
              <WarningOutlined /> {text}
            </span>
          </Tooltip>
        )
      },
    },
    {
      title: 'Trạng thái', dataIndex: 'status', width: 160,
      render: (s, r) => canAdjust
        ? <Select size="small" value={s} style={{ width: 140 }} loading={statusMut.isPending}
            onChange={(val) => statusMut.mutate({ batchId: r.id, status: val })}
            options={Object.entries(BATCH_STATUS).map(([value, m]) => ({ value, label: m.label }))} />
        : <Tag color={BATCH_STATUS[s]?.color}>{BATCH_STATUS[s]?.label || s}</Tag>,
    },
  ]

  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <Select showSearch optionFilterProp="label" placeholder="Chọn sản phẩm" style={{ width: 280 }}
          loading={products.isLoading} value={productId} onChange={setProductId}
          options={productList.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))} />
        <Select showSearch optionFilterProp="label" placeholder="Chọn kho" style={{ width: 220 }}
          loading={warehouses.isLoading} value={warehouseId} onChange={setWarehouseId}
          options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
        {enabled && <Button icon={<ReloadOutlined />} onClick={() => batches.refetch()} loading={batches.isFetching} />}
        {productId && hasPermission(P.AUDIT_VIEW_MOVEMENTS) && (
          <Link to={`/stock-movements?productId=${productId}${warehouseId ? `&warehouseId=${warehouseId}` : ''}`}><HistoryOutlined /> Thẻ kho SP này</Link>
        )}
      </Space>
      {!enabled
        ? <Empty description="Chọn sản phẩm và kho để xem lô" />
        : <FitTable rowKey="id" loading={batches.isLoading} dataSource={batches.data || []} columns={columns}
            scroll={{ x: 'max-content' }} pagination={{ pageSize: 20 }} />}
      {enabled && !canAdjust && (
        <Typography.Text type="secondary">* Cần quyền INVENTORY_ADJUST để niêm phong / mở lô.</Typography.Text>
      )}
    </>
  )
}

