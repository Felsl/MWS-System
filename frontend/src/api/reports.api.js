import api from './client'
const base = '/api/v1/reports'
export const reportsApi = {
  // [MỤC 6] Xuất-Nhập-Tồn theo ngày: [{ date, inQty, outQty, closingQty }]
  stockSummary: ({ from, to, warehouseId } = {}) =>
    api.get(`${base}/stock-summary`, { params: { from, to, warehouseId: warehouseId || undefined } }).then(r => r.data),
}
