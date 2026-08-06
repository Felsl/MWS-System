import api from './client'
// LƯU Ý: base path KHÔNG có /v1 (khác picking-lists).
const base = '/api/sales-orders'
export const salesOrdersApi = {
  list: ({ keyword, status, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword: keyword || undefined, status: status || undefined, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  /** Lặp hết các trang để tra cứu id -> soNumber. Mirror productsApi.listAll. */
  listAll: async ({ pageSize = 500, hardCap = 20 } = {}) => {
    const first = await salesOrdersApi.list({ page: 0, size: pageSize })
    const totalPages = first.totalPages ?? 1
    if (totalPages <= 1) return first.content || []
    const pages = Math.min(totalPages, hardCap)
    const rest = await Promise.all(
      Array.from({ length: pages - 1 }, (_, i) => salesOrdersApi.list({ page: i + 1, size: pageSize })),
    )
    const all = [first, ...rest].flatMap(r => r.content || [])
    if (totalPages > hardCap) {
      console.warn(`[MWS] salesOrders.listAll: ${totalPages} trang > hardCap ${hardCap}, đã cắt bớt.`)
    }
    return all
  },
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  allocate: (id) => api.post(`${base}/${id}/allocate`).then(r => r.data),
  cancel: (id) => api.post(`${base}/${id}/cancel`).then(r => r.data),
}
