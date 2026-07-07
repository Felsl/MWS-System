import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, DatePicker, InputNumber, Row, Col,
  Table, Space, Typography, Tag, Descriptions, Empty, Popconfirm, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, SendOutlined, ReloadOutlined,
  CheckOutlined, CloseOutlined, InboxOutlined, ArrowLeftOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { purchaseOrdersApi } from '../../api/purchaseOrders.api'
import { productsApi } from '../../api/products.api'
import { suppliersApi } from '../../api/partners.api'
import { warehousesApi } from '../../api/warehouses.api'

const PO_STATUS = {
  DRAFT: { color: 'default', label: 'Nháp' },
  PENDING_REVIEW: { color: 'gold', label: 'Chờ kiểm tra' },
  PENDING_APPROVAL: { color: 'blue', label: 'Chờ phê duyệt' },
  APPROVED: { color: 'green', label: 'Đã duyệt' },
  ORDERED: { color: 'geekblue', label: 'Đã đặt' },
  CLOSED: { color: 'default', label: 'Đã đóng' },
  REJECTED: { color: 'red', label: 'Bị từ chối' },
  CANCELLED: { color: 'red', label: 'Đã huỷ' },
}
const poTag = (s) => <Tag color={PO_STATUS[s]?.color || 'default'}>{PO_STATUS[s]?.label || s}</Tag>
const PO_STATUS_OPTS = Object.entries(PO_STATUS).map(([value, m]) => ({ value, label: m.label }))

// Map id -> tên NCC / kho (nhẹ, dùng cho bảng list + header chi tiết)
function useNameMaps() {
  const suppliers = useQuery({ queryKey: ['suppliers'], queryFn: suppliersApi.list })
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const supplierMap = useMemo(() => Object.fromEntries((suppliers.data || []).map(s => [s.id, s])), [suppliers.data])
  const warehouseMap = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { suppliers, warehouses, supplierMap, warehouseMap }
}

export default function PurchaseOrdersPage() {
  const [view, setView] = useState({ mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {view.mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
          )}
          <Typography.Title level={4} style={{ margin: 0 }}>Đơn mua hàng (PO)</Typography.Title>
        </Space>
        <Can permission={P.INBOUND_CREATE_PO}>
          <Button type="primary" icon={<PlusOutlined />}
            onClick={() => setView({ mode: 'create', id: null })}>Tạo đơn mua</Button>
        </Can>
      </div>

      {view.mode === 'list' && <POList onOpen={openDetail} />}
      {view.mode === 'create' && <CreatePO onCreated={(po) => openDetail(po.id)} />}
      {view.mode === 'detail' && view.id && <PODetail id={view.id} />}
    </div>
  )
}

// ---- Bảng danh sách ----
function POList({ onOpen }) {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const { supplierMap, warehouseMap } = useNameMaps()

  const list = useQuery({
    queryKey: ['po-list', keyword, status, pager.page, pager.size],
    queryFn: () => purchaseOrdersApi.list({ keyword, status, page: pager.page, size: pager.size }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data

  const columns = [
    { title: 'Mã đơn', dataIndex: 'poNumber', render: (v, r) => <a onClick={() => onOpen(r.id)}>{v || r.id}</a> },
    { title: 'Trạng thái', dataIndex: 'status', width: 140, render: poTag },
    { title: 'Nhà cung cấp', dataIndex: 'supplierId', render: (v) => supplierMap[v]?.name || v },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v, width: 150 },
    { title: 'Ngày dự kiến', dataIndex: 'expectedDate', width: 120,
      render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Người tạo', dataIndex: 'createdBy', width: 130 },
    { title: 'Tạo lúc', dataIndex: 'createdAt', width: 150,
      render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]

  return (
    <>
      <Space style={{ marginBottom: 12 }} wrap>
        <Input.Search allowClear placeholder="Tìm theo mã đơn" style={{ width: 220 }}
          prefix={<SearchOutlined />}
          onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={PO_STATUS_OPTS} value={status}
          onChange={(v) => { setStatus(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
      </Space>

      <Table rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []}
        columns={columns} scroll={{ x: 'max-content' }}
        pagination={{
          current: (pageData?.page ?? 0) + 1,
          pageSize: pageData?.size ?? 20,
          total: pageData?.totalElements ?? 0,
          showSizeChanger: true,
          showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager({ page: p - 1, size: s }),
        }} />
    </>
  )
}

// ---- Lookups cho form tạo & chi tiết (kèm sản phẩm) ----
function useLookups() {
  const { suppliers, warehouses, supplierMap, warehouseMap } = useNameMaps()
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const productList = products.data?.content || []
  const productMap = useMemo(() => Object.fromEntries(productList.map(p => [p.id, p])), [productList])
  return { suppliers, warehouses, products, productList, productMap, supplierMap, warehouseMap }
}
const productOptions = (list) => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))

// ---- Form tạo PO ----
function CreatePO({ onCreated }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const { suppliers, warehouses, productList } = useLookups()

  const createMut = useMutation({
    mutationFn: purchaseOrdersApi.create,
    onSuccess: (po) => { message.success(`Đã tạo đơn ${po.poNumber || ''}`.trim()); onCreated(po) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const submit = async () => {
    const v = await form.validateFields()
    createMut.mutate({
      supplierId: v.supplierId,
      warehouseId: v.warehouseId,
      expectedDate: v.expectedDate ? v.expectedDate.format('YYYY-MM-DD') : null,
      lines: v.lines.map(l => ({
        productId: l.productId,
        quantityOrdered: l.quantityOrdered,
        unitPrice: l.unitPrice ?? null,
      })),
    })
  }

  return (
    <Card title="Tạo đơn mua mới">
      <Form form={form} layout="vertical" initialValues={{ lines: [{}] }}>
        <Row gutter={16}>
          <Col xs={24} md={8}>
            <Form.Item name="supplierId" label="Nhà cung cấp" rules={[{ required: true, message: 'Chọn NCC' }]}>
              <Select showSearch optionFilterProp="label" loading={suppliers.isLoading}
                placeholder="Chọn NCC"
                options={(suppliers.data || []).map(s => ({ value: s.id, label: `${s.name} (${s.code})` }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item name="warehouseId" label="Kho nhận" rules={[{ required: true, message: 'Chọn kho' }]}>
              <Select showSearch optionFilterProp="label" loading={warehouses.isLoading}
                placeholder="Chọn kho"
                options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item name="expectedDate" label="Ngày dự kiến">
              <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
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
                    <Form.Item {...rest} name={[name, 'productId']}
                      rules={[{ required: true, message: 'Chọn sản phẩm' }]}>
                      <Select showSearch optionFilterProp="label" placeholder="Sản phẩm"
                        options={productOptions(productList)} />
                    </Form.Item>
                  </Col>
                  <Col flex="130px">
                    <Form.Item {...rest} name={[name, 'quantityOrdered']}
                      rules={[{ required: true, message: 'SL' }]}>
                      <InputNumber min={1} placeholder="SL đặt" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="150px">
                    <Form.Item {...rest} name={[name, 'unitPrice']}>
                      <InputNumber min={0} placeholder="Đơn giá" style={{ width: '100%' }}
                        formatter={fmt} parser={parse} />
                    </Form.Item>
                  </Col>
                  <Col flex="40px">
                    <Button danger type="text" icon={<DeleteOutlined />}
                      disabled={fields.length === 1} onClick={() => remove(name)} />
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

// ---- Chi tiết PO + workflow ----
function PODetail({ id }) {
  const { message } = AntdApp.useApp()
  const { hasPermission } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { productMap, supplierMap, warehouseMap } = useLookups()

  const { data: po, isLoading, isError, error } = useQuery({
    queryKey: ['po', id],
    queryFn: () => purchaseOrdersApi.get(id),
  })

  const mkAct = (mutFn, okMsg) => ({
    mutationFn: () => mutFn(id),
    onSuccess: (updated) => {
      message.success(okMsg)
      qc.setQueryData(['po', id], updated)
      qc.invalidateQueries({ queryKey: ['po-list'] })
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const reviewMut = useMutation(mkAct(purchaseOrdersApi.submitReview, 'Đã gửi kiểm tra'))
  const approvalMut = useMutation(mkAct(purchaseOrdersApi.submitApproval, 'Đã trình phê duyệt'))
  const approveMut = useMutation(mkAct(purchaseOrdersApi.approve, 'Đã phê duyệt'))
  const rejectMut = useMutation(mkAct(purchaseOrdersApi.reject, 'Đã từ chối'))

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy đơn mua')} /></Card>

  const s = po.status
  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SKU', dataIndex: 'productId', render: (pid) => productMap[pid]?.sku || '—', width: 120 },
    { title: 'SL đặt', dataIndex: 'quantityOrdered', width: 90, align: 'right' },
    { title: 'SL đã nhận', dataIndex: 'quantityReceived', width: 110, align: 'right' },
    { title: 'Đơn giá', dataIndex: 'unitPrice', width: 120, align: 'right',
      render: (v) => v != null ? Number(v).toLocaleString('vi-VN') : '—' },
    { title: 'Thành tiền', key: 'lt', width: 130, align: 'right',
      render: (_, r) => r.unitPrice != null ? (Number(r.unitPrice) * r.quantityOrdered).toLocaleString('vi-VN') : '—' },
  ]
  const busy = reviewMut.isPending || approvalMut.isPending || approveMut.isPending || rejectMut.isPending

  return (
    <Card
      title={<Space>Đơn mua <b>{po.poNumber || po.id}</b> {poTag(s)}</Space>}
      extra={
        <Space wrap>
          {s === 'DRAFT' && (
            <Can permission={P.INBOUND_CREATE_PO}>
              <Button icon={<SendOutlined />} loading={busy} onClick={() => reviewMut.mutate()}>Gửi kiểm tra</Button>
            </Can>
          )}
          {s === 'PENDING_REVIEW' && (
            <Can permission={P.INBOUND_CREATE_PO}>
              <Button type="primary" icon={<SendOutlined />} loading={busy}
                onClick={() => approvalMut.mutate()}>Trình phê duyệt</Button>
            </Can>
          )}
          {s === 'PENDING_APPROVAL' && (
            <Can permission={P.INBOUND_APPROVE_PO}>
              <Button type="primary" icon={<CheckOutlined />} loading={busy}
                onClick={() => approveMut.mutate()}>Phê duyệt</Button>
            </Can>
          )}
          {(s === 'PENDING_REVIEW' || s === 'PENDING_APPROVAL') && (
            <Can permission={P.INBOUND_APPROVE_PO}>
              <Popconfirm title="Từ chối đơn này?" okText="Từ chối" cancelText="Huỷ"
                onConfirm={() => rejectMut.mutate()}>
                <Button danger icon={<CloseOutlined />} loading={busy}>Từ chối</Button>
              </Popconfirm>
            </Can>
          )}
          {s === 'APPROVED' && hasPermission(P.INBOUND_CREATE_GRN) && (
            <Button type="primary" icon={<InboxOutlined />}
              onClick={() => navigate('/goods-receipts', { state: { poId: po.id } })}>
              Tạo phiếu nhập
            </Button>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2, md: 3 }} bordered
        items={[
          { key: 'sup', label: 'Nhà cung cấp', children: supplierMap[po.supplierId]?.name || po.supplierId },
          { key: 'wh', label: 'Kho', children: warehouseMap[po.warehouseId]?.name || po.warehouseId },
          { key: 'exp', label: 'Ngày dự kiến', children: po.expectedDate ? dayjs(po.expectedDate).format('DD/MM/YYYY') : '—' },
          { key: 'cb', label: 'Người tạo', children: po.createdBy || '—' },
          { key: 'ab', label: 'Người duyệt', children: po.approvedBy || '—' },
          { key: 'ca', label: 'Tạo lúc', children: po.createdAt ? dayjs(po.createdAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={po.details || []} columns={columns} scroll={{ x: 'max-content' }} />
    </Card>
  )
}

const fmt = (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
const parse = (v) => v?.replace(/,/g, '')
