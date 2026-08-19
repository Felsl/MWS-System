import FitTable from '../../components/FitTable'
import RowLink from '../../components/RowLink'
import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useRecordView } from '../../hooks/useRecordView'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, Space, Typography, Tag, Descriptions, Empty, Modal, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, ReloadOutlined, ArrowLeftOutlined, TagOutlined,
  CarOutlined, CheckCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { shipmentsApi } from '../../api/shipments.api'
import { carriersApi } from '../../api/partners.api'
import { warehousesApi } from '../../api/warehouses.api'

const SH_STATUS = {
  PACKED: { color: 'default', label: 'Đã đóng gói' },
  HANDED_OVER: { color: 'blue', label: 'Đã bàn giao' },
  SHIPPING: { color: 'gold', label: 'Đang giao' },
  DELIVERED: { color: 'green', label: 'Đã giao' },
}
const shTag = (s) => <Tag color={SH_STATUS[s]?.color || 'default'}>{SH_STATUS[s]?.label || s}</Tag>

function useCarrierMap() {
  const carriers = useQuery({ queryKey: ['carriers'], queryFn: carriersApi.list })
  const map = useMemo(() => Object.fromEntries((carriers.data || []).map(c => [c.id, c])), [carriers.data])
  return { carriers, carrierMap: map }
}

export default function ShipmentsPage() {
  // /shipments | /shipments/new?soId=... | /shipments/<id>
  const { mode, id, openList, openCreate, openDetail } = useRecordView('/shipments')
  const [sp] = useSearchParams()
  const salesOrderId = sp.get('soId') || null
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={openList}>Danh sách</Button>
          )}
        </Space>

      </div>
      {/* BUG CŨ: SHList gọi setView() của cha => ReferenceError khi bấm "Tạo vận đơn". */}
      {mode === 'list' && <SHList onOpen={openDetail} onCreate={openCreate} />}
      {mode === 'create' && <CreateSH initialSoId={salesOrderId} onCreated={(sh) => openDetail(sh.id, { replace: true })} />}
      {mode === 'detail' && id && <SHDetail id={id} />}
    </div>
  )
}

function SHList({ onOpen, onCreate }) {
  const list = useQuery({ queryKey: ['sh-list'], queryFn: shipmentsApi.list })
  const { carrierMap } = useCarrierMap()
  const columns = [
    { title: 'Mã vận đơn', dataIndex: 'shipmentNumber', render: (v, r) => <RowLink onClick={() => onOpen(r.id)}>{v || r.id}</RowLink> },
    { title: 'Trạng thái', dataIndex: 'status', width: 130, render: shTag },
    { title: 'Đơn bán', dataIndex: 'salesOrderId', render: (v, r) => r.salesOrderNumber || v || '—' },
    { title: 'ĐVVC', dataIndex: 'carrierId', width: 150, render: (v) => carrierMap[v]?.name || v || '—' },
    { title: 'Mã tracking', dataIndex: 'trackingNumber', width: 150, render: (v) => v || '—' },
    { title: 'Xuất lúc', dataIndex: 'shippedAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{  marginBottom: 12,display: 'flex',justifyContent:'space-between' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>Vận đơn (Shipment)</Typography.Title>
        <Space> 
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching}>Làm mới</Button>
        <Can permission={P.OUTBOUND_SHIP}>
          <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo vận đơn</Button>
        </Can>
        </Space>
      </Space>
      <FitTable rowKey="id" loading={list.isLoading} dataSource={list.data || []} columns={columns}
        scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />
    </>
  )
}

function CreateSH({ initialSoId, onCreated }) {
  const { message } = AntdApp.useApp()
  const { carriers } = useCarrierMap()
  const [soId, setSoId] = useState(initialSoId || '')
  const [carrierId, setCarrierId] = useState()
  const createMut = useMutation({
    mutationFn: () => shipmentsApi.create(soId.trim(), carrierId),
    onSuccess: (sh) => { message.success('Đã tạo vận đơn'); onCreated(sh) },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Card title="Tạo vận đơn">
      <Form layout="vertical" style={{ maxWidth: 520 }}>
        <Form.Item label="Đơn bán (salesOrderId)" required>
          <Input placeholder="Dán ID đơn bán" value={soId} onChange={(e) => setSoId(e.target.value)} />
        </Form.Item>
        <Form.Item label="Đơn vị vận chuyển (tuỳ chọn)">
          <Select allowClear showSearch optionFilterProp="label" placeholder="Chọn ĐVVC"
            loading={carriers.isLoading} value={carrierId} onChange={setCarrierId}
            options={(carriers.data || []).map(c => ({ value: c.id, label: `${c.name} (${c.code})` }))} />
        </Form.Item>
        <Button type="primary" disabled={!soId.trim()} loading={createMut.isPending} onClick={() => createMut.mutate()}>Tạo</Button>
      </Form>
    </Card>
  )
}

function SHDetail({ id }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const qc = useQueryClient()
  const { carrierMap } = useCarrierMap()
  const [trackOpen, setTrackOpen] = useState(false)
  const [shipOpen, setShipOpen] = useState(false)

  const { data: sh, isLoading, isError, error } = useQuery({
    queryKey: ['sh', id], queryFn: () => shipmentsApi.get(id),
  })
  const refresh = (u) => { qc.setQueryData(['sh', id], u); qc.invalidateQueries({ queryKey: ['sh-list'] }) }

  const deliverMut = useMutation({
    mutationFn: () => shipmentsApi.deliver(id),
    onSuccess: (u) => { message.success('Đã đánh dấu giao thành công'); refresh(u) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy vận đơn')} /></Card>

  const s = sh.status
  const canPreShip = s === 'PACKED' || s === 'HANDED_OVER'

  return (
    <Card
      title={<Space>Vận đơn <b>{sh.shipmentNumber || sh.id}</b> {shTag(s)}</Space>}
      extra={
        <Space wrap>
          {canPreShip && (
            <Can permission={P.OUTBOUND_SHIP}>
              <Button icon={<TagOutlined />} onClick={() => setTrackOpen(true)}>Gán tracking</Button>
            </Can>
          )}
          {canPreShip && (
            <Can permission={P.OUTBOUND_SHIP}>
              <Button type="primary" icon={<CarOutlined />} onClick={() => setShipOpen(true)}>Xuất hàng</Button>
            </Can>
          )}
          {s === 'SHIPPING' && (
            <Can permission={P.OUTBOUND_SHIP}>
              <Button type="primary" icon={<CheckCircleOutlined />} loading={deliverMut.isPending}
                onClick={() => deliverMut.mutate()}>Đã giao</Button>
            </Can>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2 }} bordered
        items={[
          { key: 'so', label: 'Đơn bán', children: sh.salesOrderNumber || sh.salesOrderId || '—' },
          { key: 'to', label: 'Phiếu điều chuyển', children: sh.transferNumber || sh.transferOrderId || '—' },
          { key: 'ca', label: 'ĐVVC', children: carrierMap[sh.carrierId]?.name || sh.carrierId || '—' },
          { key: 'tr', label: 'Mã tracking', children: sh.trackingNumber || '—' },
          { key: 'sp', label: 'Xuất lúc', children: sh.shippedAt ? dayjs(sh.shippedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'dv', label: 'Giao lúc', children: sh.deliveredAt ? dayjs(sh.deliveredAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />

      <TrackingModal open={trackOpen} onClose={() => setTrackOpen(false)} shId={id}
        defaultCarrierId={sh.carrierId} onDone={refresh} />
      <ShipModal open={shipOpen} onClose={() => setShipOpen(false)} shId={id}
        actorUserId={user?.userId} onDone={refresh} />
    </Card>
  )
}

function TrackingModal({ open, onClose, shId, defaultCarrierId, onDone }) {
  const { message } = AntdApp.useApp()
  const { carriers } = useCarrierMap()
  const [form] = Form.useForm()
  const mut = useMutation({
    mutationFn: (v) => shipmentsApi.assignTracking(shId, v.carrierId, v.trackingNumber),
    onSuccess: (u) => { message.success('Đã gán vận đơn'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Gán đơn vị vận chuyển & mã tracking" open={open} onCancel={onClose}
      onOk={async () => mut.mutate(await form.validateFields())} confirmLoading={mut.isPending}
      afterOpenChange={(o) => { if (o) form.setFieldsValue({ carrierId: defaultCarrierId, trackingNumber: '' }) }}
      destroyOnClose>
      <Form form={form} layout="vertical">
        <Form.Item name="carrierId" label="Đơn vị vận chuyển" rules={[{ required: true, message: 'Chọn ĐVVC' }]}>
          <Select showSearch optionFilterProp="label" loading={carriers.isLoading} placeholder="Chọn ĐVVC"
            options={(carriers.data || []).map(c => ({ value: c.id, label: `${c.name} (${c.code})` }))} />
        </Form.Item>
        <Form.Item name="trackingNumber" label="Mã tracking" rules={[{ required: true, message: 'Nhập mã tracking' }]}>
          <Input />
        </Form.Item>
      </Form>
    </Modal>
  )
}

function ShipModal({ open, onClose, shId, actorUserId, onDone }) {
  const { message } = AntdApp.useApp()
  const warehouses = useQuery({ queryKey: ['warehouses', 'active'], queryFn: () => warehousesApi.list(false), enabled: open })
  const [warehouseId, setWarehouseId] = useState()
  const mut = useMutation({
    mutationFn: () => shipmentsApi.ship(shId, warehouseId, actorUserId),
    onSuccess: (u) => { message.success('Đã xuất hàng'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Xuất hàng (ship)" open={open} onCancel={onClose}
      onOk={() => warehouseId ? mut.mutate() : message.warning('Chọn kho xuất')}
      confirmLoading={mut.isPending} destroyOnClose>
      <Typography.Paragraph type="secondary">Chọn kho xuất để trừ tồn và chuyển vận đơn sang trạng thái đang giao.</Typography.Paragraph>
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} placeholder="Chọn kho"
        loading={warehouses.isLoading} value={warehouseId} onChange={setWarehouseId}
        options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))} />
    </Modal>
  )
}
