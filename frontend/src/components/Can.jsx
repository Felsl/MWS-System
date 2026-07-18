import { cloneElement, isValidElement } from 'react'
import { Button, Popconfirm, Tooltip } from 'antd'
import { useAuth } from '../auth/AuthContext'
import { permLabel } from '../constants/permissions'

/**
 * Kiểm soát hiển thị theo quyền.
 *
 * TRƯỚC: luôn ẩn tiệt. Người dùng mở trang thấy thiếu nút mà không hiểu vì sao —
 * không biết là do phân quyền, do lỗi, hay do phiếu sai trạng thái. Nhiều người
 * sẽ đi hỏi admin hoặc tưởng app hỏng.
 *
 * NAY: nếu con là <Button> hoặc <Popconfirm> thì KHOÁ nút lại + tooltip
 * "Cần quyền: Duyệt đơn mua hàng". Người dùng biết chức năng có tồn tại, biết
 * đang thiếu đúng quyền nào để đi xin. Đây chính là cách InventoryPage đã làm
 * sẵn ("* Cần quyền INVENTORY_ADJUST..."), nay áp cho toàn app.
 *
 * Con không phải nút (Tag, Space, cả một khối...) thì vẫn ẩn như cũ — khoá một
 * khối nội dung là vô nghĩa.
 *
 * props:
 *  - permission: mã quyền cần có. Không truyền => luôn hiện.
 *  - mode: 'auto' (mặc định) | 'disable' | 'hide' — ép hành vi khi cần.
 *  - fallback: node thay thế khi ẩn.
 *
 * LƯU Ý BẢO MẬT: đây chỉ là lớp giao diện. Khoá nút không thay cho @PreAuthorize
 * ở BE — người dùng vẫn có thể gọi thẳng API.
 */
export default function Can({ permission, children, fallback = null, mode = 'auto' }) {
  const { hasPermission } = useAuth()

  if (!permission) return children
  if (hasPermission(permission)) return children

  const resolved = mode === 'auto' ? (isLockable(children) ? 'disable' : 'hide') : mode
  if (resolved === 'hide' || !isLockable(children)) return fallback

  return (
    <Tooltip title={`Cần quyền: ${permLabel(permission)}`}>
      {/* Nút bị disabled không phát sự kiện chuột => Tooltip sẽ không bao giờ
          hiện nếu gắn thẳng vào nó. Bọc span là cách antd khuyến nghị. */}
      <span style={{ display: 'inline-block', cursor: 'not-allowed' }}>
        {lock(children)}
      </span>
    </Tooltip>
  )
}

/** Chỉ Button và Popconfirm mới khoá được. */
function isLockable(node) {
  return isValidElement(node) && (node.type === Button || node.type === Popconfirm)
}

/**
 * Khoá cây con.
 * Popconfirm cần disabled ở CẢ hai tầng: `disabled` trên Popconfirm chặn hộp xác
 * nhận bật lên, nhưng nút bên trong vẫn trông như bấm được nếu không khoá luôn.
 */
function lock(node) {
  if (node.type === Popconfirm) {
    return cloneElement(node, {
      disabled: true,
      children: isLockable(node.props.children) ? lock(node.props.children) : node.props.children,
    })
  }
  // Button: chặn thêm onClick phòng khi có style tuỳ biến làm nút vẫn bấm được.
  return cloneElement(node, { disabled: true, onClick: undefined })
}
