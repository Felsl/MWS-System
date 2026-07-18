import { Typography, Space, Grid } from 'antd'

/**
 * Tiêu đề trang dùng chung.
 *
 * TRƯỚC: mỗi trang tự <Typography.Title level={4}> với style khác nhau —
 * `margin: 0` chỗ này, `margin: '0 0 16px'` chỗ kia, và 6 trang phiếu còn
 * `marginLeft: 20` lệch hẳn sang phải so với phần còn lại. Tiêu đề cùng cấp mà
 * nhảy vị trí mỗi khi chuyển trang trông rất nghiệp dư.
 *
 * NAY: một khoảng cách nhất quán, chừa sẵn chỗ cho nút hành động bên phải.
 *
 * props:
 *  - title:   chuỗi hoặc node
 *  - subtitle: mô tả phụ (tuỳ chọn), xám, nhỏ, nằm dưới tiêu đề
 *  - extra:   node căn phải (nút "Thêm", "Làm mới"...) — tự xuống hàng ở mobile
 *  - onBack:  nếu có, render nút "‹ Danh sách" trước tiêu đề (trang chi tiết)
 */
export default function PageHeader({ title, subtitle, extra, onBack }) {
  const screens = Grid.useBreakpoint()
  const stack = !screens.sm   // màn rất hẹp: xếp dọc cho khỏi vỡ

  return (
    <div style={{
      display: 'flex', flexDirection: stack ? 'column' : 'row',
      alignItems: stack ? 'stretch' : 'center', justifyContent: 'space-between',
      gap: 8, marginBottom: 16,
    }}>
      <div style={{ minWidth: 0 }}>
        <Space size={8} align="center">
          {onBack}
          <Typography.Title level={4} style={{ margin: 0 }} ellipsis>{title}</Typography.Title>
        </Space>
        {subtitle && (
          <Typography.Text type="secondary" style={{ fontSize: 13 }}>{subtitle}</Typography.Text>
        )}
      </div>
      {extra && <Space wrap>{extra}</Space>}
    </div>
  )
}
