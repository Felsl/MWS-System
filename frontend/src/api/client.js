import axios from 'axios'

// Base URL: rỗng khi dev (đi qua Vite proxy /api). Prod đặt VITE_API_BASE_URL.
const baseURL = import.meta.env.VITE_API_BASE_URL || ''

export const TOKEN_KEY = 'mws_access_token'
export const REFRESH_KEY = 'mws_refresh_token'

export const tokenStore = {
  get access() { return localStorage.getItem(TOKEN_KEY) },
  get refresh() { return localStorage.getItem(REFRESH_KEY) },
  set({ accessToken, refreshToken }) {
    if (accessToken) localStorage.setItem(TOKEN_KEY, accessToken)
    if (refreshToken) localStorage.setItem(REFRESH_KEY, refreshToken)
  },
  clear() { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(REFRESH_KEY) },
}

const api = axios.create({ baseURL, headers: { 'Content-Type': 'application/json' } })

api.interceptors.request.use((config) => {
  const t = tokenStore.access
  if (t) config.headers.Authorization = `Bearer ${t}`
  return config
})

// ---- Tự refresh khi gặp 401 (hàng đợi tránh gọi refresh nhiều lần) ----
let refreshing = null
let onLogout = () => { tokenStore.clear(); window.location.href = '/login' }
export function setLogoutHandler(fn) { onLogout = fn }

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const { config, response } = error
    if (!response) return Promise.reject(error) // lỗi mạng
    const isAuthCall = config?.url?.includes('/api/v1/auth/')

    if (response.status === 401 && !config._retried && !isAuthCall) {
      config._retried = true
      try {
        if (!refreshing) {
          const rt = tokenStore.refresh
          if (!rt) throw new Error('no refresh token')
          refreshing = axios
            .post(`${baseURL}/api/v1/auth/refresh`, { refreshToken: rt })
            .then((r) => { tokenStore.set(r.data); return r.data.accessToken })
            .finally(() => { refreshing = null })
        }
        const newToken = await refreshing
        config.headers.Authorization = `Bearer ${newToken}`
        return api(config)
      } catch (e) {
        onLogout()
        return Promise.reject(e)
      }
    }
    return Promise.reject(error)
  }
)

// Chuẩn hóa message lỗi vì BE gộp lỗi field vào 1 chuỗi "message".
export function getErrorMessage(err, fallback = 'Đã có lỗi xảy ra') {
  return err?.response?.data?.message || err?.message || fallback
}

export default api
