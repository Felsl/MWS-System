import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Table, Button, Space, Modal, Form, Input, Popconfirm, Typography, App as AntdApp,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons'
import Can from './Can'
import { getErrorMessage } from '../api/client'

/**
 * CRUD chuẩn cho tài nguyên trả List thuần (category, supplier, customer, carrier, permission).
 * props:
 *  - title, queryKey, api {list, create, update, remove}
 *  - columns: cột hiển thị (antd)
 *  - fields: [{name,label,rules,input?}] để sinh form
 *  - perms: {create, update, remove}
 *  - rowKey (mặc định 'id')
 */
export default function CrudResource({
  title, queryKey, api, columns, fields, perms = {}, rowKey = 'id',
}) {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)

  const { data = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: [queryKey],
    queryFn: api.list,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: [queryKey] })

  const createMut = useMutation({
    mutationFn: api.create,
    onSuccess: () => { message.success('Đã tạo'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => api.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
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

  const actionCol = {
    title: 'Thao tác', key: '_action', width: 140, fixed: 'right',
    render: (_, row) => (
      <Space>
        <Can permission={perms.update}>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(row)} />
        </Can>
        <Can permission={perms.remove}>
          <Popconfirm title="Xoá mục này?" okText="Xoá" cancelText="Huỷ"
            onConfirm={() => removeMut.mutate(row[rowKey])}>
            <Button size="small" danger icon={<DeleteOutlined />} />
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

      <Table
        rowKey={rowKey}
        loading={isLoading}
        dataSource={data}
        columns={[...columns, actionCol]}
        scroll={{ x: 'max-content' }}
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
