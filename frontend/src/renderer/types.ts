/**
 * TemplateRenderer 类型定义
 *
 * 每个模板 layout.vue 接收的 props 接口。
 */
import type { Template, CardData, ImageConfig } from '@/types'
import type { ResolvedTheme } from '@/utils/themeResolver'

/**
 * 传给模板 layout.vue 的标准 props
 *
 * 所有模板的 layout.vue 都接收相同的数据接口，
 * 但选择如何使用这些数据完全由模板决定。
 */
export interface TemplateLayoutProps {
  /** 用户填写的所有卡片字段数据 */
  cardData: CardData
  /** 解析后的主题（颜色、图标、CSS 变量） */
  theme: ResolvedTheme
  /** 角色图片 URL（blob URL 或 data URI） */
  imageUrl: string | null
  /** 图片编辑配置（形状、缩放、位置、裁剪） */
  imageConfig: ImageConfig
  /** 当前模板定义 */
  template: Template | null
}

/**
 * 模板布局模块的导出格式
 *
 * 每个 templates/{id}/index.ts 应默认导出此接口
 */
export interface TemplateModule {
  /** 布局组件 */
  default: unknown
  /** 模板的默认尺寸（可覆盖 template.json 中的 size） */
  cardSize?: { width: number; height: number }
}
