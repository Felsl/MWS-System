import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Button, Space, Modal, Form, Input, Popconfirm, Typography, App as AntdApp,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons'
import Can from './Can'
import { useAuth } from '../auth/AuthContext'
import FitTable from './FitTable'
import EmptyState from './EmptyState'
import { getErrorMessage } from '../api/client'
import { handleFormError } from '../utils/formErrors'

/**
 * CRUD chuẩn cho tài nguyên trả List thuần (category, supplier, customer, carrier, permission).
 * props:
 *  - title, queryKey, api {list, create, update, remove}
 *  - columns: cột hiển thị (antd)
 *  - fields: [{name,label,rules,input?}] để sinh form
 *  - perms: {create, update, remove}
 *  - rowKey (mặc định 'id')
 *  - entityName: danh từ dùng trong câu xác nhận xoá (mặc định lấy từ title)
 *  - labelOf: (row) => tên hiển thị của bản ghi trong câu xác nhận xoá
 */
export default function CrudResource({
  title, queryKey, api, columns, fields, perms = {}, rowKey = 'id',
  entityName, labelOf,
}) {
  const { message } = AntdApp.useApp()
  const { hasPermission } = useAuth()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)

  const { data = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: [queryKey],
    queryFn: api.list,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: [queryKey] })

  // Lỗi validate BE -> gắn vào đúng Form.Item; lỗi khác -> toast.
  const createMut = useMutation({
    mutationFn: api.create,
    onSuccess: () => { message.success('Đã tạo'); setOpen(false); invalidate() },
    onError: (e) => handleFormError(form, e, message),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => api.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => handleFormError(form, e, message),
  })
  const removeMut = useMutation({
    mutationFn: api.remove,
    onSuccess: () => { message.success('Đã xoá'); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const openCreate = () => { setEditing(null); form.resetFields(); setOpen(true) }
  const openEdit = (row) => { setEditing(row); form.setFieldsValue(row); setOpen(true) }

  const submit = async () => {
    const body = await form.validateFields()
    if (editing) updateMut.mutate({ id: editing[rowKey], body })
    else createMut.mutate(body)
  }

  // Tên bản ghi cho câu xác nhận xoá. Mọi danh mục đang dùng CrudResource đều
  // có code + name; vẫn cho ghi đè qua prop labelOf.
  const nameOf = labelOf || ((row) => {
    if (row?.code && row?.name) return `${row.code} – ${row.name}`
    return row?.name || row?.code || row?.[rowKey]
  })
  const noun = entityName || String(title || 'mục').toLowerCase()

  const canUpdate = !perms.update || hasPermission(perms.update)
  const canRemove = !perms.remove || hasPermission(perms.remove)

  const actionCol = {
    title: 'Thao tác', key: '_action', width: 140, fixed: 'right',
    render: (_, row) => (
      <Space>
        <Can permission={perms.update}>
          <Button size="small" aria-label="Sửa" icon={<EditOutlined />} onClick={() => openEdit(row)} />
        </Can>
        <Can permission={perms.remove}>
          {/* TRƯỚC: "Xoá mục này?" — không nói xoá cái gì. Trên bảng 20 dòng,
              bấm nhầm dòng là mất bản ghi khác mà không hề hay biết.
              NAY: gọi đúng tên bản ghi + cảnh báo không hoàn tác. */}
          <Popconfirm
            title={`Xoá ${noun}?`}
            description={<span>Xoá <b>{nameOf(row)}</b>. Thao tác này không hoàn tác được.</span>}
            okText="Xoá" okButtonProps={{ danger: true }} cancelText="Huỷ"
            onConfirm={() => removeMut.mutate(row[rowKey])}>
            <Button size="small" danger aria-label="Xoá" icon={<DeleteOutlined />} />
          </Popconfirm>
        </Can>
      </Space>
    ),
  }

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Typography.Title level={4} style={{ margin: 0 }}>{title}</Typography.Title>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => refetch()} loading={isFetching} />
          <Can permission={perms.create}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm mới</Button>
          </Can>
        </Space>
      </div>

      <FitTable
        rowKey={rowKey}
        loading={isLoading}
        dataSource={data}
        columns={(canUpdate || canRemove) ? [...columns, actionCol] : columns}
        emptyState={<EmptyState
          title={`Chưa có ${noun} nào`}
          action={perms.create ? { label: `Thêm ${noun}`, onClick: openCreate, permission: perms.create } : undefined}
        />}
        pagination={{ pageSize: 10, showSizeChanger: true }}
        size="middle"
      />

      <Modal
        title={editing ? `Sửa ${title.toLowerCase()}` : `Thêm ${title.toLowerCase()}`}
        open={open}
        onOk={submit}
        onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          {fields.map((f) => (
            <Form.Item key={f.name} name={f.name} label={f.label} rules={f.rules}>
              {f.input || <Input />}
            </Form.Item>
          ))}
        </Form>
      </Modal>
    </>
  )
}
