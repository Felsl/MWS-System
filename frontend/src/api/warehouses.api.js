import api from './client'
const base = '/api/v1/warehouses'
export const warehousesApi = {
  list: (adminView = false) => api.get(base, { params: { adminView } }).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
  listBins: (id) => api.get(`${base}/${id}/bin-locations`).then(r => r.data),
  bulkGenerateBins: (id, zones) =>
    api.post(`${base}/${id}/bin-locations/bulk`, { zones }).then(r => r.data),
  deleteBin: (id, binId) =>
    api.delete(`${base}/${id}/bin-locations/${binId}`).then(r => r.data),
}
