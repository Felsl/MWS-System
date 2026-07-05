import CrudResource from '../../components/CrudResource'
import { permissionsApi } from '../../api/permissions.api'
import { P } from '../../constants/permissions'

export default function PermissionsPage() {
  return (
    <CrudResource
      title="Quyền hạn"
      queryKey="permissions"
      api={permissionsApi}
      perms={{ create: P.PERMISSION_CREATE, update: P.PERMISSION_UPDATE, remove: P.PERMISSION_DELETE }}
      columns={[
        { title: 'Mã quyền', dataIndex: 'code', width: 220 },
        { title: 'Tên', dataIndex: 'name' },
        { title: 'Module', dataIndex: 'module', width: 160 },
      ]}
      fields={[
        { name: 'code', label: 'Mã quyền', rules: [{ required: true, message: 'Nhập mã' }] },
        { name: 'name', label: 'Tên', rules: [{ required: true, message: 'Nhập tên' }] },
        { name: 'module', label: 'Module', rules: [{ required: true, message: 'Nhập module' }] },
      ]}
    />
  )
}
