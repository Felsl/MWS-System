import api from './client'
const base = '/api/v1/picking-lists'
export const pickingListsApi = {
  list: () => api.get(base).then(r => r.data),          // List thuần (không phân trang)
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (soId) => api.post(base, { soId }).then(r => r.data),
  assign: (id, userId) => api.post(`${base}/${id}/assign`, { userId }).then(r => r.data),
  confirm: (detailId, body) => api.post(`${base}/details/${detailId}/confirm`, body).then(r => r.data),
  reportShort: (detailId, body) => api.post(`${base}/details/${detailId}/short`, body).then(r => r.data),
  candidateBatches: (detailId) => api.get(`${base}/details/${detailId}/candidate-batches`).then(r => r.data),
  complete: (id) => api.post(`${base}/${id}/complete`).then(r => r.data),
}
