import api from './client'
const base = '/api/v1/products'
export const productsApi = {
  // Trả PageResponse { content, page, size, totalElements, totalPages, hasNext }
  list: ({ keyword, page = 0, size = 20, sort, dir } = {}) =>
    api.get(base, { params: { keyword, page, size, sort: sort || undefined, dir: dir || undefined } }).then(r => r.data),
  /**
   * Lấy TOÀN BỘ sản phẩm bằng cách lặp qua các trang cho tới hết.
   *
   * Trước đây 10 chỗ trong FE gọi `list({ size: 500 })` để dựng map id -> tên.
   * Đó là bom hẹn giờ: BE trả tối đa 500 dòng, nên sản phẩm thứ 501 trở đi sẽ
   * KHÔNG có trong map và giao diện lặng lẽ hiển thị UUID thay vì tên — không
   * lỗi, không cảnh báo, chỉ sai. Dữ liệu demo đã có 210 sản phẩm.
   *
   * `hardCap` chặn vòng lặp vô hạn nếu BE trả totalPages sai.
   */
  listAll: async ({ pageSize = 500, hardCap = 20 } = {}) => {
    const first = await productsApi.list({ page: 0, size: pageSize })
    const totalPages = first.totalPages ?? 1
    if (totalPages <= 1) return first.content || []
    const pages = Math.min(totalPages, hardCap)
    const rest = await Promise.all(
      Array.from({ length: pages - 1 }, (_, i) => productsApi.list({ page: i + 1, size: pageSize })),
    )
    const all = [first, ...rest].flatMap(r => r.content || [])
    if (totalPages > hardCap) {
      console.warn(`[MWS] products.listAll: ${totalPages} trang > hardCap ${hardCap}, đã cắt bớt.`)
    }
    return all
  },
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
}
