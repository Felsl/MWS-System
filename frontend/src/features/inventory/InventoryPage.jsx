import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Tabs, Select, Table, Space, Typography, Tag, Empty, Button, App as AntdApp,
} from 'antd'
import { ReloadOutlined, HistoryOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { inventoryApi } from '../../api/inventory.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'

const BATCH_STATUS = {
  ACTIVE: { color: 'green', label: 'Hoạt động' },
  HOLD: { color: 'orange', label: 'Niêm phong' },
  EXPIRED: { color: 'red', label: 'Hết hạn' },
}

function useLookups() {
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const productList = products.data?.content || []
  const productMap = useMemo(() => Object.fromEntries(productList.map(p => [p.id, p])), [productList])
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
  const navigate = useNavigate()
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
    { title: 'Tồn', dataIndex: 'quantity', width: 100, align: 'right' },
    { title: 'Đang giữ', dataIndex: 'reservedQuantity', width: 110, align: 'right' },
    {
      title: 'Khả dụng', dataIndex: 'availableQuantity', width: 130, align: 'right',
      render: (v, r) => {
        const safety = productMap[r.productId]?.safetyStock
        const low = safety != null && v < safety
        return <Tag color={low ? 'red' : 'blue'}>{v}{low ? ' ⚠' : ''}</Tag>
      },
    },
    {
      title: '', key: '_a', width: 90,
      render: (_, r) => (
        <Can permission={P.AUDIT_VIEW_MOVEMENTS}>
          <a onClick={() => navigate('/stock-movements', { state: { productId: r.productId, warehouseId } })}>
            <HistoryOutlined /> Thẻ kho
          </a>
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
        : <Table rowKey="productId" loading={inv.isLoading} dataSource={inv.data || []} columns={columns}
            scroll={{ x: 'max-content' }} pagination={{ pageSize: 20, showSizeChanger: true }} />}
    </>
  )
}

function BatchView() {
  const { message } = AntdApp.useApp()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { hasPermission } = useAuth()
  const { products, warehouses, productList, productMap } = useLookups()
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
  const nearExpiry = (d) => d && dayjs(d).diff(dayjs(), 'day') <= 30

  const columns = [
    { title: 'Số lô', dataIndex: 'batchNumber', width: 150 },
    { title: 'Ô kệ', dataIndex: 'binLocation', width: 150, render: (v, r) => v || r.binLocationId || '—' },
    { title: 'SL', dataIndex: 'quantity', width: 90, align: 'right' },
    { title: 'NSX', dataIndex: 'manufacturedDate', width: 120, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    {
      title: 'HSD', dataIndex: 'expiryDate', width: 130,
      render: (v) => v ? <span style={{ color: nearExpiry(v) ? '#cf1322' : undefined }}>{dayjs(v).format('DD/MM/YYYY')}{nearExpiry(v) ? ' ⚠' : ''}</span> : '—',
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
          <a onClick={() => navigate('/stock-movements', { state: { productId, warehouseId } })}><HistoryOutlined /> Thẻ kho SP này</a>
        )}
      </Space>
      {!enabled
        ? <Empty description="Chọn sản phẩm và kho để xem lô" />
        : <Table rowKey="id" loading={batches.isLoading} dataSource={batches.data || []} columns={columns}
            scroll={{ x: 'max-content' }} pagination={{ pageSize: 20 }} />}
      {enabled && !canAdjust && (
        <Typography.Text type="secondary">* Cần quyền INVENTORY_ADJUST để niêm phong / mở lô.</Typography.Text>
      )}
    </>
  )
}

