import { useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Form, Select, InputNumber, Table, Space, Typography, Tag,
  Descriptions, Empty, Modal, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, ReloadOutlined, ArrowLeftOutlined, CheckOutlined,
  WarningOutlined, UserAddOutlined, CheckCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { pickingListsApi } from '../../api/pickingLists.api'
import { productsApi } from '../../api/products.api'
import { usersApi } from '../../api/users.api'

const PL_STATUS = {
  PENDING: { color: 'default', label: 'Chờ lấy' },
  PICKING: { color: 'gold', label: 'Đang lấy' },
  COMPLETED: { color: 'green', label: 'Hoàn thành' },
}
const plTag = (s) => <Tag color={PL_STATUS[s]?.color || 'default'}>{PL_STATUS[s]?.label || s}</Tag>

export default function PickingListsPage() {
  const location = useLocation()
  const openId = location.state?.openId || null
  const [view, setView] = useState(openId ? { mode: 'detail', id: openId } : { mode: 'list', id: null })
  const openDetail = (id) => setView({ mode: 'detail', id })

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {view.mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={() => setView({ mode: 'list', id: null })}>Danh sách</Button>
          )}
          <Typography.Title level={4} style={{ margin: 0 }}>Lệnh lấy hàng (Picking)</Typography.Title>
        </Space>
        <Can permission={P.OUTBOUND_PICK}>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setView({ mode: 'create', id: null })}>Tạo lệnh</Button>
        </Can>
      </div>
      {view.mode === 'list' && <PLList onOpen={openDetail} />}
      {view.mode === 'create' && <CreatePL onCreated={(pl) => openDetail(pl.id)} />}
      {view.mode === 'detail' && view.id && <PLDetail id={view.id} />}
    </div>
  )
}

function PLList({ onOpen }) {
  const list = useQuery({ queryKey: ['pl-list'], queryFn: pickingListsApi.list })
  const columns = [
    { title: 'Mã lệnh', dataIndex: 'id', render: (v) => <a onClick={() => onOpen(v)}>{v}</a> },
    { title: 'Trạng thái', dataIndex: 'status', width: 130, render: plTag },
    { title: 'Đơn bán (SO)', dataIndex: 'soId' },
    { title: 'Người lấy', dataIndex: 'assignedTo', width: 140, render: (v) => v || '—' },
    { title: 'Bắt đầu', dataIndex: 'startedAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{ marginBottom: 12 }}>
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching}>Làm mới</Button>
      </Space>
      <Table rowKey="id" loading={list.isLoading} dataSource={list.data || []} columns={columns}
        scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />
    </>
  )
}

function CreatePL({ onCreated }) {
  const { message } = AntdApp.useApp()
  const location = useLocation()
  const [soId, setSoId] = useState(location.state?.soId || '')
  const createMut = useMutation({
    mutationFn: () => pickingListsApi.create(soId.trim()),
    onSuccess: (pl) => { message.success('Đã tạo lệnh lấy hàng'); onCreated(pl) },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Card title="Tạo lệnh lấy hàng">
      <Typography.Paragraph type="secondary">Lệnh được sinh theo FEFO từ một đơn bán đã phân bổ (ALLOCATED).</Typography.Paragraph>
      <Space.Compact style={{ width: '100%', maxWidth: 520 }}>
        <Input placeholder="Dán ID đơn bán (soId)" value={soId} onChange={(e) => setSoId(e.target.value)} />
        <Button type="primary" disabled={!soId.trim()} loading={createMut.isPending} onClick={() => createMut.mutate()}>Tạo</Button>
      </Space.Compact>
    </Card>
  )
}

function PLDetail({ id }) {
  const { message } = AntdApp.useApp()
  const { user } = useAuth()
  const qc = useQueryClient()
  const [assignOpen, setAssignOpen] = useState(false)
  const [pickModal, setPickModal] = useState(null) // { mode:'confirm'|'short', detail }

  const { data: pl, isLoading, isError, error } = useQuery({
    queryKey: ['pl', id], queryFn: () => pickingListsApi.get(id),
  })
  const products = useQuery({ queryKey: ['products', 'all'], queryFn: () => productsApi.list({ size: 500 }) })
  const productMap = useMemo(
    () => Object.fromEntries((products.data?.content || []).map(p => [p.id, p])), [products.data])

  const refresh = (u) => { qc.setQueryData(['pl', id], u); qc.invalidateQueries({ queryKey: ['pl-list'] }) }

  const completeMut = useMutation({
    mutationFn: () => pickingListsApi.complete(id),
    onSuccess: (u) => { message.success('Đã hoàn thành lấy hàng'); refresh(u) },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  if (isLoading) return <Card loading />
  if (isError) return <Card><Empty description={getErrorMessage(error, 'Không tìm thấy lệnh lấy hàng')} /></Card>

  const details = pl.details || []
  const allConfirmed = details.length > 0 && details.every(d => d.confirmed)

  const columns = [
    { title: 'Sản phẩm', dataIndex: 'productId', render: (pid) => productMap[pid]?.name || pid },
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130 },
    { title: 'Lô cần', dataIndex: 'batchId', width: 130, render: (v) => v || '—' },
    { title: 'Lô thực', dataIndex: 'actualBatchId', width: 130, render: (v) => v || '—' },
    { title: 'SL cần', dataIndex: 'quantityToPick', width: 90, align: 'right' },
    { title: 'SL lấy', dataIndex: 'quantityPicked', width: 90, align: 'right' },
    {
      title: 'Xác nhận', dataIndex: 'confirmed', width: 110,
      render: (c) => c ? <Tag color="green">Đã lấy</Tag> : <Tag>Chưa</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 190, fixed: 'right',
      render: (_, d) => d.confirmed ? '—' : (
        <Can permission={P.OUTBOUND_PICK}>
          <Space>
            <Button size="small" type="primary" icon={<CheckOutlined />}
              onClick={() => setPickModal({ mode: 'confirm', detail: d })}>Xác nhận</Button>
            <Button size="small" danger icon={<WarningOutlined />}
              onClick={() => setPickModal({ mode: 'short', detail: d })}>Thiếu</Button>
          </Space>
        </Can>
      ),
    },
  ]

  return (
    <Card
      title={<Space>Lệnh lấy hàng <b>{pl.id}</b> {plTag(pl.status)}</Space>}
      extra={
        <Space wrap>
          <Can permission={P.OUTBOUND_PICK}>
            <Button icon={<UserAddOutlined />} onClick={() => setAssignOpen(true)}>Gán người lấy</Button>
          </Can>
          {pl.status !== 'COMPLETED' && (
            <Can permission={P.OUTBOUND_PICK}>
              <Button type="primary" icon={<CheckCircleOutlined />} loading={completeMut.isPending}
                disabled={!allConfirmed}
                onClick={() => completeMut.mutate()}>Hoàn thành</Button>
            </Can>
          )}
        </Space>
      }
    >
      <Descriptions size="small" column={{ xs: 1, sm: 2 }} bordered
        items={[
          { key: 'so', label: 'Đơn bán', children: pl.soId },
          { key: 'as', label: 'Người lấy', children: pl.assignedTo || '— (chưa gán)' },
          { key: 'st', label: 'Bắt đầu', children: pl.startedAt ? dayjs(pl.startedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'cp', label: 'Hoàn thành', children: pl.completedAt ? dayjs(pl.completedAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={details} columns={columns} scroll={{ x: 'max-content' }} />
      {!allConfirmed && pl.status !== 'COMPLETED' && (
        <Typography.Text type="secondary">* Xác nhận (hoặc báo thiếu) tất cả dòng rồi mới Hoàn thành được.</Typography.Text>
      )}

      <AssignModal open={assignOpen} onClose={() => setAssignOpen(false)} pickId={id} onDone={refresh} />
      <PickActionModal state={pickModal} onClose={() => setPickModal(null)}
        confirmedBy={user?.userId} onDone={refresh} />
    </Card>
  )
}

function AssignModal({ open, onClose, pickId, onDone }) {
  const { message } = AntdApp.useApp()
  const [userId, setUserId] = useState()
  const users = useQuery({
    queryKey: ['users', 'pickers'],
    queryFn: () => usersApi.list({ permission: 'OUTBOUND_PICK' }),
    enabled: open,
  })
  const assignMut = useMutation({
    mutationFn: () => pickingListsApi.assign(pickId, userId),
    onSuccess: (u) => { message.success('Đã gán người lấy'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  return (
    <Modal title="Gán người lấy hàng" open={open} onCancel={onClose}
      onOk={() => userId ? assignMut.mutate() : message.warning('Chọn người lấy')}
      confirmLoading={assignMut.isPending} destroyOnClose>
      <Select showSearch optionFilterProp="label" style={{ width: '100%' }} placeholder="Chọn nhân viên"
        loading={users.isLoading} value={userId} onChange={setUserId}
        options={(users.data || []).map(u => ({ value: u.id, label: `${u.fullName || u.username} (${u.username})` }))} />
    </Modal>
  )
}

function PickActionModal({ state, onClose, confirmedBy, onDone }) {
  const { message } = AntdApp.useApp()
  const [form] = Form.useForm()
  const mode = state?.mode
  const detail = state?.detail

  const confirmMut = useMutation({
    mutationFn: (body) => pickingListsApi.confirm(detail.id, body),
    onSuccess: (u) => { message.success('Đã xác nhận lấy'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const shortMut = useMutation({
    mutationFn: (body) => pickingListsApi.reportShort(detail.id, body),
    onSuccess: (u) => { message.success('Đã ghi nhận thiếu hàng'); onDone(u); onClose() },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const submit = async () => {
    const v = await form.validateFields()
    if (mode === 'confirm') {
      confirmMut.mutate({ scannedBatchNumber: v.scannedBatchNumber, confirmedBy })
    } else {
      shortMut.mutate({ scannedBatchNumber: v.scannedBatchNumber, actualQty: v.actualQty, reason: v.reason, confirmedBy })
    }
  }

  return (
    <Modal
      title={mode === 'confirm' ? 'Xác nhận lấy hàng' : 'Báo thiếu hàng'}
      open={!!state} onCancel={onClose} onOk={submit}
      confirmLoading={confirmMut.isPending || shortMut.isPending}
      afterOpenChange={(o) => { if (o) form.setFieldsValue({ scannedBatchNumber: '', actualQty: detail?.quantityToPick, reason: '' }) }}
      destroyOnClose
    >
      <Form form={form} layout="vertical">
        <Form.Item name="scannedBatchNumber" label="Số lô đã quét"
          rules={[{ required: true, message: 'Nhập/quét số lô' }]}>
          <Input placeholder="Quét mã lô thực tế" autoFocus />
        </Form.Item>
        {mode === 'short' && (
          <>
            <Form.Item name="actualQty" label="Số lượng thực lấy"
              rules={[{ required: true, message: 'Nhập SL thực' }]}>
              <InputNumber min={0} max={detail?.quantityToPick} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="reason" label="Lý do thiếu"
              rules={[{ required: true, message: 'Nhập lý do' }]}>
              <Input.TextArea rows={2} />
            </Form.Item>
          </>
        )}
      </Form>
    </Modal>
  )
}
