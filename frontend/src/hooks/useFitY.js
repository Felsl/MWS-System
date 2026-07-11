import { useLayoutEffect, useRef, useState, useCallback } from 'react'

/**
 * Đo chiều cao khả dụng từ đỉnh phần tử tới đáy viewport (trừ khoảng chừa `bottomGap`).
 * Tự cập nhật khi resize cửa sổ, thu gọn sidebar, hoặc bộ lọc phía trên xuống dòng.
 * Dùng cho vùng cần "vừa 1 màn hình" và tự cuộn bên trong (VD danh sách thông báo).
 */
export function useFitY({ bottomGap = 36, min = 160 } = {}) {
  const ref = useRef(null)
  const [y, setY] = useState(min)

  const measure = useCallback(() => {
    const el = ref.current
    if (!el) return
    const top = el.getBoundingClientRect().top
    setY(Math.max(min, Math.floor(window.innerHeight - top - bottomGap)))
  }, [bottomGap, min])

  useLayoutEffect(() => {
    measure()
    const raf = requestAnimationFrame(measure)
    const ro = new ResizeObserver(measure)
    ro.observe(document.body)
    if (ref.current) ro.observe(ref.current)
    window.addEventListener('resize', measure)
    return () => {
      cancelAnimationFrame(raf)
      ro.disconnect()
      window.removeEventListener('resize', measure)
    }
  }, [measure])

  return { ref, y }
}
