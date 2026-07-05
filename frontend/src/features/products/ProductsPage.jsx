import { useState } from 'react'
import { keepPreviousData, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Table, Button, Space, Modal, Form, Input, Select, InputNumber, Switch, Tag,
  Popconfirm, Typography, App as AntdApp,
} from 'antd'
import {
  PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined, SearchOutlined,
} from '@ant-design/icons'
import Can from '../../components/Can'
import { productsApi } from '../../api/products.api'
import { categoriesApi } from '../../api/categories.api'
import { getErrorMessage } from '../../api/client'
import { P } from '../../constants/permissions'
import { UNITS, UNIT_LABEL } from '../../constants/units'

export default function ProductsPage() {
  const { message } = AntdApp.useApp()
  const qc = useQueryClient()
  const [form] = Form.useForm()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [keyword, setKeyword] = useState('')
  const [pager, setPager] = useState({ page: 0, size: 20 })

  const categories = useQuery({ queryKey: ['categories'], queryFn: categoriesApi.list })
  const list = useQuery({
    queryKey: ['products', keyword, pager.page, pager.size],
    queryFn: () => productsApi.list({ keyword: keyword || undefined, page: pager.page, size: pager.size }),
    placeholderData: keepPreviousData,
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['products'] })
  const createMut = useMutation({
    mutationFn: productsApi.create,
    onSuccess: () => { message.success('Đã tạo sản phẩm'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, body }) => productsApi.update(id, body),
    onSuccess: () => { message.success('Đã cập nhật'); setOpen(false); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })
  const removeMut = useMutation({
    mutationFn: productsApi.remove,
    onSuccess: () => { message.success('Đã xoá'); invalidate() },
    onError: (e) => message.error(getErrorMessage(e)),
  })

  const openCreate = () => {
    setEditing(null); form.resetFields()
    form.setFieldsValue({ unit: 'PCS', safetyStock: 10, hazardousFlag: false })
    setOpen(true)
  }
  const openEdit = (row) => { setEditing(row); form.setFieldsValue(row); setOpen(true) }
  const submit = async () => {
    const body = await form.validateFields()
    if (editing) updateMut.mutate({ id: editing.id, body })
    else createMut.mutate(body)
  }

  const catOptions = (categories.data || []).map(c => ({ value: c.id, label: c.name }))

  const columns = [
    { title: 'SKU', dataIndex: 'sku', width: 140 },
    { title: 'Tên', dataIndex: 'name' },
    { title: 'Nhóm', dataIndex: 'categoryName', width: 140 },
    { title: 'ĐVT', dataIndex: 'unit', width: 90, render: (u) => UNIT_LABEL[u] || u },
    {
      title: 'Giá bán', dataIndex: 'price', width: 120, align: 'right',
      render: (v) => v != null ? Number(v).toLocaleString('vi-VN') : '—',
    },
    { title: 'Tồn an toàn', dataIndex: 'safetyStock', width: 110, align: 'right' },
    {
      title: 'Nguy hiểm', dataIndex: 'hazardousFlag', width: 100,
      render: (v) => v ? <Tag color="red">Có</Tag> : <Tag>Không</Tag>,
    },
    {
      title: 'Thao tác', key: '_a', width: 120, fixed: 'right',
      render: (_, row) => (
        <Space>
          <Can permission={P.MASTER_PRODUCT_MANAGE}>
            <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(row)} />
          </Can>
          <Can permission={P.MASTER_PRODUCT_MANAGE}>
            <Popconfirm title="Xoá sản phẩm?" okText="Xoá" cancelText="Huỷ"
              onConfirm={() => removeMut.mutate(row.id)}>
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </Can>
        </Space>
      ),
    },
  ]

  const page = list.data // PageResponse

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16, gap: 12, flexWrap: 'wrap' }}>
        <Typography.Title level={4} style={{ margin: 0 }}>Sản phẩm</Typography.Title>
        <Space wrap>
          <Input.Search allowClear placeholder="Tìm theo tên / SKU" prefix={<SearchOutlined />}
            style={{ width: 240 }}
            onSearch={(v) => { setKeyword(v); setPager(p => ({ ...p, page: 0 })) }} />
          <Button icon={<ReloadOutlined />} onClick={() => list.refetch()} loading={list.isFetching} />
          <Can permission={P.MASTER_PRODUCT_MANAGE}>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm sản phẩm</Button>
          </Can>
        </Space>
      </div>

      <Table
        rowKey="id"
        loading={list.isLoading}
        dataSource={page?.content || []}
        columns={columns}
        scroll={{ x: 'max-content' }}
        pagination={{
          current: (page?.page ?? 0) + 1,
          pageSize: page?.size ?? 20,
          total: page?.totalElements ?? 0,
          showSizeChanger: true,
          showTotal: (t) => `Tổng ${t}`,
          onChange: (p, s) => setPager({ page: p - 1, size: s }),
        }}
      />

      <Modal title={editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'} open={open} width={640}
        onOk={submit} onCancel={() => setOpen(false)}
        confirmLoading={createMut.isPending || updateMut.isPending} destroyOnClose>
        <Form form={form} layout="vertical">
          <Space style={{ display: 'flex' }} align="start" wrap>
            <Form.Item name="sku" label="SKU" rules={[{ required: true, message: 'Nhập SKU' }]}>
              <Input style={{ width: 200 }} />
            </Form.Item>
            <Form.Item name="barcode" label="Barcode">
              <Input style={{ width: 200 }} />
            </Form.Item>
          </Space>
          <Form.Item name="name" label="Tên sản phẩm" rules={[{ required: true, message: 'Nhập tên' }]}>
            <Input />
          </Form.Item>
          <Space style={{ display: 'flex' }} align="start" wrap>
            <Form.Item name="categoryId" label="Nhóm sản phẩm">
              <Select style={{ width: 200 }} options={catOptions} allowClear
                loading={categories.isLoading} placeholder="Chọn nhóm" />
            </Form.Item>
            <Form.Item name="unit" label="Đơn vị tính" rules={[{ required: true, message: 'Chọn ĐVT' }]}>
              <Select style={{ width: 160 }}
                options={UNITS.map(u => ({ value: u, label: `${UNIT_LABEL[u]} (${u})` }))} />
            </Form.Item>
            <Form.Item name="safetyStock" label="Tồn an toàn">
              <InputNumber min={0} style={{ width: 140 }} />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex' }} align="start" wrap>
            <Form.Item name="price" label="Giá bán">
              <InputNumber min={0} style={{ width: 160 }} formatter={fmt} parser={parse} />
            </Form.Item>
            <Form.Item name="costPrice" label="Giá vốn">
              <InputNumber min={0} style={{ width: 160 }} formatter={fmt} parser={parse} />
            </Form.Item>
            <Form.Item name="weight" label="Khối lượng">
              <InputNumber min={0} style={{ width: 120 }} />
            </Form.Item>
            <Form.Item name="volume" label="Thể tích">
              <InputNumber min={0} style={{ width: 120 }} />
            </Form.Item>
          </Space>
          <Form.Item name="hazardousFlag" label="Hàng nguy hiểm" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  )
}

const fmt = (v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
const parse = (v) => v?.replace(/,/g, '')
