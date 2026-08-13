import api from './client'

// [Bán vượt tồn] Nhu cầu nhập (backorder) đang OPEN — cho card Dashboard bộ phận mua.
export const stockDemandsApi = {
  open: () => api.get('/api/v1/stock-demands').then(r => r.data),
}
