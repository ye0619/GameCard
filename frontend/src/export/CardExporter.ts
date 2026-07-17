/**
 * CardExporter — 核心导出引擎
 *
 * 封装 DOM→Image 转换，提供统一的导出接口。
 * 职责仅限于将 HTMLElement 转换为 Blob/DataURL。
 *
 * 导出策略（按优先级降序尝试）：
 * 1. html2canvas — 直接 Canvas 2D 渲染（不依赖 SVG foreignObject，CSS 兼容性最好）
 * 2. html-to-image toCanvas — 标准 SVG foreignObject 方案
 * 3. html-to-image toSvg + 手动渲染 — 兜底
 */
import html2canvas from 'html2canvas'
import { toCanvas as htmlToImageToCanvas, toSvg } from 'html-to-image'
import type { ExportOptions, ExportFormat } from './types'

/** 卡片原生尺寸（1280×720 横版 HUD 布局） */
const CARD_WIDTH = 1280
const CARD_HEIGHT = 720

/**
 * html-to-image 的 MIME 类型映射
 */
const FORMAT_MIME: Record<ExportFormat, string> = {
  png: 'image/png',
  jpg: 'image/jpeg',
  webp: 'image/webp',
}

/**
 * 将指定 DOM 节点导出为 Blob
 *
 * @param node   要导出的 DOM 节点
 * @param opts   导出选项
 * @returns      Blob 对象
 */
export async function exportToBlob(
  node: HTMLElement,
  opts: ExportOptions,
): Promise<Blob> {
  const scale = opts.scale
  const mimeType = FORMAT_MIME[opts.format]

  // 策略 1：html2canvas（不依赖 SVG foreignObject，CSS 兼容性最好）
  try {
    const canvas = await html2canvas(node, buildHtml2CanvasConfig(opts))
    const blob = await canvasToBlob(canvas, mimeType, opts.format === 'png' ? undefined : opts.quality)
    if (blob) return blob
  } catch (err) {
    console.warn('Export strategy 1 (html2canvas) failed:', err)
  }

  // 策略 2：html-to-image toCanvas
  try {
    const canvas = await htmlToImageToCanvas(node, buildHtmlToImageConfig(opts))
    const blob = await canvasToBlob(canvas, mimeType, opts.format === 'png' ? undefined : opts.quality)
    if (blob) return blob
  } catch (err) {
    console.warn('Export strategy 2 (html-to-image toCanvas) failed:', err)
  }

  // 策略 3：toSvg + 手动渲染
  try {
    const svgDataUrl = await toSvg(node, buildHtmlToImageConfig(opts))
    const blob = await svgToBlob(svgDataUrl, CARD_WIDTH * scale, CARD_HEIGHT * scale, mimeType, opts)
    if (blob) return blob
  } catch (err) {
    console.warn('Export strategy 3 (toSvg) failed:', err)
  }

  // 策略 4：html-to-image 降级配置（跳过字体、纯色背景）
  try {
    const fallbackConfig = buildFallbackConfig(opts)
    const canvas = await htmlToImageToCanvas(node, fallbackConfig)
    const blob = await canvasToBlob(canvas, mimeType, opts.format === 'png' ? undefined : opts.quality)
    if (blob) return blob
  } catch (err) {
    console.warn('Export strategy 4 (fallback) failed:', err)
  }

  throw new Error('导出渲染失败，请尝试使用更简单的卡片样式或降低分辨率')
}

/**
 * 将指定 DOM 节点导出为 Data URL
 */
export async function exportToDataUrl(
  node: HTMLElement,
  opts: ExportOptions,
): Promise<string> {
  // 尝试 html2canvas 优先
  try {
    const canvas = await html2canvas(node, buildHtml2CanvasConfig(opts))
    const mimeType = FORMAT_MIME[opts.format]
    return canvas.toDataURL(mimeType, opts.format === 'png' ? undefined : opts.quality)
  } catch (err) {
    console.warn('exportToDataUrl html2canvas failed:', err)
  }

  // 回退到 html-to-image
  try {
    const config = buildHtmlToImageConfig(opts)
    const canvas = await htmlToImageToCanvas(node, config)
    const mimeType = FORMAT_MIME[opts.format]
    return canvas.toDataURL(mimeType, opts.format === 'png' ? undefined : opts.quality)
  } catch (err) {
    console.warn('exportToDataUrl html-to-image failed:', err)
  }

  throw new Error('导出 Data URL 失败')
}

// ══════════════════════════════════════════════════
// html2canvas 配置
// ══════════════════════════════════════════════════

function buildHtml2CanvasConfig(opts: ExportOptions) {
  return {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    scale: opts.scale,
    /** 使用 CORS 加载跨域图片 */
    useCORS: true,
    /** 允许加载跨域图片（不拦截） */
    allowTaint: false,
    /** PNG 导出透明背景 */
    backgroundColor: opts.backgroundColor ?? null,
    /** 日志关闭 */
    logging: false,
    /** 窗口宽高（避免滚动条干扰） */
    windowWidth: CARD_WIDTH,
    windowHeight: CARD_HEIGHT,
    /** 图片跨域处理 */
    imageTimeout: 15000,
    /** 移除 canvas 污染保护（我们使用同源资源） */
    proxy: undefined,
  }
}

// ══════════════════════════════════════════════════
// html-to-image 配置
// ══════════════════════════════════════════════════

function buildHtmlToImageConfig(opts: ExportOptions) {
  return {
    pixelRatio: opts.scale,
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    quality: opts.format === 'png' ? undefined : opts.quality,
    cacheBust: true,
    fetchRequestInit: {
      mode: 'cors' as RequestMode,
      credentials: 'same-origin' as RequestCredentials,
    },
    skipFonts: false,
    backgroundColor: opts.backgroundColor ?? (opts.format === 'jpg' ? '#FFFFFF' : undefined),
  }
}

function buildFallbackConfig(opts: ExportOptions) {
  return {
    pixelRatio: opts.scale,
    quality: opts.format === 'png' ? undefined : opts.quality,
    cacheBust: true,
    fetchRequestInit: {
      mode: 'cors' as RequestMode,
      credentials: 'same-origin' as RequestCredentials,
    },
    skipFonts: true,
    backgroundColor: '#1a1a2e',
  }
}

// ══════════════════════════════════════════════════
// 通用工具函数
// ══════════════════════════════════════════════════

function canvasToBlob(
  canvas: HTMLCanvasElement,
  mimeType: string,
  quality: number | undefined,
): Promise<Blob | null> {
  return new Promise((resolve) => {
    canvas.toBlob(
      (blob) => resolve(blob),
      mimeType,
      quality,
    )
  })
}

function svgToBlob(
  svgDataUrl: string,
  width: number,
  height: number,
  mimeType: string,
  opts: ExportOptions,
): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const img = new Image()

    let settled = false
    function settle(err?: Error, blob?: Blob) {
      if (settled) return
      settled = true
      if (err) reject(err)
      else if (blob) resolve(blob)
    }

    img.onload = () => {
      try {
        const decodePromise = img.decode
          ? img.decode().catch(() => { /* ignore decode failure */ })
          : Promise.resolve()

        decodePromise.then(() => {
          const canvas = document.createElement('canvas')
          canvas.width = width
          canvas.height = height
          const ctx = canvas.getContext('2d')
          if (!ctx) {
            settle(new Error('Canvas 2D 上下文获取失败'))
            return
          }
          ctx.drawImage(img, 0, 0, width, height)
          canvas.toBlob(
            (blob) => {
              if (blob) settle(undefined, blob)
              else settle(new Error('Canvas 转 Blob 失败'))
            },
            mimeType,
            opts.format === 'png' ? undefined : opts.quality,
          )
        })
      } catch (e) {
        settle(e instanceof Error ? e : new Error(String(e)))
      }
    }
    img.onerror = (e) => {
      const detail = e instanceof Event ? `图片加载事件: ${e.type}` : String(e)
      settle(new Error(`SVG 渲染失败（${detail}）`))
    }

    img.crossOrigin = 'anonymous'
    img.src = svgDataUrl

    if (img.complete && !settled) {
      setTimeout(() => img.onload?.(new Event('load')), 0)
    }
  })
}
