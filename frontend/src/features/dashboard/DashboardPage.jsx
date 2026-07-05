import { Typography, Card, Col, Row, Statistic } from 'antd'
import { useAuth } from '../../auth/AuthContext'

export default function DashboardPage() {
  const { user } = useAuth()
  return (
    <div>
      <Typography.Title level={4}>Xin chào, {user?.fullName || user?.username}</Typography.Title>
      <Typography.Paragraph type="secondary">
        Vai trò: <b>{user?.role}</b> · Số quyền: {user?.permissions?.length || 0}
      </Typography.Paragraph>
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col xs={12} md={6}><Card><Statistic title="Sản phẩm" value="—" /></Card></Col>
        <Col xs={12} md={6}><Card><Statistic title="Kho" value="—" /></Card></Col>
        <Col xs={12} md={6}><Card><Statistic title="Đơn chờ duyệt" value="—" /></Card></Col>
        <Col xs={12} md={6}><Card><Statistic title="Lô cận hạn" value="—" /></Card></Col>
      </Row>
      <Typography.Paragraph type="secondary" style={{ marginTop: 24 }}>
        Widget số liệu sẽ nối vào các API tồn kho / thông báo ở các giai đoạn sau.
      </Typography.Paragraph>
    </div>
  )
}
