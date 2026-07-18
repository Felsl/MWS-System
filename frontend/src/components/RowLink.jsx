import { Button } from 'antd'

/**
 * Link "hành động trong trang" (mở chi tiết trong cùng SPA, không đổi URL) —
 * thay cho <a onClick> không có href.
 *
 * Vì sao đừng dùng <a onClick> trần:
 *  - không có href => KHÔNG Tab tới được bằng bàn phím, screen reader bỏ qua,
 *    con trỏ không thành hình bàn tay ở một số trình duyệt.
 *  - antd cũng cảnh báo dùng <Button type="link"> cho hành động, <a> cho điều
 *    hướng thật.
 *
 * Dùng cho các ô "Mã đơn / Mã phiếu" trong bảng: bấm để mở chi tiết. Trông vẫn
 * y như link (type="link", padding 0, cao bằng dòng) nhưng là <button> thật nên
 * focus/keyboard/aria đều đúng.
 *
 * KHI NÀO KHÔNG dùng cái này: nếu chỗ đó điều hướng tới một URL có thật
 * (/stock-movements?...), hãy dùng <Link> của react-router để mở-tab-mới hoạt
 * động — xem cách InventoryPage đã đổi.
 */
export default function RowLink({ onClick, children, ...rest }) {
  return (
    <Button
      type="link"
      onClick={onClick}
      style={{ padding: 0, height: 'auto', lineHeight: 'inherit', ...(rest.style || {}) }}
      {...rest}>
      {children}
    </Button>
  )
}
