import { useCallback, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'

/**
 * Đưa bộ lọc danh sách (từ khoá / trạng thái / trang / cỡ trang / sắp xếp) vào
 * query string thay vì useState.
 *
 * Lợi ích thực tế:
 *  - F5 không mất bộ lọc đang xem.
 *  - Gửi được link "danh sách PO chờ duyệt" cho người khác.
 *  - Nút Back của trình duyệt lùi lại bộ lọc trước đó.
 *  - Dashboard chỉ cần điều hướng tới `/purchase-orders?status=PENDING_APPROVAL`
 *    là trang tự lọc — không cần location.state (state không sống qua F5).
 *
 * Trả về giá trị + các setter đã tự reset về trang 0 khi đổi điều kiện lọc.
 */
export function useListParams({ defaultSize = 20 } = {}) {
  const [sp, setSp] = useSearchParams()

  const keyword = sp.get('q') || ''
  const status = sp.get('status') || undefined
  const page = Number(sp.get('page') || 0)
  const size = Number(sp.get('size') || defaultSize)
  const sort = sp.get('sort') || undefined
  const dir = sp.get('dir') || undefined

  // Gộp mọi thay đổi qua 1 chỗ: bỏ key rỗng cho URL sạch, giữ nguyên các key lạ.
  const patch = useCallback((changes, { resetPage = true } = {}) => {
    setSp((prev) => {
      const next = new URLSearchParams(prev)
      const all = resetPage ? { page: 0, ...changes } : changes
      Object.entries(all).forEach(([k, v]) => {
        if (v === undefined || v === null || v === '' || (k === 'page' && Number(v) === 0)) next.delete(k)
        else next.set(k, String(v))
      })
      return next
    }, { replace: true })
  }, [setSp])

  const setKeyword = useCallback((v) => patch({ q: v }), [patch])
  const setStatus = useCallback((v) => patch({ status: v }), [patch])
  const setPager = useCallback((p, s) => patch({ page: p, size: s === defaultSize ? undefined : s }, { resetPage: false }), [patch, defaultSize])

  // antd sorter -> query. Giữ đúng hình dạng mà utils/sort.js đang dùng.
  const sorter = useMemo(
    () => (sort ? { field: sort, order: dir === 'desc' ? 'descend' : 'ascend' } : null),
    [sort, dir],
  )
  const setSorter = useCallback((s) => {
    if (!s || !s.order) patch({ sort: undefined, dir: undefined })
    else patch({ sort: s.field, dir: s.order === 'descend' ? 'desc' : 'asc' })
  }, [patch])

  return { keyword, status, page, size, sort, dir, sorter, setKeyword, setStatus, setPager, setSorter }
}
