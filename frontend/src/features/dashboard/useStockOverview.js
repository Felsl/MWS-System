import { useMemo } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { warehousesApi } from '../../api/warehouses.api'
import { inventoryApi } from '../../api/inventory.api'
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
 * GIỚI HẠN CỦA BE (đọc trước khi định mở rộng):
 *   - Không có endpoint tổng hợp nào. Phải gọi /inventory/warehouse/{id} cho
 *     từng kho rồi tự cộng ở FE.
 *   - InventoryResponse chỉ có {productId, warehouseId, quantity,
 *     reservedQuantity, availableQuantity} — KHÔNG có hạn dùng. Lô chỉ query
 *     được qua /inventory/batches?productId&warehouseId, tức mỗi cặp
 *     (sản phẩm, kho) một request. 210 sản phẩm x N kho => hàng trăm request.
 *     Vì vậy CHƯA có cảnh báo cận hạn ở Dashboard: cần BE thêm endpoint
 *     GET /inventory/batches/expiring?days=30.
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

  return { isLoading, isError, byWarehouse, lowStock, truncated, warehouseCount: whList.length }
}
