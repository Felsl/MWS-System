import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react'
import { App as AntdApp } from 'antd'
import { authApi } from '../api/auth.api'
import { tokenStore, setLogoutHandler, getErrorMessage } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const { message } = AntdApp.useApp()
  const [user, setUser] = useState(null)      // { userId, username, fullName, role, permissions[] }
  const [loading, setLoading] = useState(true)

  const logout = useCallback((silent = false) => {
    tokenStore.clear()
    localStorage.removeItem('mws_perms')
    setUser(null)
    if (!silent) message.info('Đã đăng xuất')
    // Điều hướng do ProtectedRoute lo (redirect về /login)
  }, [message])

  // Cho phép interceptor axios gọi logout khi refresh thất bại.
  useEffect(() => { setLogoutHandler(() => logout(true)) }, [logout])

  // Khôi phục phiên khi F5: có token thì gọi /me.
  useEffect(() => {
    let alive = true
    async function boot() {
      if (!tokenStore.access) { setLoading(false); return }
      try {
        const me = await authApi.me()
        if (alive) setUser(normalizeMe(me))
      } catch {
        tokenStore.clear()
      } finally {
        if (alive) setLoading(false)
      }
    }
    boot()
    return () => { alive = false }
  }, [])

  const login = useCallback(async (username, password) => {
    const data = await authApi.login(username, password) // LoginResponse
    tokenStore.set(data)
    localStorage.setItem('mws_perms', JSON.stringify(data.permissions || []))
    setUser({
      userId: data.userId,
      username: data.username,
      fullName: data.fullName,
      role: data.role,
      permissions: data.permissions || [],
    })
    return data
  }, [])

  const value = useMemo(() => {
    const perms = new Set(user?.permissions || [])
    return {
      user,
      loading,
      isAuthenticated: !!user,
      permissions: user?.permissions || [],
      hasPermission: (p) => (Array.isArray(p) ? p.some(x => perms.has(x)) : perms.has(p)),
      login,
      logout,
    }
  }, [user, loading, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

function normalizeMe(me) {
  // /me trả UserResponse (không có permissions). Giữ permissions cũ nếu có.
  return {
    userId: me.id,
    username: me.username,
    fullName: me.fullName,
    role: me.roleName || me.role,
    permissions: me.permissions || safeReadPerms(),
  }
}

// /me không trả permissions => đọc lại từ localStorage đã lưu lúc login.
function safeReadPerms() {
  try { return JSON.parse(localStorage.getItem('mws_perms') || '[]') } catch { return [] }
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth phải nằm trong <AuthProvider>')
  return ctx
}

// eslint-disable-next-line react-refresh/only-export-components -- re-export tiện dụng
export { getErrorMessage }
