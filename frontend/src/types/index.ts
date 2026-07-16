// ===================== 模板字段定义 =====================

/** 模板字段类型 */
export type FieldType = 'TEXT' | 'TEXTAREA' | 'IMAGE' | 'NUMBER' | 'SELECT' | 'ENUM' | 'ARRAY' | 'COLOR'

/** 模板字段定义 */
export interface TemplateField {
  key: string
  label: string
  type: FieldType
  required: boolean
  defaultValue: string | null
  placeholder: string | null
  options: string | null // JSON 数组字符串（SELECT / ENUM 类型）
  fields: TemplateField[] | null // ARRAY 类型的子字段定义
}

// ===================== 主题映射 =====================

/** 主题映射条目（属性值 → 视觉主题） */
export interface ThemeMappingItem {
  /** 背景标识（对应 assets.backgrounds 中的键） */
  background: string
  /** 主色调（十六进制，如 "#EF4444"） */
  color: string
  /** 图标标识（对应 assets.icons 中的键） */
  icon: string
  /** 辅助色 */
  secondaryColor?: string
  /** 扩展样式变量 */
  styles?: Record<string, string>
}

// ===================== 模板资产 =====================

/** 模板资源定义 */
export interface TemplateAssets {
  backgrounds: Record<string, string> | null
  icons: Record<string, string> | null
  frames: Record<string, string> | null
  fonts: Record<string, string> | null
}

// ===================== 尺寸 =====================

/** 卡片尺寸 */
export interface TemplateSize {
  width: number
  height: number
}

// ===================== 模板元数据 =====================

/** 模板元数据 */
export interface TemplateMetadata {
  createTime: string
  updateTime: string
  templateVersion: string
  minAppVersion: string
}

// ===================== 模板 =====================

/** 模板 - 游戏卡片模板的核心模型 */
export interface Template {
  id: string
  name: string
  description: string
  author: string
  version: string
  previewImage: string
  tags: string[]
  /** 卡片尺寸 */
  size: TemplateSize | null
  /** 字段定义列表 */
  fields: TemplateField[]
  /** 主题映射表：字段值 → 主题配置 */
  themeMapping: Record<string, ThemeMappingItem> | null
  /** 主题映射来源字段（如 "type"） */
  themeField: string | null
  /** 模板资源 */
  assets: TemplateAssets | null
  /** 模板元数据 */
  metadata: TemplateMetadata
}

// ===================== 卡片数据 =====================

/** 卡片数据（用户填写的内容） */
export interface CardData {
  [key: string]: string
}

// ===================== API 响应 =====================

/** API 统一返回 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

// ===================== 图片编辑配置 =====================

/** 图片显示形状 */
export type ImageShape = 'rectangle' | 'circle' | 'rounded'

/** 裁剪区域（归一化坐标 0~1，相对于图片原始尺寸） */
export interface CropRegion {
  x: number
  y: number
  width: number
  height: number
}

/** 图片编辑配置 */
export interface ImageConfig {
  /** 显示形状 */
  shape: ImageShape
  /** 缩放比例 (0.5 ~ 3.0, 1.0 = 100%) */
  scale: number
  /** 图片在容器中的偏移位置 */
  position: { x: number; y: number }
  /** 裁剪区域（null 表示不裁剪） */
  crop: CropRegion | null
  /** 是否已应用编辑 */
  applied: boolean
}
