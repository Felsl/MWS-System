import { Empty, Button, Space } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import Can from './Can'

/**
 * Empty state có LỜI MỜI HÀNH ĐỘNG, thay cho <Empty> trống trơn.
 *
 * "Chưa có sản phẩm" đứng một mình là ngõ cụt: người dùng mới không biết bước
 * tiếp theo là gì, phải tự mò lên thanh công cụ tìm nút "Thêm". Empty state tốt
 * là một lời mời — nêu đúng một hành động chính ngay tại chỗ.
 *
 * Nút CTA bọc trong <Can> nên tự ẩn/khoá theo quyền: người chỉ-xem sẽ thấy mô
 * tả nhưng không thấy nút tạo (đúng, vì họ không tạo được).
 *
 * props:
 *  - title:   dòng mô tả (vd "Chưa có sản phẩm nào")
 *  - action:  { label, onClick, icon?, permission? } — nút chính
 *  - secondary: { label, onClick, icon? } — nút phụ tuỳ chọn (vd "Nhập từ Excel")
 *  - image:   'default' | 'simple' (mặc định simple cho gọn)
 */
export default function EmptyState({ title, action, secondary, image = 'simple' }) {
  const img = image === 'simple' ? Empty.PRESENTED_IMAGE_SIMPLE : Empty.PRESENTED_IMAGE_DEFAULT

  const buttons = (action || secondary) && (
    <Space wrap style={{ marginTop: 8 }}>
      {action && (
        <Can permission={action.permission}>
          <Button type="primary" icon={action.icon ?? <PlusOutlined />} onClick={action.onClick}>
            {action.label}
          </Button>
        </Can>
      )}
      {secondary && (
        <Button icon={secondary.icon} onClick={secondary.onClick}>{secondary.label}</Button>
      )}
    </Space>
  )

  return (
    <Empty image={img} description={title} style={{ padding: '24px 0' }}>
      {buttons}
    </Empty>
  )
}
