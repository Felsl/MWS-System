import { useMemo, useState } from 'react'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Select, Table, Space, Typography, Tag, Descriptions, Empty,
  Popconfirm, App as AntdApp,
} from 'antd'
import { SearchOutlined, ReloadOutlined, ArrowLeftOutlined, CheckOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { adjustmentsApi } from '../../api/adjustments.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'

const AV_STATUS = {
  DRAFT: { color: 'gold', label: 'Chờ duyệt' },
  APPROVED: { color: 'green', label: 'Đã duyệt' },
  REJECTED: { color: 'red', label: 'Bị từ chối' },
}
const avTag = (s) => <Tag color={AV_STATUS[s]?.color || 'default'}>{AV_STATUS[s]?.label || s}</Tag>
const AV_STATUS_OPTS = Object.entries(AV_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useMaps() {
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  const productMap = useMemo(() => Object.fromEntries((products.data?.content || []).map(p => [p.id, p])), [products.data])
  return { warehouseMap, productMap }
}

export default function AdjustmentsPage() {
  const [view, setView] = useState({ mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {view.mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
          )}
          <Typography.Title level={4} style={{ margin: 0 }}>Phiếu điều chỉnh tồn</Typography.Title>
        </Space>
      </div>
      {view.mode === 'list' ? <AVList onOpen={openDetail} /> : <AVDetail id={view.id} />}
    </div>
  )
}

function AVList({ onOpen }) {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const { warehouseMap } = useMaps()
  const list = useQuery({
    queryKey: ['av-list', keyword, status, pager.page, pager.size],
    queryFn: () => adjustmentsApi.list({ keyword, status, page: pager.page, size: pager.size }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data
  const columns = [
    { title: 'Mã phiếu', dataIndex: 'voucherNumber', render: (v, r) => <a onClick={() => onOpen(r.id)}>{v || r.id}</a> },
    { title: 'Trạng thái', dataIndex: 'status', width: 130, render: avTag },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v },
    { title: 'Phiên kiểm kê', dataIndex: 'sessionId', render: (v) => v || '—' },
    { title: 'Lý do', dataIndex: 'reason', render: (v) => v || '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 120 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <Input.Search allowClear placeholder="Tìm theo mã phiếu" style={{ width: 220 }} prefix={<SearchOutlined />}
          onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 160 }}
          options={AV_STATUS_OPTS} value={status}
          onChange={(v) => { setStatus(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
      </Space>
      <Table rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []} columns={columns}
        scroll={{ x: 'max-content' }}
        pagination={{
          current: (pageData?.page ?? 0) + 1, pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0, showSizeChanger: true, showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager({ page: p - 1, size: s }),
        }} />
    </>
  )
}

function AVDetail({ id }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const { warehouseMap, productMap } = useMaps()
  const { data: av, isLoading, isError, error } = useQuery({
    queryKey: ['av', id], queryFn: () => adjustmentsApi.get(id),
  })
  const approveMut = useMutation({
    mutationFn: () => adjustmentsApi.approve(id),
    onSuccess: (u) => { message.success('Đã duyệt phiếu điều chỉnh'); qc.setQueryData(['av', id], u); qc.invalidateQueries({ queryKey: ['av-list'] }) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy phiếu điều chỉnh')} /></Card>

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 120, render: (v) => v || '—' },
    { title: 'Lô', dataIndex: 'batchId', width: 120, render: (v) => v || '—' },
    { title: 'Tồn trước', dataIndex: 'beforeQuantity', width: 100, align: 'right' },
    { title: 'Thay đổi', dataIndex: 'quantityChange', width: 100, align: 'right',
      render: (v) => <Tag color={v === 0 ? 'default' : v > 0 ? 'green' : 'red'}>{v > 0 ? `+${v}` : v}</Tag> },
    { title: 'Tồn sau', dataIndex: 'afterQuantity', width: 100, align: 'right' },
  ]

  return (
    <Card
      title={<Space>Phiếu điều chỉnh <b>{av.voucherNumber || av.id}</b> {avTag(av.status)}</Space>}
      extra={av.status === 'DRAFT' && (
        <Can permission={P.ADJUSTMENT_APPROVE}>
          <Popconfirm title="Duyệt phiếu điều chỉnh này?" okText="Duyệt" cancelText="Huỷ"
            onConfirm={() => approveMut.mutate()}>
            <Button type="primary" icon={<CheckOutlined />} loading={approveMut.isPending}>Duyệt</Button>
          </Popconfirm>
        </Can>
      )}
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered
        items={[
          { key: 'wh', label: 'Kho', children: warehouseMap[av.warehouseId]?.name || av.warehouseId },
          { key: 'ss', label: 'Phiên kiểm kê', children: av.sessionId || '—' },
          { key: 're', label: 'Lý do', children: av.reason || '—' },
          { key: 'cb', label: 'Người tạo', children: av.createdBy || '—' },
          { key: 'ab', label: 'Người duyệt', children: av.approvedBy || '—' },
          { key: 'ca', label: 'Tạo lúc', children: av.createdAt ? dayjs(av.createdAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={av.details || []} columns={columns} scroll={{ x: 'max-content' }} />
      <Typography.Text type="secondary">
        * Duyệt phiếu vượt ngưỡng chênh lệch % có thể bị BE từ chối (403) nếu bạn không đủ thẩm quyền.
      </Typography.Text>
    </Card>
  )
}
