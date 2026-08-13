/**
 * Nhãn trạng thái đơn bán.
 *
 * Tách khỏi SalesOrdersPage.jsx vì eslint `react-refresh/only-export-components`
 * cấm file chứa component export thêm hằng/hàm.
 */
export const SO_STATUS = {
  DRAFT: { color: 'default', label: 'Nháp' },
  ALLOCATED: { color: 'blue', label: 'Đã phân bổ' },
  PARTIALLY_ALLOCATED: { color: 'orange', label: 'Phân bổ một phần' },
  PICKING: { color: 'gold', label: 'Đang lấy hàng' },
  SHIPPED: { color: 'green', label: 'Đã xuất' },
  CANCELLED: { color: 'red', label: 'Đã huỷ' },
}

/** Chỉ các trạng thái CÓ THẬT mới lên bộ lọc (nhãn "đã lấy xong" không phải trạng thái). */
export const SO_STATUS_OPTS = Object.entries(SO_STATUS).map(([value, m]) => ({ value, label: m.label }))

/**
 * Nhãn hiển thị cho một đơn bán.
 *
 * `picked` = lệnh lấy hàng của đơn này đã COMPLETED. Khi đó đơn vẫn ở trạng thái
 * PICKING trong CSDL (backend không đổi), nhưng để nhân viên khỏi tưởng còn đang
 * lấy dở thì hiển thị "Hoàn thành lấy hàng" — bước tiếp theo là tạo vận đơn.
 */
export function soStatusMeta(status, picked = false) {
  if (status === 'PICKING' && picked) {
    return { color: 'cyan', label: 'Hoàn thành lấy hàng' }
  }
  return SO_STATUS[status] || { color: 'default', label: status }
}
