import api from './client'
const base = '/api/v1/goods-receipts'
// LƯU Ý: BE không có endpoint list -> chỉ thao tác theo id.
export const goodsReceiptsApi = {
  create: (body) => api.post(base, body).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  complete: (id) => api.post(`${base}/${id}/complete`).then(r => r.data),
}
