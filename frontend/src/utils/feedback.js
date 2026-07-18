/**
 * Phản hồi phi-thị-giác khi quét mã.
 *
 * Lý do: trong kho ồn + đeo găng, người quét không thể vừa bắn mã vừa dán mắt
 * vào toast antd (biến mất sau 3s). Một tiếng bíp + rung là cách duy nhất họ
 * biết dòng vừa rồi ăn hay trượt mà không cần nhìn màn hình.
 *
 * Dùng WebAudio thay vì file .mp3 để không thêm asset và không phải chờ tải.
 * AudioContext chỉ được tạo sau tương tác đầu tiên của người dùng (chính sách
 * autoplay của trình duyệt) — ở đây luôn là sau khi bấm "Bật camera"/nhập tay
 * nên an toàn.
 */

let ctx = null
function audio() {
  if (typeof window === 'undefined') return null
  if (!ctx) {
    const AC = window.AudioContext || window.webkitAudioContext
    if (!AC) return null
    try { ctx = new AC() } catch { return null }
  }
  if (ctx.state === 'suspended') ctx.resume().catch(() => {})
  return ctx
}

function tone(freq, ms, when = 0, gain = 0.06) {
  const ac = audio()
  if (!ac) return
  try {
    const osc = ac.createOscillator()
    const g = ac.createGain()
    osc.type = 'square'
    osc.frequency.value = freq
    g.gain.value = gain
    osc.connect(g); g.connect(ac.destination)
    const t0 = ac.currentTime + when
    osc.start(t0)
    // fade nhẹ ở cuối cho đỡ "cụp"
    g.gain.setValueAtTime(gain, t0 + ms / 1000 - 0.01)
    g.gain.linearRampToValueAtTime(0, t0 + ms / 1000)
    osc.stop(t0 + ms / 1000)
  } catch { /* thiết bị không phát được thì thôi, không chặn luồng */ }
}

function vibrate(pattern) {
  try { navigator.vibrate?.(pattern) } catch { /* noop */ }
}

/** Quét đúng: 1 bíp cao, ngắn + rung 40ms. */
export function beepOk() {
  tone(1320, 90)
  vibrate(40)
}

/** Quét sai / lỗi API: 2 bíp trầm + rung 2 nhịp — khác hẳn tiếng OK khi nghe. */
export function beepError() {
  tone(320, 140, 0)
  tone(240, 200, 0.17)
  vibrate([70, 60, 140])
}

/** Xong cả lệnh: 3 nốt đi lên. */
export function beepDone() {
  tone(880, 90, 0)
  tone(1170, 90, 0.1)
  tone(1560, 160, 0.2)
  vibrate([50, 40, 50, 40, 120])
}
