import { Input } from 'antd'
import CrudResource from '../../components/CrudResource'
import { categoriesApi } from '../../api/categories.api'
import { P } from '../../constants/permissions'

export default function CategoriesPage() {
  return (
    <CrudResource
      title="Nhóm sản phẩm"
      queryKey="categories"
      api={categoriesApi}
      perms={{ create: P.MASTER_PRODUCT_MANAGE, update: P.MASTER_PRODUCT_MANAGE, remove: P.MASTER_PRODUCT_MANAGE }}
      columns={[
        { title: 'Mã', dataIndex: 'code', width: 140 },
        { title: 'Tên', dataIndex: 'name' },
        { title: 'Mô tả', dataIndex: 'description' },
      ]}
      fields={[
        { name: 'code', label: 'Mã', rules: [{ required: true, message: 'Nhập mã' }] },
        { name: 'name', label: 'Tên', rules: [{ required: true, message: 'Nhập tên' }] },
        { name: 'description', label: 'Mô tả', input: <Input.TextArea rows={2} /> },
      ]}
    />
  )
}
