import api from './client'
const base = '/api/v1/adjustment-vouchers'
export const adjustmentsApi = {
  list: ({ keyword, status, page = 0, size = 20 } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  approve: (id) => api.post(`${base}/${id}/approve`).then(r => r.data),
}
