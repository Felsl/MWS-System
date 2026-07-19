import { useCallback, useEffect, useRef, useState } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useBinLabels } from '../../hooks/useBinLabels'
import {
  Card, Button, Input, List, Typography, Tag, Empty, Space, Progress, Modal,
  InputNumber, Form, Alert, App as AntdApp,
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
import { beepOk, beepError, beepDone } from '../../utils/feedback'

const PL_STATUS = {
  PENDING: { color: 'default', label: 'Chờ lấy' },
  PICKING: { color: 'gold', label: 'Đang lấy' },
  COMPLETED: { color: 'green', label: 'Hoàn thành' },
}

export default function PickingScanPage() {
  const [pickId, setPickId] = useState(null)
  return (
    <div style={{ maxWidth: 560, margin: '0 auto' }}>
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
        <Input size="large" placeholder="Quét / dán mã lệnh lấy hàng" value={manual}
          onChange={(e) => setManual(e.target.value)}
          onPressEnter={() => { if (manual.trim()) onPick(manual.trim()) }} />
        <Button size="large" type="primary" disabled={!manual.trim()}
          onClick={() => onPick(manual.trim())}>Mở</Button>
      </Space.Compact>
      <Card size="small" title="Lệnh chưa hoàn thành"
        extra={<Button size="small" icon={<ReloadOutlined />} aria-label="Làm mới danh sách"
          onClick={() => list.refetch()} loading={list.isFetching} />}>
        {items.length === 0
          ? <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Không có lệnh nào đang chờ" />
          : <List size="small" dataSource={items}
              renderItem={(p) => (
                <List.Item className="mws-line-clickable" onClick={() => onPick(p.id)}>
                  <List.Item.Meta
                    title={<b>{p.pickNumber || p.id}</b>}
                    description={<Tag color={PL_STATUS[p.status]?.color}>{PL_STATUS[p.status]?.label || p.status}</Tag>} />
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>SO: {p.soId}</Typography.Text>
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
  const [camOn, setCamOn] = useState(false)      // camera bật => KHÔNG focus ô nhập (bàn phím ảo che khung hình)
  const [flash, setFlash] = useState(null)       // 'ok' | 'err' — nháy nền thẻ
  const [activeId, setActiveId] = useState(null) // dòng người dùng tự chọn để quét

  const manualRef = useRef(null)

  const { data: pl, isLoading, isError, error } = useQuery({
    queryKey: ['pl', pickId], queryFn: () => pickingListsApi.get(pickId),
  })
  const { map: productMap } = useProducts()
  const { labelOf } = useBinLabels()

  const refresh = (u) => {
    if (u) qc.setQueryData(['pl', pickId], u)
    else qc.invalidateQueries({ queryKey: ['pl', pickId] })
    qc.invalidateQueries({ queryKey: ['pl-list'] })
  }

  // Trả con trỏ về ô nhập tay sau MỖI lần xử lý xong.
  // Đây là điểm chết người của bản cũ: máy quét cầm tay (USB/Bluetooth) chỉ là
  // một bàn phím — nó "gõ" mã rồi Enter. Ô nhập mất focus sau mỗi lần confirm
  // => người dùng phải bỏ máy quét xuống, cầm chuột click lại ô nhập, cho từng
  // dòng một. Ba dòng code dưới đây bỏ hẳn thao tác đó.
  const refocus = useCallback(() => {
    if (camOn) return
    requestAnimationFrame(() => manualRef.current?.focus({ cursor: 'end' }))
  }, [camOn])

  useEffect(() => { refocus() }, [refocus, pickId])

  const flashNow = (kind) => { setFlash(kind); setTimeout(() => setFlash(null), 650) }

  const confirmMut = useMutation({
    mutationFn: ({ detailId, scannedBatchNumber }) =>
      pickingListsApi.confirm(detailId, { scannedBatchNumber, confirmedBy: user?.userId }),
    onSuccess: (u) => {
      beepOk(); flashNow('ok')
      message.success('Đã xác nhận')
      refresh(u); setBusy(false); setActiveId(null); refocus()
    },
    onError: (e) => {
      beepError(); flashNow('err')
      message.error(getErrorMessage(e))
      setBusy(false); refocus()
    },
  })
  const completeMut = useMutation({
    mutationFn: () => pickingListsApi.complete(pickId),
    onSuccess: (u) => { beepDone(); message.success('Đã hoàn thành lệnh'); refresh(u) },
    onError: (e) => { beepError(); message.error(getErrorMessage(e)) },
  })

  if (isLoading) return <Card loading />
  if (isError) {
    return (
      <Card>
        <Empty description={getErrorMessage(error, 'Không tìm thấy lệnh')} />
        <Button block onClick={onBack}>Quay lại</Button>
      </Card>
    )
  }

  // Sắp theo toạ độ ô kệ: thủ kho đi theo tuyến đường trong kho, không đi theo
  // thứ tự BE trả về. Sắp xếp ở FE nên không ảnh hưởng logic BE.
  const details = [...(pl.details || [])].sort((a, b) =>
    String(labelOf(a.binLocationId)).localeCompare(String(labelOf(b.binLocationId)), 'vi', { numeric: true }))

  const done = details.filter(d => d.confirmed).length
  const pending = details.filter(d => !d.confirmed)
  // Mặc định là dòng chưa lấy đầu tiên, NHƯNG cho phép bấm chọn dòng khác:
  // thực tế hàng có thể hết ở ô này, người lấy nhảy sang ô kế rồi quay lại.
  const current = pending.find(d => d.id === activeId) || pending[0]
  const allDone = details.length > 0 && done === details.length

  const handleScan = (code) => {
    if (busy || !current || !code) return
    setBusy(true)
    confirmMut.mutate({ detailId: current.id, scannedBatchNumber: code })
  }

  return (
    <div>
      <Space style={{ justifyContent: 'space-between', width: '100%', marginBottom: 8 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={onBack}>Đổi lệnh</Button>
        <Tag>{pl.pickNumber || pl.id}</Tag>
      </Space>
      <Progress percent={details.length ? Math.round((done / details.length) * 100) : 0}
        format={() => `${done}/${details.length}`} />

      {allDone ? (
        <Card style={{ marginTop: 12, textAlign: 'center' }}>
          <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a' }} />
          <p>Đã quét xong tất cả dòng.</p>
          {pl.status !== 'COMPLETED' && (
            <Can permission={P.OUTBOUND_PICK}>
              <Button type="primary" size="large" block loading={completeMut.isPending}
                onClick={() => completeMut.mutate()}>Hoàn thành lệnh</Button>
            </Can>
          )}
        </Card>
      ) : current ? (
        <Card
          className={flash === 'ok' ? 'mws-flash-ok' : flash === 'err' ? 'mws-flash-err' : undefined}
          style={{ marginTop: 12 }} styles={{ body: { padding: 12 } }}>
          <div style={{ marginBottom: 8 }}>
            <div style={{ fontWeight: 600, fontSize: 17, lineHeight: 1.3 }}>
              {productMap[current.productId]?.name || current.productId}
            </div>
            {productMap[current.productId]?.sku && (
              <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                {productMap[current.productId].sku}
              </Typography.Text>
            )}
            {/* Ô kệ và số lượng là 2 thứ người lấy hàng nhìn nhiều nhất => cho to hẳn */}
            <Space size="large" style={{ marginTop: 6, fontSize: 14 }} wrap>
              <span>Ô kệ: <b style={{ fontSize: 18 }}>{labelOf(current.binLocationId)}</b></span>
              <span>SL: <b style={{ fontSize: 18 }}>{current.quantityToPick}</b></span>
              {current.batchId && <span style={{ fontSize: 12 }}>Lô: {current.batchId}</span>}
            </Space>
          </div>

          {/* Ô nhập tay đứng TRƯỚC camera: đa số kho dùng máy quét cầm tay
              (gõ phím + Enter), camera chỉ là phương án dự phòng. */}
          <ManualEntry ref={manualRef} disabled={busy} autoFocusOn={!camOn}
            onSubmit={handleScan} />

          <div style={{ marginTop: 10 }}>
            <BarcodeScanner onScan={handleScan} paused={busy} onActiveChange={setCamOn} />
          </div>

          <Button block danger size="large" icon={<WarningOutlined />} style={{ marginTop: 8 }}
            onClick={() => setShortLine(current)}>Báo thiếu dòng này</Button>

          {pending.length > 1 && (
            <Alert type="info" showIcon style={{ marginTop: 8 }} banner
              message="Không lấy được ở ô này? Bấm vào một dòng khác bên dưới để quét trước." />
          )}
        </Card>
      ) : null}

      <List style={{ marginTop: 12 }} size="small" bordered
        header={<b>Các dòng (sắp theo ô kệ)</b>}
        dataSource={details}
        renderItem={(d) => {
          const isCurrent = current && d.id === current.id
          return (
            <List.Item
              className={[
                !d.confirmed ? 'mws-line-clickable' : '',
                isCurrent ? 'mws-line-active' : '',
              ].filter(Boolean).join(' ')}
              onClick={() => { if (!d.confirmed) { setActiveId(d.id); refocus() } }}>
              <List.Item.Meta
                title={productMap[d.productId]?.name || d.productId}
                description={`Ô ${labelOf(d.binLocationId)} · SL ${d.quantityToPick}`} />
              {d.confirmed
                ? <Tag color="green">Đã lấy</Tag>
                : isCurrent ? <Tag color="blue">Đang quét</Tag> : <Tag>Chưa</Tag>}
            </List.Item>
          )
        }} />

      <ShortModal line={shortLine} onClose={() => { setShortLine(null); refocus() }}
        confirmedBy={user?.userId}
        onDone={(u) => { refresh(u); setActiveId(null) }} />
    </div>
  )
}

/**
 * Ô nhập/quét mã lô. Nhận ref để trang cha trả focus về sau mỗi lần xác nhận.
 * autoFocusOn=false khi camera đang bật (tránh bàn phím ảo che khung hình).
 */
function ManualEntry({ ref, onSubmit, disabled, autoFocusOn }) {
  const [v, setV] = useState('')

  const fire = () => {
    const code = v.trim()
    if (!code) return
    setV('')          // xoá ngay để mã kế tiếp không bị nối đuôi
    onSubmit(code)
  }

  return (
    <Space.Compact style={{ width: '100%' }}>
      <Input
        ref={ref}
        size="large"
        autoFocus={autoFocusOn}
        placeholder="Quét hoặc nhập mã lô rồi Enter"
        value={v}
        disabled={disabled}
        onChange={(e) => setV(e.target.value)}
        onPressEnter={fire}
      />
      <Button size="large" type="primary" disabled={disabled || !v.trim()} onClick={fire}>
        Xác nhận
      </Button>
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
    onSuccess: (u) => { beepOk(); message.success('Đã ghi nhận thiếu'); onDone(u); onClose() },
    onError: (e) => { beepError(); message.error(getErrorMessage(e)) },
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
