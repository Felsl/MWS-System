import api from './client'
// LƯU Ý: base path KHÔNG có /v1.
const base = '/api/shipments'
export const shipmentsApi = {
  list: () => api.get(base).then(r => r.data),          // List thuần
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (salesOrderId, carrierId) => api.post(base, { salesOrderId, carrierId: carrierId || null }).then(r => r.data),
  assignTracking: (id, carrierId, trackingNumber) =>
    api.post(`${base}/${id}/tracking`, { carrierId, trackingNumber }).then(r => r.data),
  ship: (id, warehouseId, actorUserId) =>
    api.post(`${base}/${id}/ship`, { warehouseId, actorUserId }).then(r => r.data),
  deliver: (id) => api.post(`${base}/${id}/deliver`).then(r => r.data),
}
