import api from './client'
const base = '/api/v1/goods-receipts'
export const goodsReceiptsApi = {
  // PageResponse { content, page, size, totalElements, totalPages, hasNext }
  list: ({ keyword, status, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  complete: (id) => api.post(`${base}/${id}/complete`).then(r => r.data),
  updateDetailDates: (grnId, detailId, body) =>
    api.patch(`${base}/${grnId}/details/${detailId}/dates`, body).then(r => r.data),
}
