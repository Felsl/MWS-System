import ExportButton from '../../components/ExportButton'
import PageHeader from '../../components/PageHeader'
import { columnSortOrder } from '../../utils/sort'
import RowLink from '../../components/RowLink'
import FitTable from '../../components/FitTable'
import { useMemo } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useRecordView } from '../../hooks/useRecordView'
import { useListParams } from '../../hooks/useListParams'
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
import { warehousesApi } from '../../api/warehouses.api'
import { useBinLabels } from '../../hooks/useBinLabels'

const AV_STATUS = {
  DRAFT: { color: 'gold', label: 'Chờ duyệt' },
  APPROVED: { color: 'green', label: 'Đã duyệt' },
  REJECTED: { color: 'red', label: 'Bị từ chối' },
}
const avTag = (s) => <Tag color={AV_STATUS[s]?.color || 'default'}>{AV_STATUS[s]?.label || s}</Tag>
const AV_STATUS_OPTS = Object.entries(AV_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useMaps() {
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const { map: productMap } = useProducts()
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { warehouseMap, productMap }
}

export default function AdjustmentsPage() {
  // Chế độ xem nằm ở URL: /adjustment-vouchers | /adjustment-vouchers/new | /adjustment-vouchers/<id>
  const { mode, id, openList, openDetail } = useRecordView('/adjustment-vouchers')
  return (
    <div>
      {mode !== 'list' && (
        <PageHeader title="Phiếu điều chỉnh tồn"
          onBack={<Button icon={<ArrowLeftOutlined />} onClick={openList}>Danh sách</Button>} />
      )}
      {mode === 'list' ? <AVList onOpen={openDetail} /> : (id && <AVDetail id={id} />)}
    </div>
  )
}

function AVList({ onOpen }) {
  // Bộ lọc nằm trong query string (?q=&status=&page=&size=&sort=&dir=).
  // Thay cho useState + location.state: F5 không mất bộ lọc, gửi link được,
  // Back lùi đúng bộ lọc trước, và Dashboard chỉ cần trỏ tới ?status=... .
  const {
    keyword, status, page, size, sort, dir, sorter,
    setKeyword, setStatus, setPager, setSorter,
  } = useListParams()
  const { warehouseMap } = useMaps()
  const list = useQuery({
    queryKey: ['av-list', keyword, status, page, size, sort, dir],
    queryFn: () => adjustmentsApi.list({ keyword, status, page: page, size: size, sort, dir }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data
  const columns = [
    { title: 'Mã phiếu', dataIndex: 'voucherNumber', sorter: true, sortOrder: columnSortOrder(sorter, 'voucherNumber'), render: (v, r) => <RowLink onClick={() => onOpen(r.id)}>{v || r.id}</RowLink> },
    { title: 'Trạng thái', dataIndex: 'status', sorter: true, sortOrder: columnSortOrder(sorter, 'status'), width: 130, render: avTag },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v },
    { title: 'Phiên kiểm kê', dataIndex: 'sessionId', render: (v) => v || '—' },
    { title: 'Lý do', dataIndex: 'reason', render: (v) => v || '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 120 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', sorter: true, sortOrder: columnSortOrder(sorter, 'createdAt'), width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <PageHeader
        title="Phiếu điều chỉnh tồn"
        extra={<>
        <ExportButton filename="phieu-dieu-chinh.xlsx" fetchRows={() => adjustmentsApi.list({ keyword, status, sort, dir, size: 10000 }).then(r => r.content)} />
        <Input.Search allowClear key={`q-${keyword}`} defaultValue={keyword} placeholder="Tìm theo mã phiếu" style={{ width: 220 }} prefix={<SearchOutlined />}
          onSearch={(v) => setKeyword(v)} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 160 }}
          options={AV_STATUS_OPTS} value={status}
          onChange={(v) => setStatus(v)} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
        </>}
      />
      <FitTable rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []} columns={columns}
        scroll={{ x: 'max-content' }}
        onChange={(_p, _f, s, extra) => { if (extra.action === 'sort') setSorter(s) }}
        pagination={{
          current: (pageData?.page ?? 0) + 1, pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0, showSizeChanger: true, showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager(p - 1, s),
        }} />
    </>
  )
}

function AVDetail({ id }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const { warehouseMap, productMap } = useMaps()
  const { labelOf } = useBinLabels()
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
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130, render: (v) => labelOf(v) },
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
          <Popconfirm title="Duyệt phiếu điều chỉnh này?"
            description={<span>Duyệt <b>{av.voucherNumber || av.id}</b>. Tồn kho sẽ được ghi nhận thay đổi ngay và không hoàn tác được.</span>}
            okText="Duyệt" cancelText="Huỷ"
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
