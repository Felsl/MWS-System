import { describe, it, expect } from 'vitest'
import { fitBarcodeSvg } from '../features/inventory/barcodeSvg'
import { buildPrintDocument, SHEET_CSS, PRINT_DOC_CSS } from '../features/inventory/printSheet'

const SVG_NS = 'http://www.w3.org/2000/svg'

/** Dựng đúng những gì JsBarcode đặt lên <svg> (bin/renderers/svg.js:151-155). */
function makeJsBarcodeSvg() {
  const el = document.createElementNS(SVG_NS, 'svg')
  el.setAttribute('width', '286px')
  el.setAttribute('height', '60px')
  el.setAttribute('viewBox', '0 0 286 60')
  return el
}

describe('fitBarcodeSvg — ghim lỗi mã vạch bị cắt cụt', () => {
  it('giữ nguyên viewBox số trần của JsBarcode', () => {
    const el = makeJsBarcodeSvg()
    fitBarcodeSvg(el)
    expect(el.getAttribute('viewBox')).toBe('0 0 286 60')
  })

  it('viewBox KHÔNG được chứa đơn vị px (viewBox có đơn vị là không hợp lệ)', () => {
    const el = makeJsBarcodeSvg()
    fitBarcodeSvg(el)
    expect(el.getAttribute('viewBox')).not.toMatch(/px/)
  })

  it('bỏ width/height cố định và đặt preserveAspectRatio meet', () => {
    const el = makeJsBarcodeSvg()
    fitBarcodeSvg(el)
    expect(el.hasAttribute('width')).toBe(false)
    expect(el.hasAttribute('height')).toBe(false)
    expect(el.getAttribute('preserveAspectRatio')).toBe('xMidYMid meet')
  })
})

describe('buildPrintDocument — ghim lỗi tem bị co ~50% khi in', () => {
  it('nhúng đúng nội dung tem', () => {
    const html = buildPrintDocument('<div class="bl-preview">XIN CHAO</div>')
    expect(html).toContain('XIN CHAO')
    expect(html).toContain('<!DOCTYPE html>')
  })

  it('gỡ max-width để .bl-preview giữ đủ 194mm (không bị khung cha ép nhỏ)', () => {
    expect(PRINT_DOC_CSS).toMatch(/\.bl-preview\s*\{[^}]*max-width:\s*none/)
    expect(PRINT_DOC_CSS).toMatch(/\.bl-preview\s*\{[^}]*width:\s*194mm/)
  })

  it('khổ giấy A4 và lề 8mm', () => {
    expect(PRINT_DOC_CSS).toMatch(/@page\s*\{[^}]*size:\s*A4/)
    expect(PRINT_DOC_CSS).toMatch(/@page\s*\{[^}]*margin:\s*8mm/)
  })

  it('không còn dựa vào position:absolute + visibility:hidden của trang app', () => {
    const html = buildPrintDocument('')
    expect(html).not.toMatch(/visibility:\s*hidden/)
    expect(html).not.toMatch(/position:\s*absolute/)
  })

  it('khung mã vạch không bị flex co lại', () => {
    expect(SHEET_CSS).toMatch(/\.bl-bar\s*\{[^}]*flex:\s*0 0 16mm/)
  })

  it('tem 2 cột + khung vạch 16mm (để X-dimension >= 0.5mm)', () => {
    expect(SHEET_CSS).toMatch(/grid-template-columns:\s*repeat\(2, 1fr\)/)
    expect(SHEET_CSS).toMatch(/\.bl-bar\s*\{[^}]*height:\s*16mm/)
  })
})
