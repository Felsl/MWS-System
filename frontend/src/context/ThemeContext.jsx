import { createContext, useContext, useEffect, useMemo, useState } from 'react'

const KEY = 'mws_theme' // 'light' | 'dark' | 'system'
const ThemeCtx = createContext(null)

function systemPrefersDark() {
  return typeof window !== 'undefined'
    && window.matchMedia?.('(prefers-color-scheme: dark)').matches
}

/**
 * Chế độ sáng/tối. Mặc định 'system' — theo cài đặt của máy.
 *
 * Kho có ca đêm; màn hình trắng 100% trong kho tối là thứ người trực đêm phàn
 * nàn đầu tiên. Antd đã có sẵn thuật toán darkAlgorithm nên phần khó không nằm
 * ở đây, mà ở chỗ dọn hết màu hardcode trong code (#fff, #f0f0f0, #e6f4ff...)
 * sang design token — nếu không, bật dark là chữ trắng trên nền trắng.
 *
 * Đặt data-theme lên <html> để CSS thuần (thanh cuộn trong index.css) đổi theo.
 */
export function ThemeProvider({ children }) {
  const [pref, setPref] = useState(() => localStorage.getItem(KEY) || 'system')
  const [sysDark, setSysDark] = useState(systemPrefersDark)

  // Theo dõi khi người dùng đổi cài đặt sáng/tối của hệ điều hành.
  useEffect(() => {
    const mq = window.matchMedia?.('(prefers-color-scheme: dark)')
    if (!mq) return
    const on = (e) => setSysDark(e.matches)
    mq.addEventListener('change', on)
    return () => mq.removeEventListener('change', on)
  }, [])

  const isDark = pref === 'dark' || (pref === 'system' && sysDark)

  useEffect(() => {
    localStorage.setItem(KEY, pref)
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light')
  }, [pref, isDark])

  const value = useMemo(() => ({
    pref, isDark,
    setPref,
    toggle: () => setPref(isDark ? 'light' : 'dark'),
  }), [pref, isDark])

  return <ThemeCtx.Provider value={value}>{children}</ThemeCtx.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components -- context + hook cùng file là mẫu chuẩn; chỉ ảnh hưởng hot-reload lúc dev
export function useThemeMode() {
  const v = useContext(ThemeCtx)
  if (!v) throw new Error('useThemeMode phải nằm trong <ThemeProvider>')
  return v
}
