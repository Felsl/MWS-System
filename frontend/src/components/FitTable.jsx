import { useLayoutEffect, useRef, useState, useCallback } from 'react'
import { Table } from 'antd'

/**
 * Bảng "vừa 1 màn hình": chỉ thân bảng cuộn dọc, hàng tiêu đề cột dính (sticky),
 * phân trang ghim ở đáy vùng nhìn. Chiều cao thân bảng tự đo theo viewport và
 * cập nhật khi resize cửa sổ / thu gọn sidebar / bộ lọc phía trên xuống dòng.
 *
 * Dùng THAY cho <Table> ở các trang DANH SÁCH. Nhận mọi prop của antd Table;
 * `scroll.x` mặc định 'max-content' (giữ cuộn ngang), `scroll.y` do component tự tính.
 *  - bottomGap: khoảng chừa dưới phân trang (mặc định 36 = padding card + padding content).
 *  - minBodyHeight: chiều cao tối thiểu của thân bảng (màn rất thấp).
 */
export default function FitTable({ scroll, bottomGap = 36, minBodyHeight = 140, ...rest }) {
  const wrapRef = useRef(null)
  const [y, setY] = useState(minBodyHeight)

  const measure = useCallback(() => {
    const wrap = wrapRef.current
    if (!wrap) return
    const top = wrap.getBoundingClientRect().top
    const thead = wrap.querySelector('.ant-table-thead')
    const pager = wrap.querySelector('.ant-pagination')
    const theadH = thead ? thead.getBoundingClientRect().height : 40
    const pagerH = pager ? pager.getBoundingClientRect().height + 16 : 0
    const avail = window.innerHeight - top - theadH - pagerH - bottomGap
    setY(Math.max(minBodyHeight, Math.floor(avail)))
  }, [bottomGap, minBodyHeight])

  useLayoutEffect(() => {
    measure()
    const raf = requestAnimationFrame(measure)
    const ro = new ResizeObserver(measure)
    ro.observe(document.body)
    if (wrapRef.current) ro.observe(wrapRef.current)
    window.addEventListener('resize', measure)
    return () => {
      cancelAnimationFrame(raf)
      ro.disconnect()
      window.removeEventListener('resize', measure)
    }
  }, [measure])

  return (
    <div ref={wrapRef}>
      <Table {...rest} scroll={{ x: 'max-content', ...(scroll || {}), y }} />
    </div>
  )
}
