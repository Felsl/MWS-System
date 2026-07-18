import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react'
import { Table, Skeleton, Empty } from 'antd'

/**
 * Bảng "vừa 1 màn hình": chỉ thân bảng cuộn dọc, hàng tiêu đề dính, phân trang
 * ghim ở đáy vùng nhìn.
 *
 * ── Vì sao viết lại (bản cũ có 3 vấn đề thật) ──────────────────────────────
 * 1) `ro.observe(document.body)` + `ro.observe(wrapRef)` cùng lúc: mỗi lần đo
 *    xong `setY` -> layout đổi -> ResizeObserver bắn tiếp -> đo lại. Vòng này
 *    tự tắt vì y hội tụ, nhưng trên máy chậm nó gây giật và Chrome đôi khi ném
 *    "ResizeObserver loop completed with undelivered notifications".
 *    => Nay CHỈ quan sát wrapper (thứ duy nhất thực sự cần biết vị trí), và
 *       gộp mọi lần bắn vào 1 lần đo trong rAF kế tiếp.
 * 2) `setY` gọi cả khi giá trị không đổi -> render thừa. => so sánh trước khi set.
 * 3) Không có đường thoát: muốn bảng cuộn tự nhiên như ERP bình thường thì phải
 *    sửa 15 trang. => Nay có công tắc FIT_TABLE_ENABLED và prop `fit={false}`.
 *
 * ── Trạng thái tải ────────────────────────────────────────────────────────
 * `loading` của antd Table = spinner tròn đè lên vùng "Không có dữ liệu". Lúc
 * mở trang lần đầu người dùng thấy một khung rỗng + cái vòng quay, rồi nội dung
 * đột ngột nhảy vào — bố cục giật, và không hình dung được sắp có gì.
 *
 * Nay lần tải ĐẦU TIÊN (loading && chưa có dòng nào) dựng khung xương: giữ
 * nguyên hàng tiêu đề và độ rộng cột thật, thay ô bằng vạch xám. Không giật,
 * và người dùng thấy trước hình dạng dữ liệu.
 *
 * Lần tải SAU (đổi trang/lọc) thì KHÔNG đụng gì: các trang truyền `isLoading`
 * (không phải `isFetching`) + placeholderData:keepPreviousData nên bảng cũ ở lại
 * cho tới khi có dữ liệu mới — không nhấp nháy.
 *
 * ── Muốn BỎ hẳn cơ chế "vừa 1 màn" ────────────────────────────────────────
 * Đổi FIT_TABLE_ENABLED = false ở ngay dưới. Toàn bộ trang danh sách lập tức
 * quay về <Table> thường: trang cuộn tự nhiên, không còn vùng cuộn lồng nhau,
 * không còn phép đo. Không phải sửa file nào khác. Cần bỏ chỉ 1 trang thì
 * truyền <FitTable fit={false} ... />.
 */

// Công tắc toàn cục — xem khối chú thích ở trên.
export const FIT_TABLE_ENABLED = true

export default function FitTable({
  scroll, bottomGap = 36, minBodyHeight = 140, fit = true, skeletonRows = 6,
  emptyState, ...rest
}) {
  const wrapRef = useRef(null)
  const rafRef = useRef(0)
  const [y, setY] = useState(minBodyHeight)

  const enabled = FIT_TABLE_ENABLED && fit

  const measure = useCallback(() => {
    const wrap = wrapRef.current
    if (!wrap) return
    const top = wrap.getBoundingClientRect().top
    const thead = wrap.querySelector('.ant-table-thead')
    const pager = wrap.querySelector('.ant-pagination')
    const theadH = thead ? thead.getBoundingClientRect().height : 40
    const pagerH = pager ? pager.getBoundingClientRect().height + 16 : 0
    const avail = window.innerHeight - top - theadH - pagerH - bottomGap
    const next = Math.max(minBodyHeight, Math.floor(avail))
    // Chỉ set khi đổi thật -> cắt vòng đo-render-đo.
    setY((prev) => (prev === next ? prev : next))
  }, [bottomGap, minBodyHeight])

  // Gộp nhiều tín hiệu (resize + observer + mount) vào 1 lần đo ở frame kế tiếp.
  const scheduleMeasure = useCallback(() => {
    if (rafRef.current) return
    rafRef.current = requestAnimationFrame(() => { rafRef.current = 0; measure() })
  }, [measure])

  useLayoutEffect(() => {
    if (!enabled) return
    scheduleMeasure()
    // CHỈ quan sát wrapper: nó dịch chuyển khi thanh lọc phía trên xuống dòng
    // hoặc sidebar thu gọn — đủ để biết cần đo lại. Không quan sát document.body.
    const ro = new ResizeObserver(scheduleMeasure)
    if (wrapRef.current) ro.observe(wrapRef.current)
    window.addEventListener('resize', scheduleMeasure)
    return () => {
      ro.disconnect()
      window.removeEventListener('resize', scheduleMeasure)
      if (rafRef.current) cancelAnimationFrame(rafRef.current)
      rafRef.current = 0
    }
  }, [enabled, scheduleMeasure])

  // Đổi số dòng (lọc, chuyển trang) làm bảng cao/thấp khác đi -> đo lại.
  useEffect(() => {
    if (enabled) scheduleMeasure()
  }, [enabled, scheduleMeasure, rest.dataSource])

  // Khung xương chỉ cho lần tải đầu (chưa có dòng nào). Đã có dữ liệu cũ thì
  // giữ nguyên bảng — thay bằng khung xương lúc đó mới là bước lùi.
  const firstLoad = rest.loading && !(rest.dataSource || []).length
  let props = firstLoad ? { ...rest, ...skeletonProps(rest.columns, skeletonRows) } : rest

  // Bảng rỗng (đã tải xong): thay "No data" tiếng Anh mặc định bằng empty-state
  // có lời mời hành động, nếu trang truyền vào. Không truyền -> ít nhất tiếng Việt.
  if (!firstLoad && !rest.loading && !(rest.dataSource || []).length) {
    props = {
      ...props,
      locale: {
        emptyText: emptyState || <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="Chưa có dữ liệu" />,
        ...(rest.locale || {}),
      },
    }
  }

  if (!enabled) {
    // Bảng thường: trang cuộn tự nhiên, không đo đạc, không vùng cuộn lồng nhau.
    return <Table {...props} scroll={{ x: 'max-content', ...(scroll || {}) }} />
  }

  return (
    <div ref={wrapRef}>
      <Table {...props} scroll={{ x: 'max-content', ...(scroll || {}), y }} />
    </div>
  )
}

/**
 * Đổi cột thật thành cột "vạch xám" + bơm N dòng giả.
 * Giữ nguyên title/width/fixed của cột gốc để hàng tiêu đề và độ rộng không đổi
 * khi dữ liệu thật về => không giật bố cục.
 */
function skeletonProps(columns = [], rows) {
  return {
    loading: false,   // tắt spinner: đã có khung xương rồi, hai thứ cùng lúc là thừa
    pagination: false,
    dataSource: Array.from({ length: rows }, (_, i) => ({ __sk: i })),
    rowKey: '__sk',
    columns: columns.map((c, i) => ({
      ...c,
      key: c.key || c.dataIndex || `sk-${i}`,
      render: () => <Skeleton.Input active size="small" style={{ width: '70%', minWidth: 40, height: 16 }} />,
    })),
  }
}
