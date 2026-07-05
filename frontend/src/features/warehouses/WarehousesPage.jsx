import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Table, Button, Space, Modal, Form, Input, Popconfirm, Typography, Tag, Switch,
  Drawer, Divider, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, ClusterOutlined,
} from '@ant-design/icons'
import Can from '../../components/Can'
import { warehousesApi } from '../../api/warehouses.api'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { useAuth } from '../../auth/AuthContext'

export default function WarehousesPage() {
  const { message } = AntdApp.useApp()
  const { hasPermission } = useAuth()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [adminView, setAdminView] = useState(false)
  const [binWh, setBinWh] = useState(null)

  const list = useQuery({
    queryKey: ['warehouses', adminView ? 'admin' : 'active'],
    queryFn: () => warehousesApi.list(adminView),
  })
  const invalidate = () => qc.invalidateQueries({ queryKey: ['warehouses'] })

  const createMut = useMutation({
    mutationFn: warehousesApi.create,
    onSuccess: () => { message.success('Đã tạo kho'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => warehousesApi.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const removeMut = useMutation({
    mutationFn: warehousesApi.remove,
    onSuccess: () => { message.success('Đã xoá (mềm)'); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (row) => { setEditing(row); form.setFieldsValue(row); setOpen(true) }
  const submit = async () => {
    const body = await form.validateFields()
    if (editing) updateMut.mutate({ id: editing.id, body })
    else createMut.mutate(body)
  }

  const columns = [
    { title: 'Mã', dataIndex: 'code', width: 140 },
    { title: 'Tên', dataIndex: 'name' },
    { title: 'Địa chỉ', dataIndex: 'address' },
    {
      title: 'Trạng thái', dataIndex: 'status', width: 120,
      render: (s) => <Tag color={s === 'ACTIVE' ? 'green' : 'red'}>{s}</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 170, fixed: 'right',
      render: (_, row) => (
        <Space>
          <Button size="small" icon={<ClusterOutlined />} title="Ô kệ"
            onClick={() => setBinWh(row)} />
          <Can permission={P.WAREHOUSE_UPDATE}>
            <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(row)} />
          </Can>
          <Can permission={P.WAREHOUSE_DELETE}>
            <Popconfirm title="Đóng (xoá mềm) kho này?" okText="Xoá" cancelText="Huỷ"
              onConfirm={() => removeMut.mutate(row.id)}>
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Can>
        </Space>
      ),
    },
  ]

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>Kho & ô kệ</Typography.Title>
        <Space>
          {hasPermission(P.WAREHOUSE_DELETE) && (
            <Space size={6}>
              <span>Hiện kho đã đóng</span>
              <Switch checked={adminView} onChange={setAdminView} />
            </Space>
          )}
          <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.WAREHOUSE_CREATE}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm kho</Button>
          </Can>
        </Space>
      </div>

      <Table rowKey="id" loading={list.isLoading} dataSource={list.data || []}
        columns={columns} scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />

      <Modal title={editing ? 'Sửa kho' : 'Thêm kho'} open={open}
        onOk={submit} onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="Mã kho"
            rules={[{ required: true, message: 'Nhập mã' }]}>
            <Input disabled={!!editing} placeholder="VD: WH-Q7" />
          </Form.Item>
          <Form.Item name="name" label="Tên kho"
            rules={[{ required: true, message: 'Nhập tên' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ"
            rules={[{ required: true, message: 'Nhập địa chỉ' }]}>
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <BinLocationsDrawer warehouse={binWh} onClose={() => setBinWh(null)} />
    </>
  )
}

function BinLocationsDrawer({ warehouse, onClose }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const open = !!warehouse
  const [genForm] = Form.useForm()
  const [showGen, setShowGen] = useState(false)

  const bins = useQuery({
    queryKey: ['bins', warehouse?.id],
    queryFn: () => warehousesApi.listBins(warehouse.id),
    enabled: open,
  })

  const genMut = useMutation({
    mutationFn: (zones) => warehousesApi.bulkGenerateBins(warehouse.id, zones),
    onSuccess: (res) => {
      message.success(`Đã sinh ${res?.totalGenerated ?? ''} ô kệ`.trim())
      setShowGen(false)
      qc.invalidateQueries({ queryKey: ['bins', warehouse.id] })
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const delMut = useMutation({
    mutationFn: (binId) => warehousesApi.deleteBin(warehouse.id, binId),
    onSuccess: () => {
      message.success('Đã xoá ô kệ')
      qc.invalidateQueries({ queryKey: ['bins', warehouse.id] })
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  // Chuyển form đơn giản -> cấu trúc zones[]{zone, aisles[]{aisle, racks[]{rack, bins[]}}}
  const submitGen = async () => {
    const v = await genForm.validateFields()
    const aisles = expandRange(v.aisleFrom, v.aisleTo)
    const racks = expandRange(v.rackFrom, v.rackTo)
    const bins = verticalBinOrder(v.binFrom, v.binTo)
    const zones = [{
      zone: v.zone,
      aisles: aisles.map(a => ({
        aisle: String(a),
        racks: racks.map(r => ({ rack: String(r), bins })),
      })),
    }]
    genMut.mutate(zones)
  }

  const binColumns = [
    { title: 'Mã ô kệ', dataIndex: 'coordinateLabel' },
    { title: 'Zone', dataIndex: 'zone', width: 80 },
    { title: 'Aisle', dataIndex: 'aisle', width: 80 },
    { title: 'Rack', dataIndex: 'rack', width: 80 },
    { title: 'Bin', dataIndex: 'bin', width: 80 },
    {
      title: '', key: '_a', width: 60,
      render: (_, row) => (
        <Can permission={P.WAREHOUSE_DELETE}>
          <Popconfirm title="Xoá ô kệ?" onConfirm={() => delMut.mutate(row.id)}
            okText="Xoá" cancelText="Huỷ">
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Can>
      ),
    },
  ]

  return (
    <Drawer
      title={`Ô kệ · ${warehouse?.name || ''}`}
      width={720}
      open={open}
      onClose={onClose}
      extra={
        <Can permission={P.WAREHOUSE_CREATE}>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setShowGen(v => !v)}>
            Sinh ô kệ hàng loạt
          </Button>
        </Can>
      }
    >
      {showGen && (
        <>
          <Typography.Text type="secondary">
            Nhập cấu trúc: 1 Zone, dải Aisle → Rack → Bin. Hệ thống sinh tổ hợp ô kệ.
          </Typography.Text>
          <Form form={genForm} layout="vertical" style={{ marginTop: 12 }}
            initialValues={{ aisleFrom: 1, aisleTo: 5, rackFrom: 1, rackTo: 4, binFrom: 1, binTo: 5 }}>
            <Form.Item name="zone" label="Zone (khu vực)"
              rules={[{ required: true, message: 'Nhập zone' }]}>
              <Input placeholder="VD: A" style={{ maxWidth: 160 }} />
            </Form.Item>
            <Space wrap>
              <Form.Item name="aisleFrom" label="Aisle từ"><Input type="number" /></Form.Item>
              <Form.Item name="aisleTo" label="đến"><Input type="number" /></Form.Item>
              <Form.Item name="rackFrom" label="Rack từ"><Input type="number" /></Form.Item>
              <Form.Item name="rackTo" label="đến"><Input type="number" /></Form.Item>
              <Form.Item name="binFrom" label="Bin từ"><Input type="number" /></Form.Item>
              <Form.Item name="binTo" label="đến"><Input type="number" /></Form.Item>
            </Space>
            <Button type="primary" onClick={submitGen} loading={genMut.isPending}>
              Sinh ô kệ
            </Button>
          </Form>
          <Divider />
        </>
      )}

      <Table rowKey="id" size="small" loading={bins.isLoading}
        dataSource={bins.data || []} columns={binColumns}
        pagination={{ pageSize: 20 }} scroll={{ x: 'max-content' }} />
    </Drawer>
  )
}

// Thứ tự ô theo hàng dọc: lẻ tăng dần trước, chẵn tăng dần sau.
// vd 1..6 -> ["1","3","5","2","4","6"]. Nếu không phải số -> giữ nguyên 1 phần tử.
function verticalBinOrder(from, to) {
  const a = Number(from), b = Number(to)
  if (!(Number.isFinite(a) && Number.isFinite(b) && b >= a)) return [String(from)]
  const nums = Array.from({ length: b - a + 1 }, (_, i) => a + i)
  const odds = nums.filter(n => n % 2 === 1)
  const evens = nums.filter(n => n % 2 === 0)
  return [...odds, ...evens].map(String)
}

// "1".."5" -> [1,2,3,4,5]; nếu không phải số thì trả về mảng 1 phần tử.
function expandRange(from, to) {
  const a = Number(from), b = Number(to)
  if (Number.isFinite(a) && Number.isFinite(b) && b >= a) {
    return Array.from({ length: b - a + 1 }, (_, i) => a + i)
  }
  return [from]
}
