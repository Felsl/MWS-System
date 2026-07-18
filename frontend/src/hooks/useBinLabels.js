import { useMemo } from 'react'
import { useQuery, useQueries } from '@tanstack/react-query'
import { warehousesApi } from '../api/warehouses.api'

/**
 * Nạp bin-locations của tất cả kho mà user được phép thấy và dựng map
 * binLocationId -> mã toạ độ thân thiện dạng zone-aisle-rack-bin (VD: A-01-R1-B3).
 *
 * Dùng chung cho mọi trang cần hiển thị ô kệ. Các query ['bins', whId] được
 * React Query cache & dedupe với những nơi khác đã fetch bins cùng kho.
 */
export function useBinLabels() {
  const warehouses = useQuery({
    queryKey: ['warehouses', 'active'],
    queryFn: () => warehousesApi.list(false),
  })
  const ids = useMemo(() => (warehouses.data || []).map(w => w.id), [warehouses.data])

  const results = useQueries({
    queries: ids.map(id => ({
      queryKey: ['bins', id],
      queryFn: () => warehousesApi.listBins(id),
    })),
  })

  const map = useMemo(() => {
    const m = {}
    results.forEach(r => (r.data || []).forEach(b => { m[b.id] = b.coordinateLabel || b.id }))
    return m
  }, [results])

  // Trả nhãn ô kệ; nếu chưa nạp được thì fallback về id (không vỡ giao diện).
  const labelOf = (id, fallback = '—') => (id ? (map[id] || id) : fallback)

  return { map, labelOf, loading: warehouses.isLoading || results.some(r => r.isLoading) }
}
