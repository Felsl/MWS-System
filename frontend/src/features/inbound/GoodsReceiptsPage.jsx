import ExportButton from '../../components/ExportButton'
import PageHeader from '../../components/PageHeader'
import RowLink from '../../components/RowLink'
import { sorterToParams, columnSortOrder } from '../../utils/sort'
import FitTable from '../../components/FitTable'
import { useEffect, useMemo, useState } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useLocation } from 'react-router-dom'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, InputNumber, DatePicker, Row, Col,
  Table, Space, Typography, Tag, Descriptions, Empty, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, CheckCircleOutlined,
  ReloadOutlined, ArrowLeftOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { goodsReceiptsApi } from '../../api/goodsReceipts.api'
import { purchaseOrdersApi } from '../../api/purchaseOrders.api'
import { warehousesApi } from '../../api/warehouses.api'
import { useBinLabels } from '../../hooks/useBinLabels'

const GRN_STATUS = {
  PENDING: { color: 'gold', label: 'Chờ hoàn thành' },
  COMPLETED: { color: 'green', label: 'Đã hoàn thành' },
}
const grnTag = (s) => <Tag color={GRN_STATUS[s]?.color || 'default'}>{GRN_STATUS[s]?.label || s}</Tag>
const GRN_STATUS_OPTS = Object.entries(GRN_STATUS).map(([value, m]) => ({ value, label: m.label }))

function useWarehouseMap() {
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })
  const map = useMemo(() => Object.fromEntries((warehouses.data || []).map(w => [w.id, w])), [warehouses.data])
  return { warehouses, warehouseMap: map }
}
function useProductMap() {
  const { query: products, list, map } = useProducts()
  return { products, list, map }
}
const productOptions = (list) => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))

export default function GoodsReceiptsPage() {
  const location = useLocation()
  const poIdFromNav = location.state?.poId || null
  const [view, setView] = useState(poIdFromNav ? { mode: 'create', id: null } : { mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })

  return (
    <div>
      {view.mode !== 'list' && (
        <PageHeader title="Phiếu nhập kho (GRN)"
          onBack={<Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>} />
      )}

      {view.mode === 'list' && <GRNList onOpen={openDetail} onCreate={() => setView({ mode: 'create', id: null })} />}
      {view.mode === 'create' && <CreateGRN initialPoId={poIdFromNav} onCreated={(grn) => openDetail(grn.id)} />}
      {view.mode === 'detail' && view.id && <GRNDetail id={view.id} />}
    </div>
  )
}

function GRNList({ onOpen, onCreate }) {
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState()
  const [pager, setPager] = useState({ page: 0, size: 20 })
  const [sorter, setSorter] = useState(null)
  const { sort, dir } = sorterToParams(sorter)
  const { warehouseMap } = useWarehouseMap()

  const list = useQuery({
    queryKey: ['grn-list', keyword, status, pager.page, pager.size, sort, dir],
    queryFn: () => goodsReceiptsApi.list({ keyword, status, page: pager.page, size: pager.size, sort, dir }),
    placeholderData: keepPreviousData,
  })
  const pageData = list.data

  const columns = [
    { title: 'Mã phiếu', dataIndex: 'grnNumber', sorter: true, sortOrder: columnSortOrder(sorter, 'grnNumber'), render: (v, r) => <RowLink onClick={() => onOpen(r.id)}>{v || r.id}</RowLink> },
    { title: 'Trạng thái', dataIndex: 'status', sorter: true, sortOrder: columnSortOrder(sorter, 'status'), width: 150, render: grnTag },
    { title: 'Kho', dataIndex: 'warehouseId', render: (v) => warehouseMap[v]?.name || v, width: 160 },
    { title: 'Từ đơn mua', dataIndex: 'poId', render: (v) => v || '— (tự do)' },
    { title: 'Người nhận', dataIndex: 'receivedBy', width: 130 },
    { title: 'Nhận lúc', dataIndex: 'receivedAt', sorter: true, sortOrder: columnSortOrder(sorter, 'receivedAt'), width: 150,
      render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]

  return (
    <>
      <PageHeader
        title="Phiếu nhập kho (GRN)"
        extra={<>
        <ExportButton filename="phieu-nhap-kho.xlsx" fetchRows={() => goodsReceiptsApi.list({ keyword, status, sort, dir, size: 10000 }).then(r => r.content)} />
        <Input.Search allowClear placeholder="Tìm theo mã phiếu" style={{ width: 220 }}
          prefix={<SearchOutlined />}
          onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Select allowClear placeholder="Lọc trạng thái" style={{ width: 180 }}
          options={GRN_STATUS_OPTS} value={status}
          onChange={(v) => { setStatus(v); setPager(p => ({ ...p, page: 0 })) }} />
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.INBOUND_CREATE_GRN}><Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo phiếu nhập</Button></Can>
        </>}
      />

      <FitTable rowKey="id" loading={list.isLoading} dataSource={pageData?.content || []}
        columns={columns} scroll={{ x: 'max-content' }}
        onChange={(_p, _f, s, extra) => { if (extra.action === 'sort') { setSorter(s); setPager(p => ({ ...p, page: 0 })) } }}
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

function CreateGRN({ initialPoId, onCreated }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const [poId, setPoId] = useState(initialPoId || '')
  const { products, list: productList } = useProductMap()
  const { warehouses } = useWarehouseMap()

  const warehouseId = Form.useWatch('warehouseId', form)
  const bins = useQuery({
    queryKey: ['bins', warehouseId],
    queryFn: () => warehousesApi.listBins(warehouseId),
    enabled: !!warehouseId,
  })
  const po = useQuery({
    queryKey: ['po', poId],
    queryFn: () => purchaseOrdersApi.get(poId),
    enabled: !!poId,
  })

  useEffect(() => {
    if (po.data) {
      form.setFieldsValue({
        warehouseId: po.data.warehouseId,
        lines: (po.data.details || []).map(d => ({
          productId: d.productId,
          poDetailId: d.id,
          quantity: Math.max(0, (d.quantityOrdered || 0) - (d.quantityReceived || 0)) || 1,
        })),
      })
    }
  }, [po.data]) // eslint-disable-line react-hooks/exhaustive-deps

  const createMut = useMutation({
    mutationFn: goodsReceiptsApi.create,
    onSuccess: (grn) => { message.success(`Đã tạo phiếu ${grn.grnNumber || ''}`.trim()); onCreated(grn) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const submit = async () => {
    const v = await form.validateFields()
    createMut.mutate({
      poId: poId || null,
      warehouseId: v.warehouseId,
      note: v.note || null,
      lines: v.lines.map(l => ({
        productId: l.productId,
        poDetailId: l.poDetailId || null,
        quantity: l.quantity,
        batchNumber: l.batchNumber || null,
        expiryDate: l.expiryDate ? l.expiryDate.format('YYYY-MM-DD') : null,
        binLocationId: l.binLocationId,
      })),
    })
  }

  const binOptions = (bins.data || []).map(b => ({ value: b.id, label: b.coordinateLabel || b.id }))

  return (
    <Card title="Tạo phiếu nhập">
      <Row gutter={16} align="bottom">
        <Col xs={24} md={10}>
          <Typography.Text type="secondary">Từ đơn mua (tuỳ chọn)</Typography.Text>
          <Input.Search placeholder="Dán ID đơn mua đã duyệt để prefill" allowClear
            defaultValue={initialPoId || ''} enterButton="Nạp"
            onSearch={(val) => setPoId(val.trim())} loading={po.isFetching} />
          {po.isError && <Typography.Text type="danger">Không nạp được đơn mua.</Typography.Text>}
          {po.data && po.data.status !== 'APPROVED' && (
            <Typography.Text type="warning"> Đơn mua đang ở trạng thái {po.data.status}, thường chỉ nhập từ đơn ĐÃ DUYỆT.</Typography.Text>
          )}
        </Col>
      </Row>

      <Form form={form} layout="vertical" initialValues={{ lines: [{}] }} style={{ marginTop: 12 }}>
        <Row gutter={16}>
          <Col xs={24} md={8}>
            <Form.Item name="warehouseId" label="Kho nhận" rules={[{ required: true, message: 'Chọn kho' }]}>
              <Select showSearch optionFilterProp="label" loading={warehouses.isLoading}
                placeholder="Chọn kho"
                options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={16}>
            <Form.Item name="note" label="Ghi chú"><Input /></Form.Item>
          </Col>
        </Row>

        <Divider orientation="left" style={{ margin: '4px 0 12px' }}>Dòng nhập (putaway)</Divider>
        {!warehouseId && <Typography.Text type="secondary">Chọn kho trước để nạp danh sách ô kệ.</Typography.Text>}
        <Form.List name="lines">
          {(fields, { add, remove }) => (
            <>
              {fields.map(({ key, name, ...rest }) => (
                <Row gutter={8} key={key} align="middle" style={{ marginBottom: 4 }}>
                  <Form.Item {...rest} name={[name, 'poDetailId']} hidden><Input /></Form.Item>
                  <Col flex="220px">
                    <Form.Item {...rest} name={[name, 'productId']} rules={[{ required: true, message: 'SP' }]}>
                      <Select showSearch optionFilterProp="label" placeholder="Sản phẩm"
                        options={productOptions(productList)} loading={products.isLoading} />
                    </Form.Item>
                  </Col>
                  <Col flex="90px">
                    <Form.Item {...rest} name={[name, 'quantity']} rules={[{ required: true, message: 'SL' }]}>
                      <InputNumber min={1} placeholder="SL" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="130px">
                    <Form.Item {...rest} name={[name, 'batchNumber']}>
                      <Input placeholder="Số lô" />
                    </Form.Item>
                  </Col>
                  <Col flex="140px">
                    <Form.Item {...rest} name={[name, 'expiryDate']}>
                      <DatePicker placeholder="HSD" format="DD/MM/YYYY" style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col flex="150px">
                    <Form.Item {...rest} name={[name, 'binLocationId']} rules={[{ required: true, message: 'Ô kệ' }]}>
                      <Select showSearch optionFilterProp="label" placeholder="Ô kệ"
                        options={binOptions} loading={bins.isFetching} disabled={!warehouseId} />
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
          <Button type="primary" onClick={submit} loading={createMut.isPending}>Tạo phiếu nhập</Button>
        </div>
      </Form>
    </Card>
  )
}

function GRNDetail({ id }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const { map: productMap } = useProductMap()
  const { labelOf } = useBinLabels()

  const { data: grn, isLoading, isError, error } = useQuery({
    queryKey: ['grn', id],
    queryFn: () => goodsReceiptsApi.get(id),
  })
  const completeMut = useMutation({
    mutationFn: () => goodsReceiptsApi.complete(id),
    onSuccess: (updated) => {
      message.success('Đã hoàn thành nhập kho')
      qc.setQueryData(['grn', id], updated)
      qc.invalidateQueries({ queryKey: ['grn-list'] })
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy phiếu nhập')} /></Card>

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SL', dataIndex: 'quantity', width: 80, align: 'right' },
    { title: 'Số lô', dataIndex: 'batchNumber', width: 130, render: (v) => v || '—' },
    { title: 'HSD', dataIndex: 'expiryDate', width: 120, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 140, render: (v) => labelOf(v) },
  ]

  return (
    <Card
      title={<Space>Phiếu nhập <b>{grn.grnNumber || grn.id}</b> {grnTag(grn.status)}</Space>}
      extra={grn.status === 'PENDING' && (
        <Can permission={P.INBOUND_COMPLETE_GRN}>
          <Button type="primary" icon={<CheckCircleOutlined />} loading={completeMut.isPending}
            onClick={() => completeMut.mutate()}>Hoàn thành</Button>
        </Can>
      )}
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2 }} bordered
        items={[
          { key: 'po', label: 'Từ đơn mua', children: grn.poId || '— (nhập tự do)' },
          { key: 'rb', label: 'Người nhận', children: grn.receivedBy || '—' },
          { key: 'ra', label: 'Nhận lúc', children: grn.receivedAt ? dayjs(grn.receivedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'note', label: 'Ghi chú', children: grn.note || '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={grn.details || []} columns={columns} scroll={{ x: 'max-content' }} />
    </Card>
  )
}
