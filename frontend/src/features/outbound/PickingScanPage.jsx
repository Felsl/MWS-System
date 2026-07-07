import { useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, List, Typography, Tag, Empty, Space, Progress, Modal,
  InputNumber, Form, App as AntdApp,
} from 'antd'
import {
  ArrowLeftOutlined, ReloadOutlined, CheckCircleOutlined, WarningOutlined, ScanOutlined,
} from '@ant-design/icons'
import Can from '../../components/Can'
import BarcodeScanner from '../../components/BarcodeScanner'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { pickingListsApi } from '../../api/pickingLists.api'
import { productsApi } from '../../api/products.api'

const PL_STATUS = {
  PENDING: { color: 'default', label: 'Chờ lấy' },
  PICKING: { color: 'gold', label: 'Đang lấy' },
  COMPLETED: { color: 'green', label: 'Hoàn thành' },
}

export default function PickingScanPage() {
  const [pickId, setPickId] = useState(null)
  return (
    <div style={{ maxWidth: 520, margin: '0 auto' }}>
      <Typography.Title level={4} style={{ margin: '0 0 12px' }}>
        <ScanOutlined /> Quét lấy hàng
      </Typography.Title>
      {pickId
        ? <ScanRunner pickId={pickId} onBack={() => setPickId(null)} />
        : <PickPicker onPick={setPickId} />}
    </div>
  )
}

function PickPicker({ onPick }) {
  const list = useQuery({ queryKey: ['pl-list'], queryFn: pickingListsApi.list })
  const [manual, setManual] = useState('')
  const items = (list.data || []).filter(p => p.status !== 'COMPLETED')
  return (
    <>
      <Space.Compact style={{ width: '100%', marginBottom: 12 }}>
        <Input placeholder="Dán ID lệnh lấy hàng" value={manual} onChange={(e) => setManual(e.target.value)} />
        <Button type="primary" disabled={!manual.trim()} onClick={() => onPick(manual.trim())}>Mở</Button>
      </Space.Compact>
      <Card size="small" title="Lệnh chưa hoàn thành"
        extra={<Button size="small" icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />}>
        {items.length === 0
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không có lệnh" />
          : <List size="small" dataSource={items}
              renderItem={(p) => (
                <List.Item style={{ cursor: 'pointer' }} onClick={() => onPick(p.id)}>
                  <List.Item.Meta title={p.id}
                    description={<Tag color={PL_STATUS[p.status]?.color}>{PL_STATUS[p.status]?.label || p.status}</Tag>} />
                  <span>SO: {p.soId}</span>
                </List.Item>
              )} />}
      </Card>
    </>
  )
}

function ScanRunner({ pickId, onBack }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const qc = useQueryClient()
  const [shortLine, setShortLine] = useState(null)
  const [busy, setBusy] = useState(false)

  const { data: pl, isLoading, isError, error } = useQuery({
    queryKey: ['pl', pickId], queryFn: () => pickingListsApi.get(pickId),
  })
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const productMap = useMemo(
    () => Object.fromEntries((products.data?.content || []).map(p => [p.id, p])), [products.data])

  const refresh = (u) => { if (u) qc.setQueryData(['pl', pickId], u); else qc.invalidateQueries({ queryKey: ['pl', pickId] }); qc.invalidateQueries({ queryKey: ['pl-list'] }) }

  const confirmMut = useMutation({
    mutationFn: ({ detailId, scannedBatchNumber }) =>
      pickingListsApi.confirm(detailId, { scannedBatchNumber, confirmedBy: user?.userId }),
    onSuccess: (u) => { message.success('Đã xác nhận'); refresh(u); setBusy(false) },
    onError: (e) => { message.error(getErrorMessage(e)); setBusy(false) },
  })
  const completeMut = useMutation({
    mutationFn: () => pickingListsApi.complete(pickId),
    onSuccess: (u) => { message.success('Đã hoàn thành lệnh'); refresh(u) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy lệnh')} /><Button onClick={onBack}>Quay lại</Button></Card>

  const details = pl.details || []
  const done = details.filter(d => d.confirmed).length
  const current = details.find(d => !d.confirmed) // dòng cần xử lý tiếp
  const allDone = details.length > 0 && done === details.length

  const handleScan = (code) => {
    if (busy || !current) return
    setBusy(true)
    confirmMut.mutate({ detailId: current.id, scannedBatchNumber: code })
  }

  return (
    <div>
      <Space style={{ justifyContent: 'space-between', width: '100%', marginBottom: 8 }}>
        <Button size="small" icon={<ArrowLeftOutlined />} onClick={onBack}>Đổi lệnh</Button>
        <Tag>{pl.id}</Tag>
      </Space>
      <Progress percent={details.length ? Math.round((done / details.length) * 100) : 0}
        format={() => `${done}/${details.length}`} />

      {allDone ? (
        <Card style={{ marginTop: 12, textAlign: 'center' }}>
          <CheckCircleOutlined style={{ fontSize: 40, color: '#52c41a' }} />
          <p>Đã quét xong tất cả dòng.</p>
          {pl.status !== 'COMPLETED' && (
            <Can permission={P.OUTBOUND_PICK}>
              <Button type="primary" loading={completeMut.isPending} onClick={() => completeMut.mutate()}>Hoàn thành lệnh</Button>
            </Can>
          )}
        </Card>
      ) : current ? (
        <Card style={{ marginTop: 12 }} styles={{ body: { padding: 12 } }}>
          <div style={{ marginBottom: 8 }}>
            <div style={{ fontWeight: 600, fontSize: 16 }}>{productMap[current.productId]?.name || current.productId}</div>
            <Space size="large" style={{ fontSize: 13, color: 'rgba(0,0,0,.65)' }}>
              <span>Ô kệ: <b>{current.binLocationId}</b></span>
              <span>SL: <b>{current.quantityToPick}</b></span>
              {current.batchId && <span>Lô: {current.batchId}</span>}
            </Space>
          </div>

          <BarcodeScanner onScan={handleScan} paused={busy} />

          <div style={{ marginTop: 10 }}>
            <ManualEntry disabled={busy} onSubmit={(code) => handleScan(code)} />
          </div>
          <Button block danger icon={<WarningOutlined />} style={{ marginTop: 8 }}
            onClick={() => setShortLine(current)}>Báo thiếu dòng này</Button>
        </Card>
      ) : null}

      <List style={{ marginTop: 12 }} size="small" header={<b>Các dòng</b>} bordered
        dataSource={details}
        renderItem={(d) => (
          <List.Item>
            <List.Item.Meta
              title={productMap[d.productId]?.name || d.productId}
              description={`Ô ${d.binLocationId} · SL ${d.quantityToPick}`} />
            {d.confirmed ? <Tag color="green">Đã lấy</Tag> : <Tag>Chưa</Tag>}
          </List.Item>
        )} />

      <ShortModal line={shortLine} onClose={() => setShortLine(null)}
        confirmedBy={user?.userId} onDone={refresh} />
    </div>
  )
}

function ManualEntry({ onSubmit, disabled }) {
  const [v, setV] = useState('')
  return (
    <Space.Compact style={{ width: '100%' }}>
      <Input placeholder="Hoặc nhập mã lô tay" value={v} disabled={disabled}
        onChange={(e) => setV(e.target.value)}
        onPressEnter={() => { if (v.trim()) { onSubmit(v.trim()); setV('') } }} />
      <Button disabled={disabled || !v.trim()} onClick={() => { onSubmit(v.trim()); setV('') }}>Xác nhận</Button>
    </Space.Compact>
  )
}

function ShortModal({ line, onClose, confirmedBy, onDone }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const mut = useMutation({
    mutationFn: (v) => pickingListsApi.reportShort(line.id, {
      scannedBatchNumber: v.scannedBatchNumber, actualQty: v.actualQty, reason: v.reason, confirmedBy,
    }),
    onSuccess: (u) => { message.success('Đã ghi nhận thiếu'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Báo thiếu hàng" open={!!line} onCancel={onClose}
      onOk={async () => mut.mutate(await form.validateFields())} confirmLoading={mut.isPending}
      afterOpenChange={(o) => { if (o) form.setFieldsValue({ scannedBatchNumber: '', actualQty: line?.quantityToPick, reason: '' }) }}
      destroyOnClose>
      <Form form={form} layout="vertical">
        <Form.Item name="scannedBatchNumber" label="Số lô đã quét" rules={[{ required: true, message: 'Nhập số lô' }]}>
          <Input autoFocus />
        </Form.Item>
        <Form.Item name="actualQty" label="Số lượng thực lấy" rules={[{ required: true, message: 'Nhập SL' }]}>
          <InputNumber min={0} max={line?.quantityToPick} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="reason" label="Lý do" rules={[{ required: true, message: 'Nhập lý do' }]}>
          <Input.TextArea rows={2} />
        </Form.Item>
      </Form>
    </Modal>
  )
}
