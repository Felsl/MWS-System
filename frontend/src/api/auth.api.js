import api from './client'
export const authApi = {
  login: (username, password) =>
    api.post('/api/v1/auth/login', { username, password }).then(r => r.data),
  me: () => api.get('/api/v1/auth/me').then(r => r.data),
  logout: () => api.post('/api/v1/auth/logout').then(r => r.data),
  changePassword: (oldPassword, newPassword) =>
    api.post('/api/v1/auth/change-password', { oldPassword, newPassword }).then(r => r.data),
}
