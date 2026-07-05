import { useAuth } from '../auth/AuthContext'
// Ẩn/hiện theo quyền. <Can permission="USER_CREATE">...</Can>
export default function Can({ permission, children, fallback = null }) {
  const { hasPermission } = useAuth()
  if (!permission) return children
  return hasPermission(permission) ? children : fallback
}
