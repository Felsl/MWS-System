import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Select, Table, Space, Typography, Tag, Descriptions, Empty,
  Modal, Form, InputNumber, Input, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, ReloadOutlined, ArrowLeftOutlined, EditOutlined,
  CheckOutlined, CheckCircleOutlined, FileDoneOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { stocktakesApi } from '../../api/stocktakes.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'

const ST_STATUS = {
  OPEN: { color: 'gold', label: 'Đang kiểm kê' },
  FREEZED: { color: 'blue', label: 'Đóng băng' },
  ADJUSTED: { color: 'green', label: 'Đã điều chỉnh' },
}
const stTag = (s) => <Tag color={ST_STATUS[s]?.color || 'default'}>{ST_STATUS[s]?.label || s}</Tag>
const ST_STATUS_OPTS = Object.entries(ST_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useWarehouseMap() {
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const map = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { warehouses, warehouseMap: map }
}
function useProductMap() {
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const map = useMemo(() => Object.fromEntries((products.data?.content || []).map(p => [p.id, p])), [products.data])
  return { map }
}

export default function StocktakesPage() {
  const [view, setView] = useState({ mode: 'list', id: null })
  const [startOpen, setStartOpen] = useState(false)
  const openDetail = (id) => setView({ mode: 'detail', id })
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {view.mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
          )}
          <Typography.Title level={4} style={{ margin: 0 }}>Kiểm kê kho (Stocktake)</Typography.Title>
        </Space>
        <Can permission={P.STOCKTAKE_MANAGE}>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setStartOpen(true)}>Bắt đầu kiểm kê</Button>
        </Can>
      </div>
      {view.mode === 'list' && <STList onOpen={openDetail} />}
      {view.mode === 'detail' && view.id && <STDetail id={view.id} />}
      <StartModal open={startOpen} onClose={() => setStartOpen(false)} onStarted={(r) => openDetail(r.session.id)} />
    </div>
  )
}

function STList({ onOpen }) {
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const { warehouseMap } = useWarehouseMap()
  const list = useQuery({
    queryKey: ['stk-list', status, pager.page, pager.size],
    queryFn: () => stocktakesApi.list({ status, page: pager.page, size: pager.size }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data
  const columns = [
    { title: 'Mã phiên', dataIndex: 'id', render: (v) => <a onClick={() => onOpen(v)}>{v}</a> },
    { title: 'Trạng thái', dataIndex: 'status', width: 150, render: stTag },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v },
    { title: 'Đóng băng từ', dataIndex: 'freezeStartedAt', width: 160, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 130 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={ST_STATUS_OPTS} value={status}
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

function StartModal({ open, onClose, onStarted }) {
  const { message } = AntdApp.useApp()
  const { warehouses } = useWarehouseMap()
  const [warehouseId, setWarehouseId] = useState()
  const mut = useMutation({
    mutationFn: () => stocktakesApi.start(warehouseId),
    onSuccess: (r) => { message.success('Đã bắt đầu phiên kiểm kê (kho bị đóng băng)'); onStarted(r); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Bắt đầu phiên kiểm kê" open={open} onCancel={onClose}
      onOk={() => warehouseId ? mut.mutate() : message.warning('Chọn kho')}
      confirmLoading={mut.isPending} destroyOnClose>
      <Typography.Paragraph type="secondary">
        Chọn kho để kiểm kê. Trong lúc kiểm kê, kho sẽ bị <b>đóng băng</b> (các thao tác nhập/xuất bị chặn — lỗi 409).
      </Typography.Paragraph>
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} placeholder="Chọn kho"
        loading={warehouses.isLoading} value={warehouseId} onChange={setWarehouseId}
        options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
    </Modal>
  )
}

function STDetail({ id }) {
  const { message } = AntdApp.useApp()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { warehouseMap } = useWarehouseMap()
  const { map: productMap } = useProductMap()
  const [countLine, setCountLine] = useState(null)

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['stk', id], queryFn: () => stocktakesApi.get(id),
  })
  const invalidate = () => qc.invalidateQueries({ queryKey: ['stk', id] })

  const approveLineMut = useMutation({
    mutationFn: (detailId) => stocktakesApi.approveLine(detailId),
    onSuccess: () => { message.success('Đã duyệt dòng'); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const completeMut = useMutation({
    mutationFn: () => stocktakesApi.complete(id),
    onSuccess: (r) => {
      message.success(`Đã hoàn tất${r?.voucher?.voucherNumber ? ' · phiếu điều chỉnh ' + r.voucher.voucherNumber : ''}`)
      qc.setQueryData(['stk', id], { session: r.session, details: r.details })
      qc.invalidateQueries({ queryKey: ['stk-list'] })
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy phiên kiểm kê')} /></Card>

  const session = data.session || {}
  const details = data.details || []
  const editable = session.status === 'OPEN' || session.status === 'FREEZED'

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 120 },
    { title: 'Lô', dataIndex: 'batchId', width: 120, render: (v) => v || '—' },
    { title: 'Tồn hệ thống', dataIndex: 'systemQuantity', width: 110, align: 'right' },
    { title: 'Đếm thực', dataIndex: 'countedQuantity', width: 100, align: 'right', render: (v) => v ?? '—' },
    {
      title: 'Chênh lệch', dataIndex: 'difference', width: 100, align: 'right',
      render: (v) => v == null ? '—' : <Tag color={v === 0 ? 'default' : v > 0 ? 'green' : 'red'}>{v > 0 ? `+${v}` : v}</Tag>,
    },
    {
      title: 'Trạng thái', key: '_s', width: 120,
      render: (_, d) => d.approvedAt ? <Tag color="green">Đã duyệt</Tag>
        : d.countedAt ? <Tag color="gold">Đã đếm</Tag> : <Tag>Chưa đếm</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 190, fixed: 'right',
      render: (_, d) => (
        <Space>
          {editable && !d.approvedAt && (
            <Can permission={P.STOCKTAKE_MANAGE}>
              <Button size="small" icon={<EditOutlined />} onClick={() => setCountLine(d)}>Đếm</Button>
            </Can>
          )}
          {editable && d.countedAt && !d.approvedAt && (
            <Can permission={P.STOCKTAKE_APPROVE}>
              <Button size="small" type="primary" icon={<CheckOutlined />}
                loading={approveLineMut.isPending}
                onClick={() => approveLineMut.mutate(d.id)}>Duyệt</Button>
            </Can>
          )}
        </Space>
      ),
    },
  ]

  const allApproved = details.length > 0 && details.every(d => d.approvedAt)

  return (
    <Card
      title={<Space>Phiên kiểm kê <b>{session.id}</b> {stTag(session.status)}</Space>}
      extra={
        <Space wrap>
          {session.status === 'ADJUSTED' && (
            <Button icon={<FileDoneOutlined />} onClick={() => navigate('/adjustment-vouchers')}>Xem phiếu điều chỉnh</Button>
          )}
          {editable && (
            <Can permission={P.STOCKTAKE_MANAGE}>
              <Button type="primary" icon={<CheckCircleOutlined />} loading={completeMut.isPending}
                disabled={!allApproved}
                onClick={() => completeMut.mutate()}>Hoàn tất & sinh điều chỉnh</Button>
            </Can>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered
        items={[
          { key: 'wh', label: 'Kho', children: warehouseMap[session.warehouseId]?.name || session.warehouseId },
          { key: 'cb', label: 'Người tạo', children: session.createdBy || '—' },
          { key: 'fs', label: 'Đóng băng từ', children: session.freezeStartedAt ? dayjs(session.freezeStartedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'fe', label: 'Mở băng lúc', children: session.freezeEndedAt ? dayjs(session.freezeEndedAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={details} columns={columns} scroll={{ x: 'max-content' }} />
      {editable && !allApproved && (
        <Typography.Text type="secondary">* Đếm và duyệt tất cả dòng rồi mới Hoàn tất được.</Typography.Text>
      )}

      <CountModal line={countLine} onClose={() => setCountLine(null)} onDone={invalidate} />
    </Card>
  )
}

function CountModal({ line, onClose, onDone }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const mut = useMutation({
    mutationFn: (v) => stocktakesApi.count(line.id, v.countedQuantity),
    onSuccess: () => { message.success('Đã ghi số đếm'); onDone(); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Nhập số đếm thực tế" open={!!line} onCancel={onClose}
      onOk={async () => mut.mutate(await form.validateFields())} confirmLoading={mut.isPending}
      afterOpenChange={(o) => { if (o) form.setFieldsValue({ countedQuantity: line?.countedQuantity ?? line?.systemQuantity }) }}
      destroyOnClose>
      <Typography.Paragraph type="secondary">Tồn hệ thống: <b>{line?.systemQuantity}</b></Typography.Paragraph>
      <Form form={form} layout="vertical">
        <Form.Item name="countedQuantity" label="Số lượng đếm được"
          rules={[{ required: true, message: 'Nhập số đếm' }]}>
          <InputNumber min={0} style={{ width: '100%' }} autoFocus />
        </Form.Item>
      </Form>
    </Modal>
  )
}
