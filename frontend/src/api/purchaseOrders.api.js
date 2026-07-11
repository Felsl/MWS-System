import api from './client'
const base = '/api/v1/purchase-orders'
export const purchaseOrdersApi = {
  // PageResponse { content, page, size, totalElements, totalPages, hasNext }
  list: ({ keyword, status, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  submitReview: (id) => api.post(`${base}/${id}/submit-review`).then(r => r.data),
  submitApproval: (id) => api.post(`${base}/${id}/submit-approval`).then(r => r.data),
  approve: (id) => api.post(`${base}/${id}/approve`).then(r => r.data),
  reject: (id) => api.post(`${base}/${id}/reject`).then(r => r.data),
}
