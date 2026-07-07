import api from './client'
// LƯU Ý: base path KHÔNG có /v1.
const base = '/api/stock-movements'
export const stockMovementsApi = {
  // Kardex theo sản phẩm — phân trang cursor: { content, nextCursor, hasNext }
  byProduct: ({ productId, warehouseId, cursor, size = 20 }) =>
    api.get(base, { params: { productId, warehouseId: warehouseId || undefined, cursor: cursor || undefined, size } }).then(r => r.data),
  // Truy vết theo chứng từ gốc
  byReference: (referenceType, referenceId) =>
    api.get(base, { params: { referenceType, referenceId } }).then(r => r.data),
}
