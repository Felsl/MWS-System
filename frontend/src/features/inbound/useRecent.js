import { useCallback, useState } from 'react'
// Vì BE chưa có list PO/GRN, lưu tạm danh sách "gần đây" ở localStorage.
export function useRecent(key, max = 12) {
  const [items, setItems] = useState(() => {
    try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] }
  })
  const push = useCallback((item) => {
    setItems((prev) => {
      const next = [{ ...item, at: Date.now() }, ...prev.filter((x) => x.id !== item.id)].slice(0, max)
      localStorage.setItem(key, JSON.stringify(next))
      return next
    })
  }, [key, max])
  const clear = useCallback(() => { localStorage.removeItem(key); setItems([]) }, [key])
  return { items, push, clear }
}
