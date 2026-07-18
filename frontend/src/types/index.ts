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
  /** 主题映射来源字段（如 "nature"） */
  themeField: string | null

  /** 属性统计字段列表（用于雷达图/数值条展示） */
  statFields: string[] | null
  /** 模板资源 */
  assets: TemplateAssets | null
  /** 模板元数据 */
  metadata: TemplateMetadata

  /** 预设技能列表（快捷选择） */
  presetSkills?: PresetSkill[] | null
  /** 预设角色简介列表（快捷选择） */
  presetIntroductions?: PresetIntroduction[] | null

  /** AI 图像处理配置（包含风格提示词等） */
  imageConfig?: TemplateImageConfig | null
}

// ===================== 预设技能 =====================

/** 预设技能定义 */
export interface PresetSkill {
  /** 技能名称 */
  name: string
  /** 技能分类（如 "攻击", "防御", "辅助", "特殊"） */
  category?: string
  /** 技能描述 */
  description?: string
  /** 威力等级（1-100） */
  power?: number
}

// ===================== 预设简介 =====================

/** 预设角色简介 */
export interface PresetIntroduction {
  /** 预设标题 */
  title: string
  /** 简介/背景故事内容 */
  content: string
  /** 建议的性格搭配 */
  natureHint?: string
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

// ===================== AI 图像处理 =====================

/** 模板图片处理配置（从 template.json / imageConfig.json 读取） */
export interface TemplateImageConfig {
  /** AI 风格提示词，发送给 AI 的 prompt */
  imagePrompt: string
  /** 处理选项 */
  processing?: {
    /** 是否需要先去除背景再传给 AI */
    removeBackground?: boolean
    /** 偏好的宽高比，如 "1:1", "16:9" */
    preferredRatio?: string
  }
}

/** 用户自有的 AI API 配置（仅前端存储，不发送到后端持久化） */
export interface AiUserConfig {
  /** API 端点地址，如 https://api.openai.com/v1/images/generations */
  endpoint: string
  /** API Key */
  apiKey: string
  /** 模型名称，如 dall-e-3 */
  model: string
  /** 是否已配置完成 */
  configured: boolean
}

/** AI 图片处理请求 */
export interface AiProcessRequest {
  /** 原始图片 base64 */
  imageUrl: string
  /** 模板 ID，用于查找 imagePrompt */
  templateId: string
  /** 操作类型 */
  operation: 'style'
  /** 自定义提示词（用户编辑，为空则使用模板默认） */
  prompt?: string
  /** 用户 AI 配置（后端透传） */
  aiConfig: {
    endpoint: string
    apiKey: string
    model: string
  }
}

/** AI 图片处理响应 */
export interface AiProcessResponse {
  /** 处理后的图片 URL（base64 data URI，用于持久化） */
  imageUrl: string
  /** 原始图片 URL（AI API 直接返回，优先用于前端显示） */
  imageSource?: string
}

/** 图片处理模式 */
export type ImageProcessMode = 'original' | 'basic' | 'ai'

// ===================== 保存的作品 =====================

/** 保存的作品（用户创作记录） */
export interface SavedWork {
  /** 唯一标识 */
  id: string
  /** 卡片名称 */
  name: string
  /** 使用的模板 ID */
  templateId: string
  /** 模板名称（显示用） */
  templateName: string
  /** 卡片所有字段数据 */
  cardData: Record<string, string>
  /** 角色图片 base64 data URI */
  imageData: string | null
  /** 图片编辑配置（形状、缩放、位置、裁剪） */
  imageConfig: ImageConfig
  /** 创建时间 ISO 字符串 */
  createdAt: string
  /** 更新时间 ISO 字符串 */
  updatedAt: string
}

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
