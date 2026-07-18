import { useState } from 'react'
import { Card, Form, Input, Button, Typography, App as AntdApp, theme } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate, useLocation, Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { getErrorMessage } from '../../api/client'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const { message } = AntdApp.useApp()
  const navigate = useNavigate()
  const location = useLocation()
  const [loading, setLoading] = useState(false)
  const { token } = theme.useToken()

  if (isAuthenticated) return <Navigate to="/" replace />

  const onFinish = async ({ username, password }) => {
    setLoading(true)
    try {
      await login(username, password)
      message.success('Đăng nhập thành công')
      const to = location.state?.from?.pathname || '/'
      navigate(to, { replace: true })
    } catch (e) {
      message.error(getErrorMessage(e, 'Sai tài khoản hoặc mật khẩu'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ display: 'grid', placeItems: 'center', height: '100vh', background: token.colorBgLayout }}>
      <Card style={{ width: 380 }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Typography.Title level={3} style={{ marginBottom: 0 }}>MWS · Quản lý kho</Typography.Title>
          <Typography.Text type="secondary">Đăng nhập hệ thống</Typography.Text>
        </div>
        <Form layout="vertical" onFinish={onFinish} requiredMark={false}>
          <Form.Item name="username" label="Tài khoản"
            rules={[{ required: true, message: 'Nhập tài khoản' }]}>
            <Input prefix={<UserOutlined />} placeholder="username" autoFocus />
          </Form.Item>
          <Form.Item name="password" label="Mật khẩu"
            rules={[{ required: true, message: 'Nhập mật khẩu' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={loading}>Đăng nhập</Button>
        </Form>
      </Card>
    </div>
  )
}
