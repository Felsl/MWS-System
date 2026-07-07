import api from './client'
const base = '/api/v1/transfer-orders'
export const transferOrdersApi = {
  list: ({ keyword, status, page = 0, size = 20 } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  requestApproval: (id) => api.post(`${base}/${id}/request-approval`).then(r => r.data),
  approve: (id, approvedBy) => api.post(`${base}/${id}/approve`, { approvedBy }).then(r => r.data),
  reject: (id, rejectedBy) => api.post(`${base}/${id}/reject`, { rejectedBy }).then(r => r.data),
  cancel: (id) => api.post(`${base}/${id}/cancel`).then(r => r.data),
  dispatch: (id, carrierId) => api.post(`${base}/${id}/dispatch`, { carrierId }).then(r => r.data), // -> ShipmentResponse
  complete: (id, lines) => api.post(`${base}/${id}/complete`, { lines }).then(r => r.data),
}
