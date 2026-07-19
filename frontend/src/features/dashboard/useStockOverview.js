import { useMemo } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import dayjs from 'dayjs'
import { warehousesApi } from '../../api/warehouses.api'
import { inventoryApi } from '../../api/inventory.api'
import { reportsApi } from '../../api/reports.api'
import { useProducts } from '../../hooks/useProducts'

// Chặn fan-out: BE chỉ có GET /inventory/warehouse/{id} nên phải gọi 1 lần/kho.
// Vài kho thì không sao; kho nhiều bất thường thì dừng lại còn hơn bắn 50 request.
const MAX_WAREHOUSES = 12

/**
 * Gom tồn kho toàn hệ thống cho Dashboard.
 *
 * MỘT lần lấy dữ liệu phục vụ CẢ hai thứ: biểu đồ tồn theo kho và danh sách
 * cảnh báo dưới tồn an toàn — không gọi API thêm lần nào cho biểu đồ.
 *
 * Tồn theo kho vẫn phải fan-out /inventory/warehouse/{id} cho từng kho (BE
 * chưa có endpoint gộp tồn). Riêng cận hạn và Xuất-Nhập-Tồn nay đã có endpoint
 * gộp riêng (mỗi thứ 1 request), lấy kèm ở dưới.
 */
export function useStockOverview({ enabled = true } = {}) {
  const warehouses = useQuery({
    queryKey: ['warehouses', 'active'],
    queryFn: () => warehousesApi.list(false),
    enabled,
  })

  const whList = useMemo(() => (warehouses.data || []).slice(0, MAX_WAREHOUSES), [warehouses.data])
  const truncated = (warehouses.data || []).length > MAX_WAREHOUSES

  const stockQueries = useQueries({
    queries: whList.map(w => ({
      queryKey: ['inventory', 'warehouse', w.id],
      queryFn: () => inventoryApi.getByWarehouse(w.id),
      enabled: enabled && !!w.id,
      staleTime: 60_000,
    })),
  })

  const { map: productMap, isLoading: productsLoading } = useProducts()

  const isLoading = warehouses.isLoading || productsLoading || stockQueries.some(q => q.isLoading)
  const isError = warehouses.isError || stockQueries.some(q => q.isError)

  // Không useMemo: deps sẽ phải là mảng useQueries (đổi tham chiếu mỗi lần
  // render) nên memo hoá vừa vô tác dụng vừa phải bịa key. Vài kho x vài trăm
  // dòng thì cộng thẳng còn rẻ hơn chi phí so sánh deps.
  // Tổng tồn theo từng kho -> dữ liệu biểu đồ.
  const byWarehouse = whList.map((w, i) => {
    const rows = stockQueries[i]?.data || []
    return {
      id: w.id,
      name: w.name || w.code || w.id,
      available: rows.reduce((sum, r) => sum + (r.availableQuantity || 0), 0),
      reserved: rows.reduce((sum, r) => sum + (r.reservedQuantity || 0), 0),
      skus: rows.length,
    }
  })

  // Dòng nào tồn khả dụng < mức an toàn của sản phẩm.
  const lowStock = (() => {
    const out = []
    whList.forEach((w, i) => {
      (stockQueries[i]?.data || []).forEach(r => {
        const p = productMap[r.productId]
        const safety = p?.safetyStock
        if (safety == null || safety <= 0) return         // 0 = không đặt ngưỡng
        if ((r.availableQuantity ?? 0) >= safety) return
        out.push({
          key: `${w.id}:${r.productId}`,
          productId: r.productId,
          productName: p?.name || r.productId,
          sku: p?.sku,
          warehouseId: w.id,
          warehouseName: w.name || w.code,
          available: r.availableQuantity ?? 0,
          safety,
          deficit: safety - (r.availableQuantity ?? 0),
        })
      })
    })
    // Thiếu nhiều nhất lên trước — đó là thứ cần mua bù trước.
    return out.sort((a, b) => b.deficit - a.deficit)
  })()

  // ── [MỤC 6] Hai thẻ mới: cận hạn + Xuất-Nhập-Tồn — mỗi thứ 1 request gộp,
  // độc lập với fan-out ở trên nên KHÔNG chặn nhau khi tải.
  const EXPIRY_DAYS = 30
  const expiring = useQuery({
    queryKey: ['dashboard', 'expiring', EXPIRY_DAYS],
    queryFn: () => inventoryApi.getExpiringBatches({ days: EXPIRY_DAYS }),
    enabled,
    staleTime: 60_000,
  })

  const SUMMARY_DAYS = 30
  const range = useMemo(() => {
    const to = dayjs()
    return { from: to.subtract(SUMMARY_DAYS - 1, 'day').format('YYYY-MM-DD'), to: to.format('YYYY-MM-DD') }
  }, [])
  const summary = useQuery({
    queryKey: ['dashboard', 'stock-summary', range.from, range.to],
    queryFn: () => reportsApi.stockSummary(range),
    enabled,
    staleTime: 60_000,
  })

  return {
    isLoading, isError, byWarehouse, lowStock, truncated, warehouseCount: whList.length,
    expiring: { data: expiring.data || [], isLoading: expiring.isLoading, isError: expiring.isError, days: EXPIRY_DAYS },
    summary: { data: summary.data || [], isLoading: summary.isLoading, isError: summary.isError, days: SUMMARY_DAYS },
  }
}
