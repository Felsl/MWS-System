import { useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Row, Col, Card, Button, Input, List, Form, Select, InputNumber, DatePicker,
  Table, Space, Typography, Tag, Descriptions, Empty, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, SearchOutlined, DeleteOutlined, CheckCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { goodsReceiptsApi } from '../../api/goodsReceipts.api'
import { purchaseOrdersApi } from '../../api/purchaseOrders.api'
import { productsApi } from '../../api/products.api'
import { warehousesApi } from '../../api/warehouses.api'
import { useRecent } from './useRecent'

const GRN_STATUS = {
  PENDING: { color: 'gold', label: 'Chờ hoàn thành' },
  COMPLETED: { color: 'green', label: 'Đã hoàn thành' },
}
const grnTag = (s) => <Tag color={GRN_STATUS[s]?.color || 'default'}>{GRN_STATUS[s]?.label || s}</Tag>

export default function GoodsReceiptsPage() {
  const location = useLocation()
  const poIdFromNav = location.state?.poId || null
  const [mode, setMode] = useState(poIdFromNav ? 'create' : 'detail')
  const [currentId, setCurrentId] = useState(null)
  const recent = useRecent('mws_recent_grn')

  const openDetail = (id) => { setCurrentId(id); setMode('detail') }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>Phiếu nhập kho (GRN)</Typography.Title>
        <Can permission={P.INBOUND_CREATE_GRN}>
          <Button type="primary" icon={<PlusOutlined />}
            onClick={() => { setMode('create'); setCurrentId(null) }}>Tạo phiếu nhập</Button>
        </Can>
      </div>
      <Typography.Paragraph type="secondary" style={{ marginTop: -8 }}>
        BE chưa có API liệt kê phiếu nhập — tra cứu theo ID hoặc chọn từ danh sách gần đây (lưu ở máy này).
      </Typography.Paragraph>

      <Row gutter={16}>
        <Col xs={24} md={7} lg={6}>
          <Card size="small" title="Tra cứu / Gần đây" styles={{ body: { padding: 12 } }}>
            <Input.Search placeholder="Dán ID phiếu nhập" allowClear enterButton={<SearchOutlined />}
              onSearch={(v) => v && openDetail(v.trim())} />
            <Divider style={{ margin: '12px 0' }} />
            {recent.items.length === 0
              ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có" />
              : <List size="small" dataSource={recent.items}
                  renderItem={(it) => (
                    <List.Item style={{ cursor: 'pointer' }} onClick={() => openDetail(it.id)}>
                      <List.Item.Meta title={it.grnNumber || it.id} description={grnTag(it.status)} />
                    </List.Item>
                  )} />}
            {recent.items.length > 0 && (
              <Button type="link" size="small" danger onClick={recent.clear}>Xoá danh sách</Button>
            )}
          </Card>
        </Col>

        <Col xs={24} md={17} lg={18}>
          {mode === 'create'
            ? <CreateGRN initialPoId={poIdFromNav}
                onCreated={(grn) => { recent.push({ id: grn.id, grnNumber: grn.grnNumber, status: grn.status }); openDetail(grn.id) }} />
            : currentId
              ? <GRNDetail id={currentId} onChanged={(grn) => recent.push({ id: grn.id, grnNumber: grn.grnNumber, status: grn.status })} />
              : <Card><Empty description="Chọn một phiếu nhập hoặc tạo mới" /></Card>}
        </Col>
      </Row>
    </div>
  )
}

function useProductMap() {
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const list = products.data?.content || []
  const map = useMemo(() => Object.fromEntries(list.map(p => [p.id, p])), [list])
  return { products, list, map }
}
const productOptions = (list) => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` }))

function CreateGRN({ initialPoId, onCreated }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const [poId, setPoId] = useState(initialPoId || '')
  const { products, list: productList } = useProductMap()
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false) })

  // Kho đang chọn -> tải ô kệ để putaway
  const warehouseId = Form.useWatch('warehouseId', form)
  const bins = useQuery({
    queryKey: ['bins', warehouseId],
    queryFn: () => warehousesApi.listBins(warehouseId),
    enabled: !!warehouseId,
  })

  // Nạp PO để prefill dòng hàng (nếu có poId)
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
                  {/* poDetailId ẩn để gắn về dòng PO khi prefill */}
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

function GRNDetail({ id, onChanged }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const { map: productMap } = useProductMap()

  const { data: grn, isLoading, isError, error } = useQuery({
    queryKey: ['grn', id],
    queryFn: () => goodsReceiptsApi.get(id),
  })
  const completeMut = useMutation({
    mutationFn: () => goodsReceiptsApi.complete(id),
    onSuccess: (updated) => { message.success('Đã hoàn thành nhập kho'); qc.setQueryData(['grn', id], updated); onChanged?.(updated) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy phiếu nhập')} /></Card>

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'SL', dataIndex: 'quantity', width: 80, align: 'right' },
    { title: 'Số lô', dataIndex: 'batchNumber', width: 130, render: (v) => v || '—' },
    { title: 'HSD', dataIndex: 'expiryDate', width: 120, render: (v) => v ? dayjs(v).format('DD/MM/YYYY') : '—' },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 140 },
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
