import FitTable from '../../components/FitTable'
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Tabs, Tag, Button, Modal, Form, Input, App as AntdApp } from 'antd'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons'
import CrudResource from '../../components/CrudResource'
import { suppliersApi, customersApi, carriersApi } from '../../api/partners.api'

import { handleFormError } from '../../utils/formErrors'
import { P } from '../../constants/permissions'
import Can from '../../components/Can'

const partnerPerms = {
  create: P.INBOUND_APPROVE_PO,
  update: P.INBOUND_APPROVE_PO,
  remove: P.INBOUND_APPROVE_PO,
}

const statusCol = {
  title: 'Trạng thái', dataIndex: 'status', width: 120,
  render: (s) => <Tag color={s === 'ACTIVE' ? 'green' : 'default'}>{s || 'ACTIVE'}</Tag>,
}

function Suppliers() {
  return (
    <CrudResource
      title="Nhà cung cấp" queryKey="suppliers" api={suppliersApi} perms={partnerPerms}
      columns={[
        { title: 'Mã', dataIndex: 'code', width: 120 },
        { title: 'Tên', dataIndex: 'name' },
        { title: 'Người liên hệ', dataIndex: 'contactName' },
        { title: 'Điện thoại', dataIndex: 'phone', width: 130 },
        { title: 'Email', dataIndex: 'email' },
        statusCol,
      ]}
      fields={[
        { name: 'code', label: 'Mã', rules: [{ required: true, message: 'Nhập mã' }] },
        { name: 'name', label: 'Tên', rules: [{ required: true, message: 'Nhập tên' }] },
        { name: 'contactName', label: 'Người liên hệ' },
        { name: 'phone', label: 'Điện thoại' },
        { name: 'email', label: 'Email', rules: [{ type: 'email', message: 'Email không hợp lệ' }] },
        { name: 'address', label: 'Địa chỉ' },
      ]}
    />
  )
}

function Customers() {
  return (
    <CrudResource
      title="Khách hàng" queryKey="customers" api={customersApi} perms={partnerPerms}
      columns={[
        { title: 'Mã', dataIndex: 'code', width: 120 },
        { title: 'Tên', dataIndex: 'name' },
        { title: 'MST', dataIndex: 'taxCode', width: 130 },
        { title: 'Điện thoại', dataIndex: 'phone', width: 130 },
        { title: 'Email', dataIndex: 'email' },
        statusCol,
      ]}
      fields={[
        { name: 'code', label: 'Mã', rules: [{ required: true, message: 'Nhập mã' }] },
        { name: 'name', label: 'Tên', rules: [{ required: true, message: 'Nhập tên' }] },
        { name: 'taxCode', label: 'Mã số thuế' },
        { name: 'phone', label: 'Điện thoại' },
        { name: 'email', label: 'Email', rules: [{ type: 'email', message: 'Email không hợp lệ' }] },
        { name: 'address', label: 'Địa chỉ' },
      ]}
    />
  )
}

// BE: Carrier chỉ có POST + GET (không PUT/DELETE) -> tab này create-only.
// shippingFeeRule là 1 chuỗi JSON thô -> gộp 2 ô text thành JSON để lưu.
// Đổi 2 khóa feeBase/feePerKg dưới đây nếu team thống nhất schema khác.
const FEE_KEYS = { first: 'feeBase', second: 'feePerKg' }
const FEE_LABELS = { first: 'Phí cơ bản', second: 'Phí theo kg' }

function parseFee(raw) {
  try { const o = JSON.parse(raw || '{}'); return { first: o[FEE_KEYS.first] ?? '', second: o[FEE_KEYS.second] ?? '' } }
  catch { return { first: raw || '', second: '' } }
}

function Carriers() {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)

  const list = useQuery({ queryKey: ['carriers'], queryFn: carriersApi.list })
  const createMut = useMutation({
    mutationFn: carriersApi.create,
    onSuccess: () => { message.success('Đã tạo đơn vị vận chuyển'); setOpen(false); qc.invalidateQueries({ queryKey: ['carriers'] }) },
    onError: (e) => handleFormError(form, e, message),
  })

  const submit = async () => {
    const v = await form.validateFields()
    const shippingFeeRule = JSON.stringify({ [FEE_KEYS.first]: v.feeFirst || '', [FEE_KEYS.second]: v.feeSecond || '' })
    createMut.mutate({ code: v.code, name: v.name, shippingFeeRule })
  }

  const columns = [
    { title: 'Mã', dataIndex: 'code', width: 120 },
    { title: 'Tên', dataIndex: 'name' },
    { title: FEE_LABELS.first, key: 'f1', width: 140, render: (_, r) => parseFee(r.shippingFeeRule).first || '—' },
    { title: FEE_LABELS.second, key: 'f2', width: 140, render: (_, r) => parseFee(r.shippingFeeRule).second || '—' },
    statusCol,
  ]

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 12 }}>
        <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} style={{ marginRight: 8 }} />
        <Can permission={P.INBOUND_APPROVE_PO}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setOpen(true) }}>
          Thêm đơn vị vận chuyển
        </Button>
        </Can>
      </div>
      <FitTable rowKey="id" size="middle" loading={list.isLoading} dataSource={list.data || []}
        columns={columns} scroll={{ x: 'max-content' }} pagination={{ pageSize: 10 }} />

      <Modal title="Thêm đơn vị vận chuyển" open={open} onOk={submit} onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending} destroyOnClose>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="Mã" rules={[{ required: true, message: 'Nhập mã' }]}><Input /></Form.Item>
          <Form.Item name="name" label="Tên" rules={[{ required: true, message: 'Nhập tên' }]}><Input /></Form.Item>
          <Form.Item name="feeFirst" label={FEE_LABELS.first}><Input placeholder="VD: 20000" /></Form.Item>
          <Form.Item name="feeSecond" label={FEE_LABELS.second}><Input placeholder="VD: 5000" /></Form.Item>
        </Form>
      </Modal>
    </>
  )
}

export default function PartnersPage() {
  return (
    <Tabs
      items={[
        { key: 's', label: 'Nhà cung cấp', children: <Suppliers /> },
        { key: 'c', label: 'Khách hàng', children: <Customers /> },
        { key: 'r', label: 'Vận chuyển', children: <Carriers /> },
      ]}
    />
  )
}
