import api from './client'
// LƯU Ý: base path KHÔNG có /v1 (khác picking-lists).
const base = '/api/sales-orders'
export const salesOrdersApi = {
  list: ({ keyword, status, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  allocate: (id) => api.post(`${base}/${id}/allocate`).then(r => r.data),
  cancel: (id) => api.post(`${base}/${id}/cancel`).then(r => r.data),
}
