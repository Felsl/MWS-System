import api from './client'
const base = '/api/v1/users'
export const usersApi = {
  list: () => api.get(base).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
  getWarehouses: (id) => api.get(`${base}/${id}/warehouses`).then(r => r.data),
  assignWarehouses: (id, warehouseIds) =>
    api.put(`${base}/${id}/warehouses`, { warehouseIds }).then(r => r.data),
}
