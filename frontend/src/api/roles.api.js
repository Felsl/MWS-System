import api from './client'
const base = '/api/v1/roles'
export const rolesApi = {
  list: () => api.get(base).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
  assignPermissions: (id, permissionIds) =>
    api.put(`${base}/${id}/permissions`, { permissionIds }).then(r => r.data),
}
