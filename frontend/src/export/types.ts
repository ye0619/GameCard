/**
 * 导出模块 — 类型定义
 */

/** 支持的导出格式 */
export type ExportFormat = 'png' | 'jpg' | 'webp'

/** 导出选项 */
export interface ExportOptions {
  /** 输出格式，默认 png */
  format: ExportFormat
  /** 像素倍率：1/2/4，默认 2 */
  scale: number
  /** 有损格式质量 (0-1)，默认 0.92 */
  quality: number
  /** 输出文件名（不含扩展名），自动生成时为空 */
  filename?: string
  /** 背景色（JPG 必须，PNG 可选） */
  backgroundColor?: string
}

/** 导出选项默认值 */
export const DEFAULT_EXPORT_OPTIONS: ExportOptions = {
  format: 'png',
  scale: 2,
  quality: 0.92,
}

/** 导出状态 */
export type ExportStatus = 'idle' | 'exporting' | 'success' | 'error'

/** 导出结果 */
export interface ExportResult {
  success: boolean
  /** 失败时的错误消息 */
  error?: string
  /** 生成的文件名 */
  filename?: string
}
