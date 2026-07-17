/**
 * CardExporter — 核心导出引擎
 *
 * 封装 html-to-image，提供统一的导出接口。
 * 职责仅限于将 HTMLElement 转换为 Blob/DataURL。
 */
import { toBlob, toPng, toJpeg } from 'html-to-image'
import type { ExportOptions, ExportFormat } from './types'

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
  const config = buildConfig(opts)
  return callExporter(node, opts.format, config)
}

/**
 * 将指定 DOM 节点导出为 Data URL
 */
export async function exportToDataUrl(
  node: HTMLElement,
  opts: ExportOptions,
): Promise<string> {
  const config = buildConfig(opts)

  switch (opts.format) {
    case 'jpg':
      return toJpeg(node, config)
    case 'webp':
      return toPng(node, { ...config, pixelRatio: opts.scale }) // toPng works as base
    case 'png':
    default:
      return toPng(node, config)
  }
}

/**
 * 构建 html-to-image 配置
 */
function buildConfig(opts: ExportOptions) {
  return {
    pixelRatio: opts.scale,
    quality: opts.format === 'png' ? undefined : opts.quality,
    cacheBust: true,
    // 跨域图片处理：采用 cors 模式
    fetchRequestInit: {
      mode: 'cors' as RequestMode,
      credentials: 'same-origin' as RequestCredentials,
    },
    // 不跳过字体——确保嵌入 web fonts
    skipFonts: false,
    // 背景色：JPG 必须有背景色（透明 PNG 不需要）
    backgroundColor: opts.backgroundColor ?? (opts.format === 'jpg' ? '#FFFFFF' : undefined),
  }
}

/**
 * 根据格式调用不同的 html-to-image 导出函数
 */
async function callExporter(
  node: HTMLElement,
  format: ExportFormat,
  config: ReturnType<typeof buildConfig>,
): Promise<Blob> {
  let blob: Blob | null = null

  switch (format) {
    case 'jpg':
      blob = await toBlob(node, config)
      break
    case 'webp':
      blob = await toBlob(node, config)
      break
    case 'png':
    default:
      blob = await toBlob(node, config)
      break
  }

  if (!blob) {
    throw new Error('导出失败：生成的图片为空')
  }

  return blob
}
