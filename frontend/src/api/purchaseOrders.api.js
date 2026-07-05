import api from './client'
const base = '/api/v1/purchase-orders'
// LƯU Ý: BE không có endpoint list -> chỉ thao tác theo id.
export const purchaseOrdersApi = {
  create: (body) => api.post(base, body).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  submitReview: (id) => api.post(`${base}/${id}/submit-review`).then(r => r.data),
  submitApproval: (id) => api.post(`${base}/${id}/submit-approval`).then(r => r.data),
  approve: (id) => api.post(`${base}/${id}/approve`).then(r => r.data),
  reject: (id) => api.post(`${base}/${id}/reject`).then(r => r.data),
}
