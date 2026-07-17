/**
 * ExportService — 导出编排服务
 *
 * 职责：
 * 1. 验证导出元素和数据
 * 2. 等待字体加载完成（确保 web fonts 渲染）
 * 3. 调用 CardExporter 导出
 * 4. 触发浏览器下载
 * 5. 管理导出状态（loading guard）
 */
import { ref } from 'vue'
import type { ExportOptions, ExportResult, ExportStatus } from './types'
import { DEFAULT_EXPORT_OPTIONS } from './types'
import { exportToBlob } from './CardExporter'

// ══════════════════════════════════════════════════
// 工具函数
// ══════════════════════════════════════════════════

/**
 * 生成导出文件名
 * 格式: {卡片名称}-card-{YYYYMMDD-HHmmss}.{ext}
 */
export function generateFilename(
  cardName: string,
  format: string,
  ext?: string,
): string {
  const now = new Date()
  const ts = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}-${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
  const name = cardName?.trim()
    ? cardName.trim().replace(/[^a-zA-Z0-9一-鿿_-]/g, '_').slice(0, 40)
    : 'gamecard'
  const suffix = ext ?? format
  return `${name}-card-${ts}.${suffix}`
}

/**
 * 触发浏览器文件下载
 */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  // 延迟回收 ObjectURL
  setTimeout(() => URL.revokeObjectURL(url), 10_000)
}

/**
 * 验证导出元素是否有效
 */
export function validateElement(
  element: HTMLElement | null | undefined,
): { valid: true } | { valid: false; error: string } {
  if (!element) {
    return { valid: false, error: '卡片元素未找到，请先选择模板并填写内容' }
  }
  if (!(element instanceof HTMLElement)) {
    return { valid: false, error: '卡片元素无效' }
  }
  return { valid: true }
}

/**
 * 等待字体加载完成
 */
async function waitForFonts(): Promise<void> {
  if (typeof document !== 'undefined' && document.fonts?.ready) {
    await document.fonts.ready
  }
  // 额外等待一帧，让浏览器完成字体重排
  await new Promise((r) => setTimeout(r, 200))
}

// ══════════════════════════════════════════════════
// Composable
// ══════════════════════════════════════════════════

/**
 * useCardExport — Vue Composable
 *
 * 提供 exportCard() 方法，管理导出状态。
 *
 * @example
 * const { exportCard, exportStatus, exportResult } = useCardExport()
 * await exportCard(cardElement, '皮卡丘', store.cardData)
 */
export function useCardExport() {
  const exportStatus = ref<ExportStatus>('idle')
  const exportResult = ref<ExportResult | null>(null)

  /**
   * 导出并下载卡片
   *
   * @param element    卡片 DOM 元素
   * @param cardName   卡片名称（用于文件名）
   * @param options    导出选项（可选，将合并到默认值）
   */
  async function exportCard(
    element: HTMLElement | null,
    cardName: string,
    options?: Partial<ExportOptions>,
  ): Promise<ExportResult> {
    // 防止重复导出
    if (exportStatus.value === 'exporting') {
      return { success: false, error: '正在导出中，请稍候' }
    }

    // 验证元素
    const validation = validateElement(element)
    if (!validation.valid) {
      exportResult.value = { success: false, error: validation.error }
      exportStatus.value = 'error'
      return exportResult.value
    }

    // 合并选项
    const opts: ExportOptions = { ...DEFAULT_EXPORT_OPTIONS, ...options }
    exportStatus.value = 'exporting'
    exportResult.value = null

    try {
      // 1. 等待字体加载
      await waitForFonts()

      // 2. 导出为 Blob
      const blob = await exportToBlob(element!, opts)

      // 3. 生成文件名并下载
      const ext = opts.format === 'jpg' ? 'jpeg' : opts.format
      const filename = generateFilename(cardName, opts.format, ext)
      downloadBlob(blob, filename)

      const result: ExportResult = { success: true, filename }
      exportResult.value = result
      exportStatus.value = 'success'

      // 3 秒后自动重置状态
      setTimeout(() => {
        if (exportStatus.value === 'success') {
          exportStatus.value = 'idle'
        }
      }, 3000)

      return result
    } catch (err: any) {
      const msg = err?.message || err?.toString?.() || '导出失败'
      const result: ExportResult = { success: false, error: msg }
      exportResult.value = result
      exportStatus.value = 'error'
      return result
    }
  }

  /** 重置导出状态 */
  function resetStatus() {
    exportStatus.value = 'idle'
    exportResult.value = null
  }

  return {
    exportStatus,
    exportResult,
    exportCard,
    resetStatus,
  }
}
