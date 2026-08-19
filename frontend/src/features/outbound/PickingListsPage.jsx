import FitTable from '../../components/FitTable'
import RowLink from '../../components/RowLink'
import { useState } from 'react'
import { useProducts } from '../../hooks/useProducts'
import { useBinLabels } from '../../hooks/useBinLabels'
import { Link, useSearchParams } from 'react-router-dom'
import { useRecordView } from '../../hooks/useRecordView'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Card, Button, Input, Select, Table, Space, Typography, Tag,
  Descriptions, Empty, Modal, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, ReloadOutlined, ArrowLeftOutlined,
  UserAddOutlined, CheckCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import Can from '../../components/Can'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { pickingListsApi } from '../../api/pickingLists.api'
import { useSalesOrderNumbers } from '../../hooks/useSalesOrderLookup'
import { sortPickingLists } from './pickingSort'
import { usersApi } from '../../api/users.api'

const PL_STATUS = {
  PENDING: { color: 'default', label: 'Chờ lấy' },
  PICKING: { color: 'gold', label: 'Đang lấy' },
  COMPLETED: { color: 'green', label: 'Hoàn thành' },
}
const plTag = (s) => <Tag color={PL_STATUS[s]?.color || 'default'}>{PL_STATUS[s]?.label || s}</Tag>

export default function PickingListsPage() {
  // Chế độ xem nằm ở URL: /picking-lists | /picking-lists/new | /picking-lists/<id>
  // (thay location.state?.openId — state chết sau F5).
  const { mode, id, openList, openCreate, openDetail } = useRecordView('/picking-lists')

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          {mode !== 'list' && (
            <Button icon={<ArrowLeftOutlined />} onClick={openList}>Danh sách</Button>
          )}
        </Space>

      </div>

      {mode === 'list' && <PLList onOpen={openDetail} onCreate={openCreate} />}
      {mode === 'create' && <CreatePL onCreated={(pl) => openDetail(pl.id, { replace: true })} />}
      {mode === 'detail' && id && <PLDetail id={id} />}
    </div>
  )
}

function PLList({ onOpen, onCreate }) {
  const list = useQuery({ queryKey: ['pl-list'], queryFn: pickingListsApi.list })
  const { numberOf } = useSalesOrderNumbers()
  // Ưu tiên việc đang làm: Đang lấy -> Chờ lấy -> Hoàn thành.
  const rows = sortPickingLists(list.data || [])
  const columns = [
    { title: 'Mã lệnh', dataIndex: 'pickNumber', render: (_, r) => <RowLink onClick={() => onOpen(r.id)}>{r.pickNumber || r.id}</RowLink> },
    { title: 'Trạng thái', dataIndex: 'status', width: 130, render: plTag },
    // Nguồn của lệnh: đơn bán (SO) HOẶC phiếu điều chuyển — dùng chung một danh sách.
    { title: 'Nguồn', key: 'source', render: (_, r) => r.soId
        ? <Link to={`/sales-orders/${r.soId}`}>{r.soNumber || numberOf(r.soId)}</Link>
        : <Link to={`/purchase-orders/${r.toId}`}>{r.transferNumber || numberOf(r.transferOrderId)}</Link>
       },
    { title: 'Người lấy', dataIndex: 'assignedToName', width: 140, render: (v, r) => v || r.assignedTo || '—' },
    { title: 'Bắt đầu', dataIndex: 'startedAt', width: 150, render: (v) => v ? dayjs(v).format('DD/MM/YYYY HH:mm') : '—' },
  ]
  return (
    <>
      <Space style={{  marginBottom: 12,display: 'flex',justifyContent:'space-between'  }}>
        <Typography.Title level={4} style={{ margin: 0 }}>Lệnh lấy hàng (Picking)</Typography.Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching}>Làm mới</Button>
          <Can permission={P.OUTBOUND_PICK}>
            <Button type="primary" icon={<PlusOutlined />} onClick={onCreate}>Tạo lệnh</Button>
          </Can>
        </Space>
        
      </Space>
      <FitTable rowKey="id" loading={list.isLoading} dataSource={rows} columns={columns}
        scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />
    </>
  )
}

function CreatePL({ onCreated }) {
  const { message } = AntdApp.useApp()
  // ?soId=... khi được điều hướng từ trang Đơn bán hàng (thay location.state).
  const [sp] = useSearchParams()
  const [soId, setSoId] = useState(sp.get('soId') || '')
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
  const qc = useQueryClient()
  const [assignOpen, setAssignOpen] = useState(false)

  const { data: pl, isLoading, isError, error } = useQuery({
    queryKey: ['pl', id], queryFn: () => pickingListsApi.get(id),
  })
  const { map: productMap } = useProducts()
  const { labelOf } = useBinLabels()
  const { numberOf } = useSalesOrderNumbers()

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
    { title: 'Ô kệ', dataIndex: 'binLocationId', width: 130, render: (v, r) => r.binLocationLabel || labelOf(v) },
    { title: 'Lô cần lấy', dataIndex: 'batchNumber', width: 130, render: (v, r) => v || r.batchId || '—' },
    { title: 'Lô thực lấy', dataIndex: 'actualBatchId', width: 130, render: (v) => v || '—' },
    { title: 'SL cần', dataIndex: 'quantityToPick', width: 90, align: 'right' },
    { title: 'SL lấy', dataIndex: 'quantityPicked', width: 90, align: 'right' },
    {
      title: 'Xác nhận', dataIndex: 'confirmed', width: 110,
      render: (c) => c ? <Tag color="green">Đã lấy</Tag> : <Tag>Chưa</Tag>,
    },
  ]

  return (
    <Card
      title={<Space>Lệnh lấy hàng <b>{pl.pickNumber || pl.id}</b> {plTag(pl.status)}</Space>}
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
          {
            key: 'so', label: 'Đơn bán',
            children: pl.soId ? <Link to={`/sales-orders/${pl.soId}`}>{pl.soNumber || numberOf(pl.soId)}</Link> : '—',
          },
          { key: 'as', label: 'Người lấy', children: pl.assignedToName || pl.assignedTo || '— (chưa gán)' },
          { key: 'st', label: 'Bắt đầu', children: pl.startedAt ? dayjs(pl.startedAt).format('DD/MM/YYYY HH:mm') : '—' },
          { key: 'cp', label: 'Hoàn thành', children: pl.completedAt ? dayjs(pl.completedAt).format('DD/MM/YYYY HH:mm') : '—' },
        ]} />
      <Table style={{ marginTop: 16 }} rowKey="id" size="small" pagination={false}
        dataSource={details} columns={columns} scroll={{ x: 'max-content' }} />
      {!allConfirmed && pl.status !== 'COMPLETED' && (
        <Typography.Text type="secondary">* Xác nhận (hoặc báo thiếu) tất cả dòng rồi mới Hoàn thành được.</Typography.Text>
      )}

      <AssignModal open={assignOpen} onClose={() => setAssignOpen(false)} pickId={id} onDone={refresh} />
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
