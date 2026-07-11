import api from './client'
const base = '/api/v1/stocktakes'
export const stocktakesApi = {
  // list trả PageResponse<StocktakeSessionResponse> (chỉ lọc status)
  list: ({ status, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { status: status || undefined, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),          // -> { session, details }
  start: (warehouseId) => api.post(base, { warehouseId }).then(r => r.data),
  count: (detailId, countedQuantity) =>
    api.post(`${base}/details/${detailId}/count`, { countedQuantity }).then(r => r.data),
  approveLine: (detailId, reason) =>
    api.post(`${base}/details/${detailId}/approve-line`, reason ? { reason } : {}).then(r => r.data),
  complete: (id) => api.post(`${base}/${id}/complete`).then(r => r.data), // -> { session, details, voucher }
}
