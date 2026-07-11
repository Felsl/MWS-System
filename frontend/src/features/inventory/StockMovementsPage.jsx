import FitTable from '../../components/FitTable'
import { useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useQuery, useInfiniteQuery } from '@tanstack/react-query'
import {
  Card, Select, Table, Space, Typography, Tag, Empty, Button, Modal,
} from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { getErrorMessage } from '../../api/client'
import { stockMovementsApi } from '../../api/stockMovements.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'
import { useBinLabels } from '../../hooks/useBinLabels'

// Màu theo dấu thay đổi tồn
const changeTag = (v) => <Tag color={v === 0 ? 'default' : v > 0 ? 'green' : 'red'}>{v > 0 ? `+${v}` : v}</Tag>

export default function StockMovementsPage() {
  const location = useLocation()
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const { labelOf } = useBinLabels()
  const productList = products.data?.content || []
  const productMap = useMemo(() => Object.fromEntries(productList.map(p => [p.id, p])), [productList])

  const [productId, setProductId] = useState(location.state?.productId || undefined)
  const [warehouseId, setWarehouseId] = useState(location.state?.warehouseId || undefined)
  const [refModal, setRefModal] = useState(null) // { referenceType, referenceId }

  const q = useInfiniteQuery({
    queryKey: ['kardex', productId, warehouseId],
    enabled: !!productId,
    queryFn: ({ pageParam }) => stockMovementsApi.byProduct({ productId, warehouseId, cursor: pageParam, size: 20 }),
    initialPageParam: undefined,
    getNextPageParam: (last) => (last.hasNext ? last.nextCursor : undefined),
  })
  const rows = (q.data?.pages || []).flatMap(p => p.content || [])

  const columns = [
    { title: 'Thời gian', dataIndex: 'createdAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
    { title: 'Loại', dataIndex: 'movementType', width: 140, render: (v) => <Tag>{v}</Tag> },
    { title: 'Thay đổi', dataIndex: 'quantityChange', width: 100, align: 'right', render: changeTag },
    { title: 'Trước', dataIndex: 'quantityBefore', width: 90, align: 'right' },
    { title: 'Sau', dataIndex: 'quantityAfter', width: 90, align: 'right' },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130, render: (v) => labelOf(v) },
    {
      title: 'Chứng từ', key: 'ref', width: 200,
      render: (_, r) => r.referenceId
        ? <a onClick={() => setRefModal({ referenceType: r.referenceType, referenceId: r.referenceId })}>
            {r.referenceType}: {r.referenceId}
          </a>
        : '—',
    },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 120, render: (v) => v || '—' },
    { title: 'Ghi chú', dataIndex: 'note', render: (v) => v || '—' },
  ]

  return (
    <div>
      <Typography.Title level={4} style={{ margin: '0 0 16px' }}>Thẻ kho (Kardex)</Typography.Title>
      <Space style={{ marginBottom: 12 }} wrap>
        <Select showSearch optionFilterProp="label" placeholder="Chọn sản phẩm" style={{ width: 280 }}
          loading={products.isLoading} value={productId} onChange={setProductId}
          options={productList.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))} />
        <Select allowClear showSearch optionFilterProp="label" placeholder="Lọc theo kho (tuỳ chọn)" style={{ width: 240 }}
          loading={warehouses.isLoading} value={warehouseId} onChange={setWarehouseId}
          options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
        {productId && <Button icon={<ReloadOutlined />} onClick={() => q.refetch()} loading={q.isFetching}>Làm mới</Button>}
      </Space>

      {!productId
        ? <Empty description="Chọn sản phẩm để xem thẻ kho" />
        : q.isError
          ? <Card><Empty description={getErrorMessage(q.error, 'Không tải được thẻ kho')} /></Card>
          : (
            <>
              <FitTable rowKey="id" loading={q.isLoading} dataSource={rows} columns={columns}
                pagination={false} size="small" bottomGap={80} />
              <div style={{ textAlign: 'center', marginTop: 12 }}>
                {q.hasNextPage
                  ? <Button onClick={() => q.fetchNextPage()} loading={q.isFetchingNextPage}>Tải thêm</Button>
                  : rows.length > 0 && <Typography.Text type="secondary">— Hết —</Typography.Text>}
              </div>
            </>
          )}

      <ReferenceModal state={refModal} onClose={() => setRefModal(null)} productMap={productMap} />
    </div>
  )
}

function ReferenceModal({ state, onClose, productMap }) {
  const open = !!state
  const { labelOf } = useBinLabels()
  const q = useQuery({
    queryKey: ['ref-moves', state?.referenceType, state?.referenceId],
    queryFn: () => stockMovementsApi.byReference(state.referenceType, state.referenceId),
    enabled: open,
  })
  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'Loại', dataIndex: 'movementType', width: 130, render: (v) => <Tag>{v}</Tag> },
    { title: 'Thay đổi', dataIndex: 'quantityChange', width: 100, align: 'right', render: changeTag },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130, render: (v) => labelOf(v) },
    { title: 'Thời gian', dataIndex: 'createdAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <Modal title={`Biến động theo chứng từ · ${state?.referenceType || ''} ${state?.referenceId || ''}`}
      open={open} onCancel={onClose} footer={null} width={760} destroyOnClose>
      <Table rowKey="id" size="small" loading={q.isLoading} dataSource={q.data || []} columns={columns}
        scroll={{ x: 'max-content' }} pagination={false} />
    </Modal>
  )
}
