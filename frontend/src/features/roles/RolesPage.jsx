import FitTable from '../../components/FitTable'
import { useMemo, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Table, Button, Space, Modal, Form, Input, Popconfirm, Typography,
  Checkbox, Collapse, Tag, Empty, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, SafetyCertificateOutlined,
} from '@ant-design/icons'
import Can from '../../components/Can'
import { rolesApi } from '../../api/roles.api'
import { permissionsApi } from '../../api/permissions.api'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'

export default function RolesPage() {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [permRole, setPermRole] = useState(null)

  const roles = useQuery({ queryKey: ['roles'], queryFn: rolesApi.list })
  const invalidate = () => qc.invalidateQueries({ queryKey: ['roles'] })

  const createMut = useMutation({
    mutationFn: rolesApi.create,
    onSuccess: () => { message.success('Đã tạo vai trò'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => rolesApi.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const removeMut = useMutation({
    mutationFn: rolesApi.remove,
    onSuccess: () => { message.success('Đã xoá'); invalidate() },
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
    { title: 'Mã', dataIndex: 'code', width: 160 },
    { title: 'Tên', dataIndex: 'name' },
    { title: 'Mô tả', dataIndex: 'description' },
    {
      title: 'Số quyền', dataIndex: 'permissionIds', width: 110,
      render: (ids) => <Tag>{ids?.length || 0}</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 180, fixed: 'right',
      render: (_, row) => (
        <Space>
          <Can permission={P.ROLE_UPDATE}>
            <Button size="small" icon={<SafetyCertificateOutlined />} title="Phân quyền"
              onClick={() => setPermRole(row)} />
          </Can>
          <Can permission={P.ROLE_UPDATE}>
            <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(row)} />
          </Can>
          <Can permission={P.ROLE_DELETE}>
            <Popconfirm title="Xoá vai trò?" okText="Xoá" cancelText="Huỷ"
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
        <Typography.Title level={4} style={{ margin: 0 }}>Vai trò</Typography.Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => roles.refetch()} loading={roles.isFetching} />
          <Can permission={P.ROLE_CREATE}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm vai trò</Button>
          </Can>
        </Space>
      </div>

      <FitTable rowKey="id" loading={roles.isLoading} dataSource={roles.data || []}
        columns={columns} scroll={{ x: 'max-content' }}
        pagination={{ pageSize: 10 }} />

      <Modal title={editing ? 'Sửa vai trò' : 'Thêm vai trò'} open={open}
        onOk={submit} onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="Mã vai trò"
            rules={[{ required: true, message: 'Nhập mã' }]}>
            <Input disabled={!!editing} placeholder="VD: STOREKEEPER" />
          </Form.Item>
          <Form.Item name="name" label="Tên"
            rules={[{ required: true, message: 'Nhập tên' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <PermissionMatrixModal role={permRole} onClose={() => setPermRole(null)} />
    </>
  )
}

function PermissionMatrixModal({ role, onClose }) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const open = !!role

  const perms = useQuery({ queryKey: ['permissions'], queryFn: permissionsApi.list, enabled: open })
  const [checked, setChecked] = useState([])

  const grouped = useMemo(() => {
    const g = {}
    for (const p of perms.data || []) {
      (g[p.module || 'Khác'] ||= []).push(p)
    }
    return g
  }, [perms.data])

  const saveMut = useMutation({
    mutationFn: (ids) => rolesApi.assignPermissions(role.id, ids),
    onSuccess: () => {
      message.success('Đã cập nhật quyền cho vai trò')
      qc.invalidateQueries({ queryKey: ['roles'] })
      onClose()
    },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const toggleModule = (mod, on) => {
    const ids = grouped[mod].map(p => p.id)
    setChecked((prev) => on ? [...new Set([...prev, ...ids])] : prev.filter(x => !ids.includes(x)))
  }

  const items = Object.entries(grouped).map(([mod, list]) => {
    const ids = list.map(p => p.id)
    const all = ids.every(id => checked.includes(id))
    const some = ids.some(id => checked.includes(id))
    return {
      key: mod,
      label: (
        <Checkbox checked={all} indeterminate={!all && some}
          onClick={(e) => e.stopPropagation()}
          onChange={(e) => toggleModule(mod, e.target.checked)}>
          <b>{mod}</b> <Tag>{ids.filter(id => checked.includes(id)).length}/{ids.length}</Tag>
        </Checkbox>
      ),
      children: (
        <Checkbox.Group
          value={checked}
          onChange={(vals) => {
            const others = checked.filter(id => !ids.includes(id))
            setChecked([...others, ...vals.filter(v => ids.includes(v))])
          }}
          style={{ display: 'flex', flexDirection: 'column', gap: 8 }}
          options={list.map(p => ({ value: p.id, label: `${p.name}  (${p.code})` }))}
        />
      ),
    }
  })

  return (
    <Modal
      title={`Phân quyền: ${role?.name || ''}`}
      open={open}
      width={640}
      onCancel={onClose}
      onOk={() => saveMut.mutate(checked)}
      confirmLoading={saveMut.isPending}
      afterOpenChange={(o) => { if (o) setChecked(role?.permissionIds || []) }}
      destroyOnClose
    >
      {perms.isLoading ? 'Đang tải...' :
        items.length ? <Collapse items={items} defaultActiveKey={Object.keys(grouped)} />
          : <Empty description="Chưa có quyền nào" />}
    </Modal>
  )
}
