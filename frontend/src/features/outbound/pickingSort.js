/** Ưu tiên hiển thị: đang lấy trước (việc đang làm), rồi chờ lấy, cuối là xong. */
export const PL_STATUS_ORDER = { PICKING: 0, PENDING: 1, COMPLETED: 2 }

const rank = (s) => (PL_STATUS_ORDER[s] ?? 99)

/**
 * Sắp lệnh lấy hàng theo trạng thái, trong mỗi nhóm thì mới nhất lên trước.
 * Trả về MẢNG MỚI (không sửa mảng gốc của React Query cache).
 */
export function sortPickingLists(rows = []) {
  return [...rows].sort((a, b) => {
    const d = rank(a?.status) - rank(b?.status)
    if (d !== 0) return d
    const ta = a?.startedAt ? Date.parse(a.startedAt) : 0
    const tb = b?.startedAt ? Date.parse(b.startedAt) : 0
    if (tb !== ta) return tb - ta
    return String(b?.pickNumber || '').localeCompare(String(a?.pickNumber || ''))
  })
}
