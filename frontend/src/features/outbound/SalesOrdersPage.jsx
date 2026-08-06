import ExportButton from '../../components/ExportButton'
import PageHeader from '../../components/PageHeader'
import FitTable from '../../components/FitTable'
import { useMemo } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useRecordView } from '../../hooks/useRecordView'
import { useListParams } from '../../hooks/useListParams'
import { useNavigate } from 'react-router-dom'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { columnSortOrder } from '../../utils/sort'
import RowLink from '../../components/RowLink'
import {
  Card, Button, Input, Form, Select, DatePicker, InputNumber, Row, Col,
  Table, Space, Tag, Descriptions, Empty, Popconfirm, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, ReloadOutlined, ArrowLeftOutlined,
  ThunderboltOutlined, ProfileOutlined, CarOutlined, CloseOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { handleFormError } from '../../utils/formErrors'
import { P } from '../../constants/permissions'
import { salesOrdersApi } from '../../api/salesOrders.api'
import { usePickingStatusBySo } from '../../hooks/useSalesOrderLookup'
import { SO_STATUS_OPTS, soStatusMeta } from './soStatus'
import { pickingListsApi } from '../../api/pickingLists.api'
import { customersApi } from '../../api/partners.api'
import { warehousesApi } from '../../api/warehouses.api'


function useNameMaps() {
  const customers = useQuery({ queryKey: ['customers'], queryFn: customersApi.list })
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const customerMap = useMemo(() => Object.fromEntries((customers.data || []).map(c => [c.id, c])), [customers.data])
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { customers, warehouses, customerMap, warehouseMap }
}

// `picked` = lệnh lấy của đơn đã COMPLETED -> đổi nhãn (xem soStatus.js).
const soTag = (s, picked = false) => {
  const m = soStatusMeta(s, picked)
  return <Tag color={m.color}>{m.label}</Tag>
}

export default function SalesOrdersPage() {
  // Chế độ xem nằm ở URL: /sales-orders | /sales-orders/new | /sales-orders/<id>
  const { mode, id, openList, openCreate, openDetail } = useRecordView('/sales-orders')
  return (
    <div>
      {mode !== 'list' && (
        <PageHeader title="Đơn bán hàng (SO)"
          onBack={<Button icon={<ArrowLeftOutlined />} onClick={openList}>Danh sách</Button>} />
      )}
      {mode === 'list' && <SOList onOpen={openDetail} onCreate={openCreate} />}
      {mode === 'create' && <CreateSO onCreated={(r) => openDetail(r.id, { replace: true })} />}
      {mode === 'detail' && id && <SODetail id={id} />}
    </div>
  )
}

function SOList({ onOpen, onCreate }) {
  // Bộ lọc nằm trong query string (?q=&status=&page=&size=&sort=&dir=).
  // Thay cho useState + location.state: F5 không mất bộ lọc, gửi link được,
  // Back lùi đúng bộ lọc trước, và Dashboard chỉ cần trỏ tới ?status=... .
  const {
    keyword, status, page, size, sort, dir, sorter,
    setKeyword, setStatus, setPager, setSorter,
  } = useListParams()
  const { customerMap, warehouseMap } = useNameMaps()
  // Đơn ở PICKING nhưng lệnh lấy đã xong -> đổi nhãn (backend không đổi status).
  const { isPicked } = usePickingStatusBySo()

  const list = useQuery({
    queryKey: ['so-list', keyword, status, page, size, sort, dir],
    queryFn: () => salesOrdersApi.list({ keyword, status, page: page, size: size, sort, dir }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data

  const columns = [
    { title: 'Mã đơn', dataIndex: 'soNumber', sorter: true, sortOrder: columnSortOrder(sorter, 'soNumber'), render: (v, r) => <RowLink onClick={() => onOpen(r.id)}>{v || r.id}</RowLink> },
    { title: 'Trạng thái', dataIndex: 'status', width: 140, sorter: true, sortOrder: columnSortOrder(sorter, 'status'), render: (v, r) => soTag(v, isPicked(r.id)) },
    { title: 'Khách hàng', dataIndex: 'customerId', render: (v) => customerMap[v]?.name || v },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v, width: 150 },
    { title: 'Cần giao', dataIndex: 'requiredDate', width: 120, sorter: true, sortOrder: columnSortOrder(sorter, 'requiredDate'), render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 130 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', width: 150, sorter: true, sortOrder: columnSortOrder(sorter, 'createdAt'), render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]

  return (
    <>
      <PageHeader
        title="Đơn bán hàng (SO)"
        extra={<>
        <ExportButton filename="don-ban-hang.xlsx" fetchRows={() => salesOrdersApi.list({ keyword, status, sort, dir, size: 10000 }).then(r => r.content)} />
        <Input.Search allowClear key={`q-${keyword}`} defaultValue={keyword} placeholder="Tìm theo mã đơn" style={{ width: 220 }} prefix={<SearchOutlined />}
          onSearch={(v) => setKeyword(v)} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={SO_STATUS_OPTS} value={status}
          onChange={(v) => setStatus(v)} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.OUTBOUND_CREATE_SO}><Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo đơn bán</Button></Can>
        </>}
      />
      <FitTable rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []}
        columns={columns} scroll={{ x: 'max-content' }}
        onChange={(_p, _f, s, extra) => { if (extra.action === 'sort') setSorter(s) }}
        pagination={{
          current: (pageData?.page ?? 0) + 1, pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0, showSizeChanger: true, showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager(p - 1, s),
        }} />
    </>
  )
}

function useLookups() {
  const { customers, warehouses, customerMap, warehouseMap } = useNameMaps()
  const { query: products, list: productList, map: productMap } = useProducts()
  return { customers, warehouses, products, productList, productMap, customerMap, warehouseMap }
}
const productOptions = (list) => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))

function CreateSO({ onCreated }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const [form] = Form.useForm()
  const { customers, warehouses, productList } = useLookups()

  const createMut = useMutation({
    mutationFn: salesOrdersApi.create,
    onSuccess: (so) => { message.success(`Đã tạo đơn ${so.soNumber || ''}`.trim()); onCreated(so) },
    onError: (e) => handleFormError(form, e, message),
  })

  const submit = async () => {
    const v = await form.validateFields()
    createMut.mutate({
      warehouseId: v.warehouseId,
      customerId: v.customerId,
      discountAmount: v.discountAmount ?? null,
      priority: v.priority ?? 0,
      requiredDate: v.requiredDate ? v.requiredDate.format('YYYY-MM-DD') : null,
      createdBy: user?.userId,               // BE yêu cầu createdBy
      lines: v.lines.map(l => ({
        productId: l.productId,
        quantityOrdered: l.quantityOrdered,
        unitPrice: l.unitPrice,
        discountPercent: l.discountPercent ?? null,
      })),
    })
  }

  return (
    <Card title="Tạo đơn bán mới">
      <Form form={form} layout="vertical" initialValues={{ lines: [{}], priority: 0 }}>
        <Row gutter={16}>
          <Col xs={24} md={8}>
            <Form.Item name="customerId" label="Khách hàng" rules={[{ required: true, message: 'Chọn KH' }]}>
              <Select showSearch optionFilterProp="label" loading={customers.isLoading} placeholder="Chọn KH"
                options={(customers.data || []).map(c => ({ value: c.id, label: `${c.name} (${c.code})` }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item name="warehouseId" label="Kho xuất" rules={[{ required: true, message: 'Chọn kho' }]}>
              <Select showSearch optionFilterProp="label" loading={warehouses.isLoading} placeholder="Chọn kho"
                options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
            </Form.Item>
          </Col>
          <Col xs={12} md={4}>
            <Form.Item name="requiredDate" label="Cần giao">
              <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
            </Form.Item>
          </Col>
          <Col xs={12} md={4}>
            <Form.Item name="discountAmount" label="Giảm giá (đ)">
              <InputNumber min={0} style={{ width: '100%' }} formatter={fmt} parser={parse} />
            </Form.Item>
          </Col>
          <Col xs={12} md={4}>
            <Form.Item name="priority" label="Ưu tiên" tooltip="Số càng lớn càng ưu tiên (mặc định 0)">
              <InputNumber min={0} style={{ width: '100%' }} placeholder="0" />
            </Form.Item>
          </Col>
        </Row>

        <Divider orientation="left" style={{ margin: '4px 0 12px' }}>Dòng hàng</Divider>
        <Form.List name="lines">
          {(fields, { add, remove }) => (
            <>
              {fields.map(({ key, name, ...rest }) => (
                <Row gutter={8} key={key} align="middle">
                  <Col flex="auto">
                    <Form.Item {...rest} name={[name, 'productId']} rules={[{ required: true, message: 'Chọn SP' }]}>
                      <Select showSearch optionFilterProp="label" placeholder="Sản phẩm" options={productOptions(productList)} />
                    </Form.Item>
                  </Col>
                  <Col flex="120px">
                    <Form.Item {...rest} name={[name, 'quantityOrdered']} rules={[{ required: true, message: 'SL' }]}>
                      <InputNumber min={1} placeholder="SL" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="150px">
                    <Form.Item {...rest} name={[name, 'unitPrice']} rules={[{ required: true, message: 'Đơn giá' }]}>
                      <InputNumber min={0} placeholder="Đơn giá" style={{ width: '100%' }} formatter={fmt} parser={parse} />
                    </Form.Item>
                  </Col>
                  <Col flex="110px">
                    <Form.Item {...rest} name={[name, 'discountPercent']}>
                      <InputNumber min={0} max={100} placeholder="CK %" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="40px">
                    <Button danger type="text" icon={<DeleteOutlined />} disabled={fields.length === 1} onClick={() => remove(name)} />
                  </Col>
                </Row>
              ))}
              <Button type="dashed" block icon={<PlusOutlined />} onClick={() => add({})}>Thêm dòng</Button>
            </>
          )}
        </Form.List>

        <div style={{ marginTop: 16, textAlign: 'right' }}>
          <Button type="primary" onClick={submit} loading={createMut.isPending}>Tạo đơn</Button>
        </div>
      </Form>
    </Card>
  )
}

function SODetail({ id }) {
  const { message } = AntdApp.useApp()
  const { hasPermission } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { productMap, customerMap, warehouseMap } = useLookups()
  const { isPicked } = usePickingStatusBySo()

  const { data: so, isLoading, isError, error } = useQuery({
    queryKey: ['so', id], queryFn: () => salesOrdersApi.get(id),
  })
  const refresh = (updated) => { qc.setQueryData(['so', id], updated); qc.invalidateQueries({ queryKey: ['so-list'] }) }

  const allocateMut = useMutation({
    mutationFn: () => salesOrdersApi.allocate(id),
    onSuccess: (u) => { message.success('Đã phân bổ (giữ tồn)'); refresh(u) },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const cancelMut = useMutation({
    mutationFn: () => salesOrdersApi.cancel(id),
    onSuccess: (u) => { message.success('Đã huỷ đơn'); refresh(u) },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const createPickMut = useMutation({
    mutationFn: () => pickingListsApi.create(id),
    onSuccess: (pl) => { message.success('Đã tạo lệnh lấy hàng'); navigate(`/picking-lists/${pl.id}`) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy đơn bán')} /></Card>

  const s = so.status
  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SKU', dataIndex: 'productId', width: 120, render: (pid) => productMap[pid]?.sku || '—' },
    { title: 'SL đặt', dataIndex: 'quantityOrdered', width: 90, align: 'right' },
    { title: 'SL đã lấy', dataIndex: 'quantityPicked', width: 100, align: 'right' },
    { title: 'Đơn giá', dataIndex: 'unitPrice', width: 120, align: 'right', render: (v) => v != null ? Number(v).toLocaleString('vi-VN') : '—' },
    { title: 'CK %', dataIndex: 'discountPercent', width: 80, align: 'right', render: (v) => v != null ? v : '—' },
  ]

  return (
    <Card
      title={<Space>Đơn bán <b>{so.soNumber || so.id}</b> {soTag(s, isPicked(so.id))}</Space>}
      extra={
        <Space wrap>
          {s === 'DRAFT' && (
            <Can permission={P.OUTBOUND_PICK}>
              <Button type="primary" icon={<ThunderboltOutlined />} loading={allocateMut.isPending}
                onClick={() => allocateMut.mutate()}>Phân bổ tồn</Button>
            </Can>
          )}
          {s === 'ALLOCATED' && (
            <Can permission={P.OUTBOUND_PICK}>
              <Button type="primary" icon={<ProfileOutlined />} loading={createPickMut.isPending}
                onClick={() => createPickMut.mutate()}>Tạo lệnh lấy hàng</Button>
            </Can>
          )}
          {(s === 'PICKING') && hasPermission(P.OUTBOUND_SHIP) && (
            <Button type="primary" icon={<CarOutlined />}
              onClick={() => navigate(`/shipments/new?soId=${so.id}`)}>Tạo vận đơn</Button>
          )}
          {(s === 'DRAFT' || s === 'ALLOCATED') && (
            <Can permission={P.OUTBOUND_CREATE_SO}>
              <Popconfirm title="Huỷ đơn bán này?"
                description={<span>Huỷ đơn <b>{so.soNumber || so.id}</b>. Hàng đã phân bổ sẽ được trả lại tồn khả dụng.</span>}
                okText="Huỷ đơn" okButtonProps={{ danger: true }} cancelText="Không"
                onConfirm={() => cancelMut.mutate()}>
                <Button danger icon={<CloseOutlined />} loading={cancelMut.isPending}>Huỷ</Button>
              </Popconfirm>
            </Can>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered
        items={[
          { key: 'cus', label: 'Khách hàng', children: customerMap[so.customerId]?.name || so.customerId },
          { key: 'wh', label: 'Kho xuất', children: warehouseMap[so.warehouseId]?.name || so.warehouseId },
          { key: 'req', label: 'Cần giao', children: so.requiredDate ? dayjs(so.requiredDate).format('DD/MM/YYYY') : '—' },
          { key: 'disc', label: 'Giảm giá', children: so.discountAmount != null ? Number(so.discountAmount).toLocaleString('vi-VN') : '—' },
          { key: 'cb', label: 'Người tạo', children: so.createdBy || '—' },
          { key: 'ca', label: 'Tạo lúc', children: so.createdAt ? dayjs(so.createdAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={so.details || []} columns={columns} scroll={{ x: 'max-content' }} />
    </Card>
  )
}

const fmt = (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
const parse = (v) => v?.replace(/,/g, '')
