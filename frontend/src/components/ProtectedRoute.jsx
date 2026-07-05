import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { Spin, Result, Button } from 'antd'
import { useAuth } from '../auth/AuthContext'

export default function ProtectedRoute({ permission }) {
  const { isAuthenticated, loading, hasPermission } = useAuth()
  const location = useLocation()

  if (loading) {
    return <div style={{ display: 'grid', placeItems: 'center', height: '100vh' }}><Spin size="large" /></div>
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }
  if (permission && !hasPermission(permission)) {
    return (
      <Result status="403" title="403"
        subTitle="Bạn không có quyền truy cập trang này."
        extra={<Button type="primary" href="/">Về trang chủ</Button>} />
    )
  }
  return <Outlet />
}
