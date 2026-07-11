import api from './client'
const base = '/api/v1/products'
export const productsApi = {
  // Trả PageResponse { content, page, size, totalElements, totalPages, hasNext }
  list: ({ keyword, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
}
