import { useCallback } from 'react'
import { useNavigate, useParams, useLocation } from 'react-router-dom'

/**
 * Đưa "đang xem bản ghi nào" vào URL thay vì useState.
 *
 * Trước đây các trang phiếu dùng:
 *     const [view, setView] = useState({ mode: 'list', id: null })
 * Hệ quả: mở 1 phiếu rồi F5 => về danh sách (mất chỗ đang làm); bấm Back của
 * trình duyệt => thoát thẳng khỏi trang chứ không quay về danh sách; và không
 * gửi link 1 phiếu cho người duyệt được.
 *
 * Nay trạng thái nằm ở URL:
 *     /transfer-orders          -> mode 'list'
 *     /transfer-orders/new      -> mode 'create'
 *     /transfer-orders/<uuid>   -> mode 'detail'
 *
 * API trả về giữ nguyên hình dạng cũ ({ mode, id }) nên phần thân trang gần như
 * không phải sửa gì.
 *
 * @param basePath ví dụ '/transfer-orders' (không có dấu / ở cuối)
 */
export function useRecordView(basePath) {
  const navigate = useNavigate()
  const location = useLocation()
  const { id } = useParams()

  const isCreate = location.pathname === `${basePath}/new`
  const mode = isCreate ? 'create' : (id ? 'detail' : 'list')

  const openList = useCallback(() => navigate(basePath), [navigate, basePath])
  const openCreate = useCallback(() => navigate(`${basePath}/new`), [navigate, basePath])
  // replace=true khi vừa tạo xong: người dùng bấm Back sẽ về danh sách, chứ
  // không quay lại form tạo rỗng của phiếu vừa tạo.
  const openDetail = useCallback(
    (recId, { replace = false } = {}) => navigate(`${basePath}/${recId}`, { replace }),
    [navigate, basePath],
  )

  return { mode, id: isCreate ? null : (id || null), openList, openCreate, openDetail }
}
