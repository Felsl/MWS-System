import api from './client'
const base = '/api/v1/inventory'
export const inventoryApi = {
  getByWarehouse: (warehouseId) => api.get(`${base}/warehouse/${warehouseId}`).then(r => r.data),
  availableBySupplier: (productId, warehouseId) =>
    api.get(`${base}/available-by-supplier`, { params: { productId, warehouseId } }).then(r => r.data),
  // [Bán theo NCC] sản phẩm bán được trong kho (lô ACTIVE & chưa hết hạn)
  sellableByWarehouse: (warehouseId) =>
    api.get(`${base}/sellable-by-warehouse`, { params: { warehouseId } }).then(r => r.data),
  getOne: (productId, warehouseId) => api.get(base, { params: { productId, warehouseId } }).then(r => r.data),
  getBatches: (productId, warehouseId) => api.get(`${base}/batches`, { params: { productId, warehouseId } }).then(r => r.data),
  // [MỤC 6] Lô còn hàng sắp hết hạn (expiry_date <= hôm nay + days). warehouseId tuỳ chọn.
  getExpiringBatches: ({ days = 30, warehouseId } = {}) =>
    api.get(`${base}/batches/expiring`, { params: { days, warehouseId: warehouseId || undefined } }).then(r => r.data),
  // Gợi ý lô theo FEFO cho 1 sản phẩm tại 1 kho với số lượng cần
  allocateBatches: (productId, warehouseId, quantity) =>
    api.get(`${base}/batches/allocate`, { params: { productId, warehouseId, quantity } }).then(r => r.data),
  updateBatchStatus: (batchId, status) =>
    api.patch(`${base}/batches/${batchId}/status`, { status }).then(r => r.data),
}
