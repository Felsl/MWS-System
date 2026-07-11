import ExportButton from '../../components/ExportButton'
import { sorterToParams, columnSortOrder } from '../../utils/sort'
import FitTable from '../../components/FitTable'
import { useEffect, useMemo, useState } from 'react'
import { keepPreviousData, useQuery, useQueries, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, InputNumber, Row, Col, Table, Space,
  Typography, Tag, Descriptions, Empty, Popconfirm, Divider, Modal, Progress, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, ReloadOutlined, ArrowLeftOutlined,
  SendOutlined, CheckOutlined, CloseOutlined, CarOutlined, InboxOutlined,
  ProfileOutlined, ScanOutlined, LockOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import BarcodeScanner from '../../components/BarcodeScanner'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { transferOrdersApi } from '../../api/transferOrders.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'
import { carriersApi } from '../../api/partners.api'
import { inventoryApi } from '../../api/inventory.api'

const TO_STATUS = {
  DRAFT: { color: 'default', label: 'Nháp' },
  PENDING_APPROVAL: { color: 'gold', label: 'Chờ phê duyệt' },
  APPROVED: { color: 'blue', label: 'Đã duyệt' },
  PICKING: { color: 'orange', label: 'Đang gom hàng' },
  IN_TRANSIT: { color: 'geekblue', label: 'Đang chuyển' },
  COMPLETED: { color: 'green', label: 'Hoàn thành' },
  REJECTED: { color: 'red', label: 'Bị từ chối' },
  CANCELLED: { color: 'red', label: 'Đã huỷ' },
}
const toTag = (s) => <Tag color={TO_STATUS[s]?.color || 'default'}>{TO_STATUS[s]?.label || s}</Tag>
const TO_STATUS_OPTS = Object.entries(TO_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useNameMaps() {
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { warehouses, warehouseMap }
}
function useProductMap() {
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const list = products.data?.content || []
  const map = useMemo(() => Object.fromEntries(list.map(p => [p.id, p])), [list])
  return { products, list, map }
}
const productOptions = (list) => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))

// Map binLocationId -> nhãn toạ độ ô kệ dạng zone-aisle-rack-bin (VD: A-01-R1-B3) của 1 kho.
function useBinLabelMap(warehouseId) {
  const bins = useQuery({
    queryKey: ['bins', warehouseId],
    queryFn: () => warehousesApi.listBins(warehouseId),
    enabled: !!warehouseId,
  })
  return useMemo(
    () => Object.fromEntries((bins.data || []).map(b => [b.id, b.coordinateLabel || b.id])),
    [bins.data],
  )
}

// Map batchId -> batchNumber cho danh sách sản phẩm tại 1 kho (để hiển thị mã lô thay vì ID).
function useBatchNumberMap(warehouseId, productIds) {
  const ids = useMemo(() => [...new Set((productIds || []).filter(Boolean))], [productIds])
  const results = useQueries({
    queries: ids.map(pid => ({
      queryKey: ['batches', pid, warehouseId],
      queryFn: () => inventoryApi.getBatches(pid, warehouseId),
      enabled: !!warehouseId && !!pid,
    })),
  })
  return useMemo(() => {
    const m = {}
    results.forEach(r => (r.data || []).forEach(b => { m[b.id] = b.batchNumber }))
    return m
  }, [results])
}

export default function TransferOrdersPage() {
  const [view, setView] = useState({ mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })
  return (
    <div>
      {view.mode !== 'list' && (
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
            <Typography.Title level={4} style={{ margin: 0 }}>Điều chuyển nội bộ (Transfer)</Typography.Title>
          </Space>
        </div>
      )}
      {view.mode === 'list' && <TOList onOpen={openDetail} onCreate={() => setView({ mode: 'create', id: null })} />}
      {view.mode === 'create' && <CreateTO onCreated={(to) => openDetail(to.id)} />}
      {view.mode === 'detail' && view.id && <TODetail id={view.id} />}
    </div>
  )
}

function TOList({ onOpen, onCreate }) {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const [sorter, setSorter] = useState(null)
  const { sort, dir } = sorterToParams(sorter)
  const { warehouseMap } = useNameMaps()
  const list = useQuery({
    queryKey: ['to-list', keyword, status, pager.page, pager.size, sort, dir],
    queryFn: () => transferOrdersApi.list({ keyword, status, page: pager.page, size: pager.size, sort, dir }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data
  const columns = [
    { title: 'Mã phiếu', dataIndex: 'transferNumber', sorter: true, sortOrder: columnSortOrder(sorter, 'transferNumber'), render: (v, r) => <a onClick={() => onOpen(r.id)}>{v || r.id}</a> },
    { title: 'Trạng thái', dataIndex: 'status', sorter: true, sortOrder: columnSortOrder(sorter, 'status'), width: 140, render: toTag },
    { title: 'Kho nguồn', dataIndex: 'fromWarehouseId', render: (v) => warehouseMap[v]?.name || v },
    { title: 'Kho đích', dataIndex: 'toWarehouseId', render: (v) => warehouseMap[v]?.name || v },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 130 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', sorter: true, sortOrder: columnSortOrder(sorter, 'createdAt'), width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={4} style={{ marginLeft: 20, marginTop: 0 }}>Điều chuyển nội bộ (Transfer)</Typography.Title>
        <Space wrap>
        <ExportButton filename="phieu-dieu-chuyen.xlsx" fetchRows={() => transferOrdersApi.list({ keyword, status, sort, dir, size: 10000 }).then(r => r.content)} />
        <Input.Search allowClear placeholder="Tìm theo mã phiếu" style={{ width: 220 }} prefix={<SearchOutlined />}
          onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={TO_STATUS_OPTS} value={status}
          onChange={(v) => { setStatus(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.TRANSFER_CREATE}><Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo phiếu chuyển</Button></Can>
        </Space>
      </Space>
      <FitTable rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []} columns={columns}
        scroll={{ x: 'max-content' }}
        onChange={(_p, _f, s, extra) => { if (extra.action === 'sort') { setSorter(s); setPager(p => ({ ...p, page: 0 })) } }}
        pagination={{
          current: (pageData?.page ?? 0) + 1, pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0, showSizeChanger: true, showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager({ page: p - 1, size: s }),
        }} />
    </>
  )
}

// Ô chọn lô cho 1 dòng: mặc định "Tự động (FEFO)", có thể chỉ định lô cụ thể (gợi ý theo FEFO).
const AUTO = '__auto__'
function BatchLineSelect({ fromWarehouseId, productId, quantity, value, onChange, binMap = {} }) {
  const enabled = !!fromWarehouseId && !!productId && quantity > 0
  const sug = useQuery({
    queryKey: ['fefo', fromWarehouseId, productId, quantity],
    queryFn: () => inventoryApi.allocateBatches(productId, fromWarehouseId, quantity),
    enabled,
  })
  const options = [
    { value: AUTO, label: 'Tự động (FEFO) — picker quét lô bất kỳ' },
    ...(sug.data || []).map(s => ({
      value: s.batchId,
      label: `Chỉ định: ${s.batchNumber} · ${binMap[s.binLocationId] || s.binLocationId} · gợi ý ${s.suggestedQuantity}`,
    })),
  ]
  return (
    <Select value={value || AUTO} onChange={onChange} options={options}
      loading={sug.isFetching} style={{ width: '100%' }}
      disabled={!enabled} placeholder={enabled ? undefined : 'Chọn kho nguồn + SP + SL trước'} />
  )
}

function CreateTO({ onCreated }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const [form] = Form.useForm()
  const { warehouses } = useNameMaps()
  const { list: productList } = useProductMap()
  const fromId = Form.useWatch('fromWarehouseId', form)

  // Chỉ cho chọn sản phẩm ĐANG CÓ TỒN ở kho nguồn.
  const invInWh = useQuery({
    queryKey: ['inv-by-wh', fromId],
    queryFn: () => inventoryApi.getByWarehouse(fromId),
    enabled: !!fromId,
  })
  const allowedProductIds = useMemo(
    () => new Set((invInWh.data || []).filter(r => (r.quantity ?? 0) > 0).map(r => r.productId)),
    [invInWh.data],
  )
  const availableProducts = useMemo(
    () => productList.filter(p => allowedProductIds.has(p.id)),
    [productList, allowedProductIds],
  )
  const binMap = useBinLabelMap(fromId)

  // Đổi kho nguồn -> reset dòng hàng (sản phẩm cũ có thể không còn tồn ở kho mới).
  useEffect(() => {
    form.setFieldsValue({ lines: [{ designatedBatchId: AUTO }] })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fromId])

  const createMut = useMutation({
    mutationFn: transferOrdersApi.create,
    onSuccess: (to) => { message.success(`Đã tạo phiếu ${to.transferNumber || ''}`.trim()); onCreated(to) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const submit = async () => {
    const v = await form.validateFields()
    if (v.fromWarehouseId === v.toWarehouseId) { message.error('Kho nguồn và kho đích phải khác nhau'); return }
    createMut.mutate({
      fromWarehouseId: v.fromWarehouseId,
      toWarehouseId: v.toWarehouseId,
      createdBy: user?.userId,
      lines: v.lines.map(l => ({
        productId: l.productId,
        quantity: l.quantity,
        designatedBatchId: l.designatedBatchId && l.designatedBatchId !== AUTO ? l.designatedBatchId : null,
      })),
    })
  }

  const whOptions = (warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))

  return (
    <Card title="Tạo phiếu điều chuyển">
      <Form form={form} layout="vertical" initialValues={{ lines: [{ designatedBatchId: AUTO }] }}>
        <Row gutter={16}>
          <Col xs={24} md={10}>
            <Form.Item name="fromWarehouseId" label="Kho nguồn" rules={[{ required: true, message: 'Chọn kho nguồn' }]}>
              <Select showSearch optionFilterProp="label" loading={warehouses.isLoading} placeholder="Chọn kho" options={whOptions} />
            </Form.Item>
          </Col>
          <Col xs={24} md={10}>
            <Form.Item name="toWarehouseId" label="Kho đích" rules={[{ required: true, message: 'Chọn kho đích' }]}>
              <Select showSearch optionFilterProp="label" loading={warehouses.isLoading} placeholder="Chọn kho"
                options={whOptions.filter(o => o.value !== fromId)} />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left" style={{ margin: '4px 0 12px' }}>Dòng hàng</Divider>
        <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
          Mặc định để "Tự động (FEFO)" — picker quét lô ACTIVE bất kỳ. Nếu chọn "Chỉ định", picker buộc phải quét đúng lô đó.
        </Typography.Paragraph>
        <Form.List name="lines">
          {(fields, { add, remove }) => (
            <>
              {fields.map(({ key, name, ...rest }) => (
                <Row gutter={8} key={key} align="top" style={{ marginBottom: 4 }}>
                  <Col flex="260px">
                    <Form.Item {...rest} name={[name, 'productId']} rules={[{ required: true, message: 'Chọn SP' }]}>
                      <Select showSearch optionFilterProp="label"
                        disabled={!fromId}
                        loading={!!fromId && invInWh.isFetching}
                        placeholder={fromId ? (invInWh.isFetching ? 'Đang tải sản phẩm…' : 'Sản phẩm') : 'Chọn kho nguồn trước'}
                        notFoundContent={fromId && !invInWh.isFetching ? 'Kho nguồn không có sản phẩm tồn' : null}
                        options={productOptions(availableProducts)} />
                    </Form.Item>
                  </Col>
                  <Col flex="110px">
                    <Form.Item {...rest} name={[name, 'quantity']} rules={[{ required: true, message: 'SL' }]}>
                      <InputNumber min={1} placeholder="Số lượng" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="auto">
                    <Form.Item noStyle shouldUpdate>
                      {() => (
                        <Form.Item {...rest} name={[name, 'designatedBatchId']}>
                          <BatchLineSelect
                            fromWarehouseId={fromId}
                            productId={form.getFieldValue(['lines', name, 'productId'])}
                            quantity={form.getFieldValue(['lines', name, 'quantity'])}
                            binMap={binMap} />
                        </Form.Item>
                      )}
                    </Form.Item>
                  </Col>
                  <Col flex="40px">
                    <Button danger type="text" icon={<DeleteOutlined />} disabled={fields.length === 1} onClick={() => remove(name)} />
                  </Col>
                </Row>
              ))}
              <Button type="dashed" block icon={<PlusOutlined />} onClick={() => add({ designatedBatchId: AUTO })}>Thêm dòng</Button>
            </>
          )}
        </Form.List>

        <div style={{ marginTop: 16, textAlign: 'right' }}>
          <Button type="primary" onClick={submit} loading={createMut.isPending}>Tạo phiếu</Button>
        </div>
      </Form>
    </Card>
  )
}

function TODetail({ id }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const qc = useQueryClient()
  const { warehouseMap } = useNameMaps()
  const { map: productMap } = useProductMap()
  const [dispatchOpen, setDispatchOpen] = useState(false)
  const [receiveOpen, setReceiveOpen] = useState(false)

  const { data: to, isLoading, isError, error } = useQuery({
    queryKey: ['to', id], queryFn: () => transferOrdersApi.get(id),
  })
  // Nhãn ô kệ kho đích (binLocationId của dòng là ô kệ cất tại kho nhận).
  const binMapDest = useBinLabelMap(to?.toWarehouseId)
  const refresh = (u) => { if (u?.id) qc.setQueryData(['to', id], u); else qc.invalidateQueries({ queryKey: ['to', id] }); qc.invalidateQueries({ queryKey: ['to-list'] }) }

  const reqMut = useMutation({ mutationFn: () => transferOrdersApi.requestApproval(id),
    onSuccess: (u) => { message.success('Đã trình duyệt'); refresh(u) }, onError: (e) => message.error(getErrorMessage(e)) })
  const approveMut = useMutation({ mutationFn: () => transferOrdersApi.approve(id, user?.userId),
    onSuccess: (u) => { message.success('Đã phê duyệt'); refresh(u) }, onError: (e) => message.error(getErrorMessage(e)) })
  const rejectMut = useMutation({ mutationFn: () => transferOrdersApi.reject(id, user?.userId),
    onSuccess: (u) => { message.success('Đã từ chối'); refresh(u) }, onError: (e) => message.error(getErrorMessage(e)) })
  const cancelMut = useMutation({ mutationFn: () => transferOrdersApi.cancel(id),
    onSuccess: (u) => { message.success('Đã huỷ phiếu'); refresh(u) }, onError: (e) => message.error(getErrorMessage(e)) })
  const genPickMut = useMutation({ mutationFn: () => transferOrdersApi.generatePicking(id),
    onSuccess: () => { message.success('Đã tạo lệnh gom hàng'); refresh(); qc.invalidateQueries({ queryKey: ['to-picking', id] }) },
    onError: (e) => message.error(getErrorMessage(e)) })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy phiếu điều chuyển')} /></Card>

  const s = to.status
  const busy = reqMut.isPending || approveMut.isPending || rejectMut.isPending || cancelMut.isPending
  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SL chuyển', dataIndex: 'quantity', width: 90, align: 'right' },
    {
      title: 'Lô', key: 'batch', width: 150,
      render: (_, r) => r.designatedBatchId
        ? <Tag color="purple" icon={<LockOutlined />}>Chỉ định</Tag>
        : <Tag>FEFO tự động</Tag>,
    },
    { title: 'SL nhận', dataIndex: 'quantityReceived', width: 90, align: 'right' },
    { title: 'Hao hụt', dataIndex: 'lostQuantity', width: 90, align: 'right', render: (v) => v > 0 ? <Tag color="red">{v}</Tag> : v },
    { title: 'Ô kệ đích', dataIndex: 'binLocationId', width: 140, render: (v) => (v ? (binMapDest[v] || v) : '—') },
  ]

  return (
    <Card
      title={<Space>Phiếu chuyển <b>{to.transferNumber || to.id}</b> {toTag(s)}</Space>}
      extra={
        <Space wrap>
          {s === 'DRAFT' && (
            <Can permission={P.TRANSFER_CREATE}>
              <Button type="primary" icon={<SendOutlined />} loading={busy} onClick={() => reqMut.mutate()}>Trình duyệt</Button>
            </Can>
          )}
          {s === 'PENDING_APPROVAL' && (
            <Can permission={P.TRANSFER_APPROVE}>
              <Button type="primary" icon={<CheckOutlined />} loading={busy} onClick={() => approveMut.mutate()}>Phê duyệt</Button>
            </Can>
          )}
          {s === 'PENDING_APPROVAL' && (
            <Can permission={P.TRANSFER_APPROVE}>
              <Popconfirm title="Từ chối phiếu này?" okText="Từ chối" cancelText="Huỷ" onConfirm={() => rejectMut.mutate()}>
                <Button danger icon={<CloseOutlined />} loading={busy}>Từ chối</Button>
              </Popconfirm>
            </Can>
          )}
          {s === 'APPROVED' && (
            <Can permission={P.TRANSFER_DISPATCH}>
              <Button type="primary" icon={<ProfileOutlined />} loading={genPickMut.isPending}
                onClick={() => genPickMut.mutate()}>Tạo lệnh gom hàng</Button>
            </Can>
          )}
          {s === 'IN_TRANSIT' && (
            <Can permission={P.TRANSFER_RECEIVE}>
              <Button type="primary" icon={<InboxOutlined />} onClick={() => setReceiveOpen(true)}>Nhận hàng</Button>
            </Can>
          )}
          {(s === 'DRAFT' || s === 'PENDING_APPROVAL' || s === 'APPROVED') && (
            <Can permission={P.TRANSFER_CREATE}>
              <Popconfirm title="Huỷ phiếu này?" okText="Huỷ phiếu" cancelText="Không" onConfirm={() => cancelMut.mutate()}>
                <Button danger loading={busy}>Huỷ</Button>
              </Popconfirm>
            </Can>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered
        items={[
          { key: 'from', label: 'Kho nguồn', children: warehouseMap[to.fromWarehouseId]?.name || to.fromWarehouseId },
          { key: 'to', label: 'Kho đích', children: warehouseMap[to.toWarehouseId]?.name || to.toWarehouseId },
          { key: 'cb', label: 'Người tạo', children: to.createdBy || '—' },
          { key: 'ab', label: 'Người duyệt', children: to.approvedBy || '—' },
          { key: 'aa', label: 'Duyệt lúc', children: to.approvedAt ? dayjs(to.approvedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'ca', label: 'Tạo lúc', children: to.createdAt ? dayjs(to.createdAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={to.details || []} columns={columns} scroll={{ x: 'max-content' }} />

      {s === 'PICKING' && (
        <TransferPickingPanel transferId={id} fromWarehouseId={to.fromWarehouseId}
          productMap={productMap} onDispatch={() => setDispatchOpen(true)} />
      )}

      <DispatchModal open={dispatchOpen} onClose={() => setDispatchOpen(false)} toId={id} onDone={refresh} />
      <ReceiveModal open={receiveOpen} onClose={() => setReceiveOpen(false)} to={to} productMap={productMap} onDone={refresh} />
    </Card>
  )
}

function TransferPickingPanel({ transferId, fromWarehouseId, productMap, onDispatch }) {
  const [scanOpen, setScanOpen] = useState(false)
  const picking = useQuery({ queryKey: ['to-picking', transferId], queryFn: () => transferOrdersApi.getPicking(transferId) })
  const details = picking.data?.details || []
  const done = details.filter(d => d.confirmed).length
  const allDone = details.length > 0 && done === details.length
  const binMap = useBinLabelMap(fromWarehouseId)
  const batchMap = useBatchNumberMap(fromWarehouseId, details.map(d => d.productId))

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130, render: (v) => (v ? (binMap[v] || v) : '—') },
    {
      title: 'Yêu cầu lô', dataIndex: 'requiredBatchId', width: 160,
      render: (v) => v ? <Tag color="purple" icon={<LockOutlined />}>{batchMap[v] || v}</Tag> : <Tag>Lô ACTIVE bất kỳ</Tag>,
    },
    { title: 'Lô đã quét', dataIndex: 'actualBatchId', width: 140, render: (v) => (v ? (batchMap[v] || v) : '—') },
    { title: 'SL cần', dataIndex: 'quantityToPick', width: 80, align: 'right' },
    { title: 'Xác nhận', dataIndex: 'confirmed', width: 100, render: (c) => c ? <Tag color="green">Đã lấy</Tag> : <Tag>Chưa</Tag> },
  ]

  return (
    <Card size="small" style={{ marginTop: 16 }} title={<Space><ProfileOutlined /> Gom hàng</Space>}
      extra={
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => picking.refetch()} loading={picking.isFetching} />
          <Can permission={P.TRANSFER_DISPATCH}>
            <Button type="primary" icon={<ScanOutlined />} disabled={allDone} onClick={() => setScanOpen(true)}>Quét lấy hàng</Button>
          </Can>
          <Can permission={P.TRANSFER_DISPATCH}>
            <Button type="primary" icon={<CarOutlined />} disabled={!allDone} onClick={onDispatch}>Xuất chuyển</Button>
          </Can>
        </Space>
      }>
      <Progress percent={details.length ? Math.round((done / details.length) * 100) : 0} format={() => `${done}/${details.length}`} />
      <Table style={{ marginTop: 8 }} rowKey="id" size="small" pagination={false}
        loading={picking.isLoading} dataSource={details} columns={columns} scroll={{ x: 'max-content' }} />
      {!allDone && <Typography.Text type="secondary">* Quét đủ tất cả dòng rồi mới "Xuất chuyển" được.</Typography.Text>}

      <TransferScanModal open={scanOpen} onClose={() => setScanOpen(false)} transferId={transferId}
        productMap={productMap} binMap={binMap} batchMap={batchMap} />
    </Card>
  )
}

function TransferScanModal({ open, onClose, transferId, productMap, binMap = {}, batchMap = {} }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const qc = useQueryClient()
  const [busy, setBusy] = useState(false)

  const picking = useQuery({ queryKey: ['to-picking', transferId], queryFn: () => transferOrdersApi.getPicking(transferId), enabled: open })
  const details = picking.data?.details || []
  const current = details.find(d => !d.confirmed)

  const scanMut = useMutation({
    mutationFn: (code) => transferOrdersApi.scanPicking(current.id, code, user?.userId),
    onSuccess: (u) => { message.success('Đã xác nhận'); qc.setQueryData(['to-picking', transferId], u); setBusy(false) },
    onError: (e) => { message.error(getErrorMessage(e)); setBusy(false) },
  })

  const handleScan = (code) => { if (busy || !current) return; setBusy(true); scanMut.mutate(code) }
  const [manual, setManual] = useState('')

  return (
    <Modal title="Quét lấy hàng điều chuyển" open={open} onCancel={onClose} footer={null} destroyOnClose width={480}>
      {!current ? (
        <Empty description="Đã quét xong tất cả dòng." />
      ) : (
        <>
          <div style={{ marginBottom: 8 }}>
            <div style={{ fontWeight: 600 }}>{productMap[current.productId]?.name || current.productId}</div>
            <Space size="large" style={{ fontSize: 13, color: 'rgba(0,0,0,.65)' }}>
              <span>Ô kệ: <b>{binMap[current.binLocationId] || current.binLocationId}</b></span>
              <span>SL: <b>{current.quantityToPick}</b></span>
            </Space>
            <div style={{ marginTop: 4 }}>
              {current.requiredBatchId
                ? <Tag color="purple" icon={<LockOutlined />}>Phải quét đúng lô: {batchMap[current.requiredBatchId] || current.requiredBatchId}</Tag>
                : <Tag color="blue">Quét lô ACTIVE bất kỳ còn tồn</Tag>}
            </div>
          </div>
          <BarcodeScanner onScan={handleScan} paused={busy} />
          <Space.Compact style={{ width: '100%', marginTop: 10 }}>
            <Input placeholder="Hoặc nhập mã lô tay" value={manual} disabled={busy}
              onChange={(e) => setManual(e.target.value)}
              onPressEnter={() => { if (manual.trim()) { handleScan(manual.trim()); setManual('') } }} />
            <Button disabled={busy || !manual.trim()} onClick={() => { handleScan(manual.trim()); setManual('') }}>Xác nhận</Button>
          </Space.Compact>
          <div style={{ marginTop: 8, textAlign: 'right' }}><Typography.Text type="secondary">Còn {details.filter(d => !d.confirmed).length} dòng</Typography.Text></div>
        </>
      )}
    </Modal>
  )
}

function DispatchModal({ open, onClose, toId, onDone }) {
  const { message } = AntdApp.useApp()
  const carriers = useQuery({ queryKey: ['carriers'], queryFn: carriersApi.list, enabled: open })
  const [carrierId, setCarrierId] = useState()
  const mut = useMutation({
    mutationFn: () => transferOrdersApi.dispatch(toId, carrierId),
    onSuccess: (shipment) => {
      message.success(`Đã xuất chuyển${shipment?.shipmentNumber ? ' · vận đơn ' + shipment.shipmentNumber : ''}`)
      onDone(); onClose()
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Xuất chuyển (dispatch)" open={open} onCancel={onClose}
      onOk={() => carrierId ? mut.mutate() : message.warning('Chọn đơn vị vận chuyển')}
      confirmLoading={mut.isPending} destroyOnClose>
      <Typography.Paragraph type="secondary">Trừ tồn theo lô thực nhặt và chuyển phiếu sang "Đang chuyển".</Typography.Paragraph>
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} placeholder="Chọn ĐVVC"
        loading={carriers.isLoading} value={carrierId} onChange={setCarrierId}
        options={(carriers.data || []).map(c => ({ value: c.id, label: `${c.name} (${c.code})` }))} />
    </Modal>
  )
}

function ReceiveModal({ open, onClose, to, productMap, onDone }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const bins = useQuery({
    queryKey: ['bins', to?.toWarehouseId],
    queryFn: () => warehousesApi.listBins(to.toWarehouseId),
    enabled: open && !!to?.toWarehouseId,
  })
  const binOptions = (bins.data || []).map(b => ({ value: b.id, label: b.coordinateLabel || b.id }))
  const details = to?.details || []

  const mut = useMutation({
    mutationFn: (lines) => transferOrdersApi.complete(to.id, lines),
    onSuccess: (u) => { message.success('Đã nhận hàng & hoàn thành'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const submit = async () => {
    const v = await form.validateFields()
    const lines = details.map((d, i) => ({
      detailId: d.id,
      quantityReceived: v.rows[i].quantityReceived,
      binLocationId: v.rows[i].binLocationId,
    }))
    mut.mutate(lines)
  }

  return (
    <Modal title="Nhận hàng điều chuyển" open={open} onCancel={onClose} onOk={submit}
      width={720} confirmLoading={mut.isPending}
      afterOpenChange={(o) => { if (o) form.setFieldsValue({ rows: details.map(d => ({ quantityReceived: d.quantity, binLocationId: undefined })) }) }}
      destroyOnClose>
      <Typography.Paragraph type="secondary">
        Nhập số thực nhận + ô kệ đích cho từng dòng. Chênh lệch so với SL chuyển sẽ được ghi nhận là hao hụt.
      </Typography.Paragraph>
      <Form form={form} layout="vertical">
        {details.map((d, i) => (
          <Row gutter={8} key={d.id} align="middle" style={{ marginBottom: 4 }}>
            <Col flex="auto">
              <div style={{ paddingBottom: 6 }}><b>{productMap[d.productId]?.name || d.productId}</b> · chuyển {d.quantity}</div>
            </Col>
            <Col flex="140px">
              <Form.Item name={['rows', i, 'quantityReceived']} label="SL nhận" rules={[{ required: true, message: 'SL' }]}>
                <InputNumber min={0} max={d.quantity} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col flex="200px">
              <Form.Item name={['rows', i, 'binLocationId']} label="Ô kệ đích" rules={[{ required: true, message: 'Chọn ô kệ' }]}>
                <Select showSearch optionFilterProp="label" placeholder="Ô kệ" options={binOptions} loading={bins.isFetching} />
              </Form.Item>
            </Col>
          </Row>
        ))}
      </Form>
    </Modal>
  )
}
