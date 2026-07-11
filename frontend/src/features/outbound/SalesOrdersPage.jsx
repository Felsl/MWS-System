import ExportButton from '../../components/ExportButton'
import FitTable from '../../components/FitTable'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { sorterToParams, columnSortOrder } from '../../utils/sort'
import {
  Card, Button, Input, Form, Select, DatePicker, InputNumber, Row, Col,
  Table, Space, Typography, Tag, Descriptions, Empty, Popconfirm, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, ReloadOutlined, ArrowLeftOutlined,
  ThunderboltOutlined, ProfileOutlined, CarOutlined, CloseOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { salesOrdersApi } from '../../api/salesOrders.api'
import { pickingListsApi } from '../../api/pickingLists.api'
import { productsApi } from '../../api/products.api'
import { customersApi } from '../../api/partners.api'
import { warehousesApi } from '../../api/warehouses.api'

const SO_STATUS = {
  DRAFT: { color: 'default', label: 'Nháp' },
  ALLOCATED: { color: 'blue', label: 'Đã phân bổ' },
  PICKING: { color: 'gold', label: 'Đang lấy hàng' },
  SHIPPED: { color: 'green', label: 'Đã xuất' },
  CANCELLED: { color: 'red', label: 'Đã huỷ' },
}
const soTag = (s) => <Tag color={SO_STATUS[s]?.color || 'default'}>{SO_STATUS[s]?.label || s}</Tag>
const SO_STATUS_OPTS = Object.entries(SO_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useNameMaps() {
  const customers = useQuery({ queryKey: ['customers'], queryFn: customersApi.list })
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const customerMap = useMemo(() => Object.fromEntries((customers.data || []).map(c => [c.id, c])), [customers.data])
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { customers, warehouses, customerMap, warehouseMap }
}

export default function SalesOrdersPage() {
  const [view, setView] = useState({ mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })
  return (
    <div>
      {view.mode !== 'list' && (
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
          <Space>
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
            <Typography.Title level={4} style={{ margin: 0 }}>Đơn bán hàng (SO)</Typography.Title>
          </Space>
        </div>
      )}
      {view.mode === 'list' && <SOList onOpen={openDetail} onCreate={() => setView({ mode: 'create', id: null })} />}
      {view.mode === 'create' && <CreateSO onCreated={(so) => openDetail(so.id)} />}
      {view.mode === 'detail' && view.id && <SODetail id={view.id} />}
    </div>
  )
}

function SOList({ onOpen, onCreate }) {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const [sorter, setSorter] = useState(null)
  const { sort, dir } = sorterToParams(sorter)
  const { customerMap, warehouseMap } = useNameMaps()

  const list = useQuery({
    queryKey: ['so-list', keyword, status, pager.page, pager.size, sort, dir],
    queryFn: () => salesOrdersApi.list({ keyword, status, page: pager.page, size: pager.size, sort, dir }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data

  const columns = [
    { title: 'Mã đơn', dataIndex: 'soNumber', sorter: true, sortOrder: columnSortOrder(sorter, 'soNumber'), render: (v, r) => <a onClick={() => onOpen(r.id)}>{v || r.id}</a> },
    { title: 'Trạng thái', dataIndex: 'status', width: 140, sorter: true, sortOrder: columnSortOrder(sorter, 'status'), render: soTag },
    { title: 'Khách hàng', dataIndex: 'customerId', render: (v) => customerMap[v]?.name || v },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v, width: 150 },
    { title: 'Cần giao', dataIndex: 'requiredDate', width: 120, sorter: true, sortOrder: columnSortOrder(sorter, 'requiredDate'), render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 130 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', width: 150, sorter: true, sortOrder: columnSortOrder(sorter, 'createdAt'), render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]

  return (
    <>
      <Space style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={4} style={{ marginLeft: 20, marginTop: 0 }}>Đơn bán hàng (SO)</Typography.Title>
        <Space wrap>
        <ExportButton filename="don-ban-hang.xlsx" fetchRows={() => salesOrdersApi.list({ keyword, status, sort, dir, size: 10000 }).then(r => r.content)} />
        <Input.Search allowClear placeholder="Tìm theo mã đơn" style={{ width: 220 }} prefix={<SearchOutlined />}
          onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={SO_STATUS_OPTS} value={status}
          onChange={(v) => { setStatus(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.OUTBOUND_CREATE_SO}><Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo đơn bán</Button></Can>
        </Space>
      </Space>
      <FitTable rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []}
        columns={columns} scroll={{ x: 'max-content' }}
        onChange={(_p, _f, s, extra) => { if (extra.action === 'sort') { setSorter(s); setPager(p => ({ ...p, page: 0 })) } }}
        pagination={{
          current: (pageData?.page ?? 0) + 1, pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0, showSizeChanger: true, showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager({ page: p - 1, size: s }),
        }} />
    </>
  )
}

function useLookups() {
  const { customers, warehouses, customerMap, warehouseMap } = useNameMaps()
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const productList = products.data?.content || []
  const productMap = useMemo(() => Object.fromEntries(productList.map(p => [p.id, p])), [productList])
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
    onError: (e) => message.error(getErrorMessage(e)),
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
    onSuccess: (pl) => { message.success('Đã tạo lệnh lấy hàng'); navigate('/picking-lists', { state: { openId: pl.id } }) },
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
      title={<Space>Đơn bán <b>{so.soNumber || so.id}</b> {soTag(s)}</Space>}
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
              onClick={() => navigate('/shipments', { state: { salesOrderId: so.id } })}>Tạo vận đơn</Button>
          )}
          {(s === 'DRAFT' || s === 'ALLOCATED') && (
            <Can permission={P.OUTBOUND_CREATE_SO}>
              <Popconfirm title="Huỷ đơn bán này?" okText="Huỷ đơn" cancelText="Không"
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
