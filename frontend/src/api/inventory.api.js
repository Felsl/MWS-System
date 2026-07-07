import api from './client'
const base = '/api/v1/inventory'
export const inventoryApi = {
  getByWarehouse: (warehouseId) => api.get(`${base}/warehouse/${warehouseId}`).then(r => r.data),
  getOne: (productId, warehouseId) => api.get(base, { params: { productId, warehouseId } }).then(r => r.data),
  getBatches: (productId, warehouseId) => api.get(`${base}/batches`, { params: { productId, warehouseId } }).then(r => r.data),
  updateBatchStatus: (batchId, status) =>
    api.patch(`${base}/batches/${batchId}/status`, { status }).then(r => r.data),
}
