import { useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { salesOrdersApi } from '../api/salesOrders.api'
import { pickingListsApi } from '../api/pickingLists.api'

/**
 * Tra cứu id đơn bán -> mã đơn (soNumber).
 *
 * API lệnh lấy hàng (PickingListResponse) chỉ trả `soId` chứ KHÔNG có soNumber,
 * nên các trang picking phải tự ghép ở phía client. Dùng chung queryKey
 * ['sales-orders','all'] để React Query dedupe giữa các trang.
 *
 * `numberOf` fallback về chính id để giao diện không vỡ khi chưa nạp xong.
 */
export function useSalesOrderNumbers() {
  const query = useQuery({
    queryKey: ['sales-orders', 'all'],
    queryFn: () => salesOrdersApi.listAll(),
    staleTime: 60_000,
  })
  const list = useMemo(() => query.data || [], [query.data])
  const map = useMemo(() => Object.fromEntries(list.map(o => [o.id, o.soNumber])), [list])
  const numberOf = (id, fallback = '—') => (id ? (map[id] || id) : fallback)
  return { map, numberOf, isLoading: query.isLoading }
}

/**
 * Tra cứu soId -> đã lấy hàng xong hay chưa.
 *
 * Backend KHÔNG đổi trạng thái SO khi hoàn thành lệnh lấy (PickingDomainService
 * .complete chỉ đụng PickingList), nên SO nằm mãi ở PICKING tới lúc xuất hàng.
 * Trang Đơn bán dựa vào map này để đổi NHÃN hiển thị, không đụng dữ liệu.
 *
 * Một SO có thể có nhiều lệnh lấy (tạo lại sau khi huỷ) nên COMPLETED được ưu
 * tiên: chỉ cần một lệnh đã hoàn thành là coi như đã lấy xong.
 */
export function usePickingStatusBySo() {
  const query = useQuery({ queryKey: ['pl-list'], queryFn: pickingListsApi.list })
  const map = useMemo(() => {
    const m = {}
    for (const pl of query.data || []) {
      if (!pl?.soId) continue
      if (pl.status === 'COMPLETED' || !m[pl.soId]) m[pl.soId] = pl.status
    }
    return m
  }, [query.data])
  const isPicked = (soId) => map[soId] === 'COMPLETED'
  return { map, isPicked, isLoading: query.isLoading }
}
