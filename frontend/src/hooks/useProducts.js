import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi } from '../api/products.api'

/**
 * Nguồn duy nhất cho danh mục sản phẩm dùng để tra cứu id -> tên/SKU.
 *
 * Thay cho đoạn `productsApi.list({ size: 500 })` từng bị chép tay ở 10 file:
 *  - `listAll()` lặp hết các trang => KHÔNG vỡ khi vượt 500 sản phẩm.
 *  - Chung một queryKey ['products','all'] => React Query dedupe, 10 trang dùng
 *    lại cùng một kết quả đã cache thay vì mỗi trang tự gọi lại.
 *  - staleTime 5 phút: danh mục sản phẩm gần như tĩnh trong một phiên làm việc.
 *
 * `nameOf`/`skuOf` fallback về chính id để giao diện không vỡ khi chưa nạp xong.
 */
export function useProducts() {
  const query = useQuery({
    queryKey: ['products', 'all'],
    queryFn: () => productsApi.listAll(),
    staleTime: 5 * 60_000,
  })

  const list = useMemo(() => query.data || [], [query.data])
  const map = useMemo(() => Object.fromEntries(list.map(p => [p.id, p])), [list])

  const nameOf = (id) => map[id]?.name || id || '—'
  const skuOf = (id) => map[id]?.sku || '—'
  // Dùng cho <Select> chọn sản phẩm: nhãn "Tên · SKU".
  const options = useMemo(() => list.map(p => ({ value: p.id, label: `${p.name} · ${p.sku}` })), [list])

  return { query, list, map, options, nameOf, skuOf, isLoading: query.isLoading }
}
