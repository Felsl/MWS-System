import api from './client'
const make = (base) => ({
  list: () => api.get(base).then(r => r.data),
  get: (id) => api.get(`${base}/${id}`).then(r => r.data),
  create: (body) => api.post(base, body).then(r => r.data),
  update: (id, body) => api.put(`${base}/${id}`, body).then(r => r.data),
  remove: (id) => api.delete(`${base}/${id}`).then(r => r.data),
})
export const suppliersApi = make('/api/v1/suppliers')
export const customersApi = make('/api/v1/customers')
// Carrier ở BE chỉ có POST + GET (không update/delete)
export const carriersApi = {
  list: () => api.get('/api/v1/carriers').then(r => r.data),
  create: (body) => api.post('/api/v1/carriers', body).then(r => r.data),
}
