import ExportButton from '../../components/ExportButton'
import PageHeader from '../../components/PageHeader'
import RowLink from '../../components/RowLink'
import { sorterToParams, columnSortOrder } from '../../utils/sort'
import FitTable from '../../components/FitTable'
import { useEffect, useMemo, useRef, useState } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useLocation } from 'react-router-dom'
import { keepPreviousData, useQuery, useQueries, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, InputNumber, DatePicker, Row, Col,
  Table, Space, Typography, Tag, Descriptions, Empty, Divider, App as AntdApp, Alert,
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
import { inventoryApi } from '../../api/inventory.api'
import { allocateBins } from './allocateBins'
import { purchaseOrdersApi } from '../../api/purchaseOrders.api'
import { warehousesApi } from '../../api/warehouses.api'
import { suppliersApi } from '../../api/partners.api'
import { useAuth } from '../../auth/AuthContext'
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
  const { user } = useAuth()
  // Giá khoá mặc định (tự điền từ giá vốn); chỉ ADMIN mới sửa tay được.
  const canEditPrice = user?.role === 'ADMIN'
  const suppliers = useQuery({ queryKey: ['suppliers'], queryFn: suppliersApi.list })
  const supplierOptions = (suppliers.data || []).map(s => ({ value: s.id, label: `${s.name}${s.code ? ` (${s.code})` : ''}` }))
  const pById = Object.fromEntries((productList || []).map(p => [p.id, p]))
  // Khi đổi sản phẩm ở một dòng -> tự gắn đơn giá = giá vốn (costPrice) của sản phẩm.
  const fillPriceFromProduct = (name, productId) => {
    const p = pById[productId]
    form.setFieldValue(['lines', name, 'unitPrice'], p?.costPrice != null ? Number(p.costPrice) : 0)
  }

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
          supplierId: d.supplierId || null,
          quantity: Math.max(0, (d.quantityOrdered || 0) - (d.quantityReceived || 0)) || 1,
          unitPrice: d.unitPrice != null ? Number(d.unitPrice)
            : (pById[d.productId]?.costPrice != null ? Number(pById[d.productId].costPrice) : 0),
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
        supplierId: l.supplierId || null,
        unitPrice: l.unitPrice != null ? l.unitPrice : null,
      })),
    })
  }

  const binOptions = (bins.data || []).map(b => ({ value: b.id, label: b.coordinateLabel || b.id }))

  // [PA1] Cảnh báo mềm khi nhập: cộng (đang chiếm + sắp thêm) theo ô kệ, so với giới hạn cấu hình.
  // KHÔNG chặn ở đây — chỉ báo. Chặn cứng nằm ở BE lúc HOÀN TẤT phiếu (-> 409).
  const lines = Form.useWatch('lines', form)
  const capacityWarnings = useMemo(() => {
    const binById = Object.fromEntries((bins.data || []).map(b => [b.id, b]))
    const pById = Object.fromEntries((productList || []).map(p => [p.id, p]))
    const add = {} // binId -> { w, v }
    for (const l of (lines || [])) {
      if (!l || !l.binLocationId || !l.productId || !l.quantity) continue
      const p = pById[l.productId]; if (!p) continue
      const a = add[l.binLocationId] || (add[l.binLocationId] = { w: 0, v: 0 })
      a.w += Number(p.weight || 0) * Number(l.quantity)
      a.v += Number(p.volume || 0) * Number(l.quantity)
    }
    const warns = []
    for (const [binId, a] of Object.entries(add)) {
      const b = binById[binId]; if (!b) continue
      const label = b.coordinateLabel || binId
      const maxW = Number(b.maxWeight || 0), maxV = Number(b.maxVolume || 0)
      const projW = Number(b.occupiedWeight || 0) + a.w
      const projV = Number(b.occupiedVolume || 0) + a.v
      if (maxW > 0 && projW > maxW) warns.push(`${label}: tải trọng ${projW.toFixed(1)}/${maxW} kg`)
      if (maxV > 0 && projV > maxV) warns.push(`${label}: thể tích ${projV.toFixed(1)}/${maxV} m³`)
    }
    return warns
  }, [lines, bins.data, productList])

  // [lat7] Tự phân bổ ô kệ: lấy các ô đã có sẵn từng sản phẩm để ưu tiên gom.
  const distinctProductIds = useMemo(
    () => [...new Set((lines || []).map(l => l?.productId).filter(Boolean))],
    [lines],
  )
  const batchQueries = useQueries({
    queries: distinctProductIds.map(pid => ({
      queryKey: ['batches', pid, warehouseId],
      queryFn: () => inventoryApi.getBatches(pid, warehouseId),
      enabled: !!warehouseId && !!pid,
    })),
  })
  const existingBinsByProduct = useMemo(() => {
    const m = {}
    distinctProductIds.forEach((pid, i) => {
      m[pid] = new Set((batchQueries[i]?.data || []).map(b => b.binLocationId).filter(Boolean))
    })
    return m
  }, [distinctProductIds, batchQueries])
  const existingReady = batchQueries.every(q => !q.isLoading)

  const runAllocate = () => {
    const cur = form.getFieldValue('lines') || []
    const pMap = Object.fromEntries((productList || []).map(p => [p.id, p]))
    const next = allocateBins(cur, bins.data || [], pMap, existingBinsByProduct)
    form.setFieldsValue({ lines: next })
  }

  // Tự phân bổ MỘT LẦN khi nạp xong đơn mua + đã có danh sách ô kệ + tồn từng SP.
  const allocatedForPo = useRef(null)
  useEffect(() => {
    if (!po.data || !bins.data || !existingReady) return
    if (allocatedForPo.current === poId) return
    allocatedForPo.current = poId
    runAllocate()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [po.data, bins.data, existingReady, poId])

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
        {warehouseId && (
          <div style={{ marginBottom: 8 }}>
            <Button size="small" onClick={runAllocate}
              disabled={!bins.data?.length || !(lines || []).some(l => l?.productId && l?.quantity)}>
              Tự phân bổ ô kệ
            </Button>
            <Typography.Text type="secondary" style={{ fontSize: 12, marginLeft: 8 }}>
              Ưu tiên ô đã có cùng sản phẩm, tách sang ô khác khi đầy; sửa tay lại được. Không đủ chỗ sẽ hiện cảnh báo bên dưới.
            </Typography.Text>
          </div>
        )}
        <Form.List name="lines">
          {(fields, { add, remove }) => (
            <>
              {fields.map(({ key, name, ...rest }) => (
                <Row gutter={8} key={key} align="middle" style={{ marginBottom: 4 }}>
                  <Form.Item {...rest} name={[name, 'poDetailId']} hidden><Input /></Form.Item>
                  <Col flex="220px">
                    <Form.Item {...rest} name={[name, 'productId']} rules={[{ required: true, message: 'SP' }]}>
                      <Select showSearch optionFilterProp="label" placeholder="Sản phẩm"
                        options={productOptions(productList)} loading={products.isLoading}
                        onChange={(pid) => fillPriceFromProduct(name, pid)} />
                    </Form.Item>
                  </Col>
                  <Col flex="180px">
                    <Form.Item {...rest} name={[name, 'supplierId']}>
                      <Select showSearch optionFilterProp="label" placeholder="Nhà cung cấp"
                        allowClear options={supplierOptions} loading={suppliers.isLoading} />
                    </Form.Item>
                  </Col>
                  <Col flex="120px">
                    <Form.Item {...rest} name={[name, 'unitPrice']}
                      tooltip={canEditPrice ? 'Tự điền theo giá vốn, có thể sửa' : 'Giá vốn — chỉ ADMIN sửa được'}>
                      <InputNumber min={0} placeholder="Đơn giá" style={{ width: '100%' }}
                        disabled={!canEditPrice}
                        formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                        parser={(v) => (v || '').replace(/,/g, '')} />
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
          {capacityWarnings.length > 0 && (
            <Alert type="warning" showIcon style={{ marginBottom: 12, textAlign: 'left' }}
              message="Vượt sức chứa ô kệ (cảnh báo — vẫn tạo phiếu được; chặn cứng khi hoàn tất)"
              description={<ul style={{ margin: 0, paddingLeft: 18 }}>
                {capacityWarnings.map((w, i) => <li key={i}>{w}</li>)}
              </ul>} />
          )}
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
  const suppliers = useQuery({ queryKey: ['suppliers'], queryFn: suppliersApi.list })
  const supplierMap = Object.fromEntries((suppliers.data || []).map(s => [s.id, s]))
  const fmtVnd = (v) => v == null ? '—' : `${Number(v).toLocaleString('vi-VN')} đ`

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
    { title: 'Nhà cung cấp', dataIndex: 'supplierId', width: 160, ellipsis: true, render: (v) => supplierMap[v]?.name || '—' },
    { title: 'Đơn giá', dataIndex: 'unitPrice', width: 120, align: 'right', render: fmtVnd },
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
