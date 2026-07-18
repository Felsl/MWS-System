import FitTable from '../../components/FitTable'
import EmptyState from '../../components/EmptyState'
import PageHeader from '../../components/PageHeader'
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Button, Space, Modal, Form, Input, Select, Tag, Popconfirm, Typography, Alert, Empty, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, HomeOutlined,
} from '@ant-design/icons'
import Can from '../../components/Can'
import { usersApi } from '../../api/users.api'
import { rolesApi } from '../../api/roles.api'
import { warehousesApi } from '../../api/warehouses.api'
import { getErrorMessage } from '../../api/client'
import { handleFormError } from '../../utils/formErrors'
import { P } from '../../constants/permissions'

export default function UsersPage() {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [whUser, setWhUser] = useState(null) // user đang gán kho

  const users = useQuery({ queryKey: ['users'], queryFn: () => usersApi.list() })
  const roles = useQuery({ queryKey: ['roles'], queryFn: rolesApi.list })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['users'] })
  const createMut = useMutation({
    mutationFn: usersApi.create,
    onSuccess: () => { message.success('Đã tạo người dùng'); setOpen(false); invalidate() },
    onError: (e) => handleFormError(form, e, message),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => usersApi.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => handleFormError(form, e, message),
  })
  const removeMut = useMutation({
    mutationFn: usersApi.remove,
    onSuccess: () => { message.success('Đã xoá'); invalidate() },
    onError: (e) => handleFormError(form, e, message),
  })

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (row) => {
    setEditing(row)
    form.setFieldsValue({ ...row, roleId: row.roleId })
    setOpen(true)
  }
  const submit = async () => {
    const body = await form.validateFields()
    if (editing) {
      // eslint-disable-next-line no-unused-vars -- loại 2 trường này khỏi payload update
      const { password, username, ...rest } = body // không đổi username/password ở update
      updateMut.mutate({ id: editing.id, body: rest })
    } else {
      createMut.mutate(body)
    }
  }

  const roleOptions = (roles.data || []).map(r => ({ value: r.id, label: `${r.name} (${r.code})` }))

  const columns = [
    { title: 'Tài khoản', dataIndex: 'username', width: 150 },
    { title: 'Họ tên', dataIndex: 'fullName' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Điện thoại', dataIndex: 'phone', width: 130 },
    { title: 'Vai trò', dataIndex: 'roleName', width: 150, render: (v) => v && <Tag color="blue">{v}</Tag> },
    {
      title: 'Trạng thái', dataIndex: 'status', width: 120,
      render: (s) => <Tag color={s === 'ACTIVE' ? 'green' : 'red'}>{s}</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 170, fixed: 'right',
      render: (_, row) => (
        <Space>
          <Can permission={P.USER_ASSIGN_WAREHOUSE}>
            <Button size="small" icon={<HomeOutlined />} title="Gán kho"
              onClick={() => setWhUser(row)} />
          </Can>
          <Can permission={P.USER_UPDATE}>
            <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(row)} />
          </Can>
          <Can permission={P.USER_DELETE}>
            <Popconfirm title="Xoá người dùng?"
              description={<span>Xoá tài khoản <b>{row.username}</b>{row.fullName ? ` (${row.fullName})` : ''}. Không hoàn tác được.</span>}
              okText="Xoá" okButtonProps={{ danger: true }} cancelText="Huỷ"
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
      <PageHeader
        title="Người dùng"
        extra={<>
          <Button icon={<ReloadOutlined />} onClick={() => users.refetch()} loading={users.isFetching} />
          <Can permission={P.USER_CREATE}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm người dùng</Button>
          </Can>
        </>}
      />

      <FitTable rowKey="id" loading={users.isLoading} dataSource={users.data || []}
        emptyState={<EmptyState title="Chưa có người dùng nào" action={{ label: 'Thêm người dùng', onClick: openCreate, permission: P.USER_CREATE }} />}
        columns={columns} scroll={{ x: 'max-content' }}
        pagination={{ pageSize: 10, showSizeChanger: true }} />

      <Modal title={editing ? 'Sửa người dùng' : 'Thêm người dùng'} open={open}
        onOk={submit} onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="Tài khoản"
            rules={[{ required: true, message: 'Nhập tài khoản' }]}>
            <Input disabled={!!editing} />
          </Form.Item>
          {!editing && (
            <Form.Item name="password" label="Mật khẩu"
              rules={[{ required: true, message: 'Nhập mật khẩu' }]}>
              <Input.Password />
            </Form.Item>
          )}
          <Form.Item name="fullName" label="Họ tên"
            rules={[{ required: true, message: 'Nhập họ tên' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email"
            rules={[{ type: 'email', message: 'Email không hợp lệ' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Điện thoại"><Input /></Form.Item>
          <Form.Item name="roleId" label="Vai trò"
            rules={[{ required: true, message: 'Chọn vai trò' }]}>
            <Select options={roleOptions} loading={roles.isLoading} placeholder="Chọn vai trò" />
          </Form.Item>
        </Form>
      </Modal>

      <WarehouseAccessModal user={whUser} onClose={() => setWhUser(null)} />
    </>
  )
}

function WarehouseAccessModal({ user, onClose }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const open = !!user

  const warehouses = useQuery({
    queryKey: ['warehouses', 'admin'],
    queryFn: () => warehousesApi.list(true),
    enabled: open,
  })
  const current = useQuery({
    queryKey: ['user-warehouses', user?.id],
    queryFn: () => usersApi.getWarehouses(user.id),
    enabled: open,
  })

  const [selected, setSelected] = useState([])
  // Response chuẩn: { userId, warehouseIds: [...] }
  const currentIds = current.data?.warehouseIds || []

  const saveMut = useMutation({
    mutationFn: (ids) => usersApi.assignWarehouses(user.id, ids),
    onSuccess: () => {
      message.success('Đã cập nhật quyền truy cập kho')
      qc.invalidateQueries({ queryKey: ['user-warehouses', user.id] })
      onClose()
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  return (
    <Modal
      title={`Gán kho cho: ${user?.fullName || user?.username || ''}`}
      open={open}
      onCancel={onClose}
      onOk={() => {
        if (!selected.length) {
          message.warning('Chọn ít nhất 1 kho. BE hiện chưa hỗ trợ bỏ toàn bộ kho của người dùng.')
          return
        }
        saveMut.mutate(selected)
      }}
      confirmLoading={saveMut.isPending}
      afterOpenChange={(o) => { if (o) setSelected(currentIds) }}
      destroyOnClose
    >
      <Typography.Paragraph type="secondary">
        Chọn các kho người dùng được phép thao tác. BE sẽ lọc dữ liệu theo danh sách này.
      </Typography.Paragraph>

      {warehouses.isError && (
        <Alert type="error" showIcon style={{ marginBottom: 12 }}
          message="Không tải được danh sách kho"
          description={
            <>
              {getErrorMessage(warehouses.error)}
              <br />
              Lấy danh sách kho cần quyền <b>WAREHOUSE_VIEW</b>. Tài khoản đang đăng nhập
              cần có cả <b>USER_ASSIGN_WAREHOUSE</b> lẫn <b>WAREHOUSE_VIEW</b> để dùng chức năng này.
            </>
          }
          action={<Button size="small" onClick={() => warehouses.refetch()}>Thử lại</Button>}
        />
      )}

      {!warehouses.isError && !warehouses.isLoading && (warehouses.data || []).length === 0 && (
        <Empty description="Chưa có kho nào (hoặc bạn không có kho trong phạm vi). Hãy tạo kho ở mục Kho & ô kệ trước." />
      )}

      <Select
        mode="multiple"
        style={{ width: '100%' }}
        placeholder="Chọn kho"
        loading={warehouses.isLoading || current.isLoading}
        value={selected}
        onChange={setSelected}
        notFoundContent={warehouses.isLoading ? 'Đang tải...' : 'Không có kho'}
        options={(warehouses.data || []).map(w => ({ value: w.id, label: `${w.name} (${w.code})` }))}
      />
    </Modal>
  )
}
