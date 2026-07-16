/** 模板字段定义 */
export interface TemplateField {
  key: string
  label: string
  type: 'TEXT' | 'TEXTAREA' | 'IMAGE' | 'NUMBER' | 'SELECT' | 'COLOR'
  required: boolean
  defaultValue: string | null
  placeholder: string | null
  options: string | null // JSON 数组字符串
}

/** 模板元数据 */
export interface TemplateMetadata {
  createTime: string
  updateTime: string
  templateVersion: string
  minAppVersion: string
}

/** 模板 */
export interface Template {
  id: string
  name: string
  description: string
  author: string
  version: string
  previewImage: string
  tags: string[]
  fields: TemplateField[]
  metadata: TemplateMetadata
}

/** 卡片数据（用户填写的内容） */
export interface CardData {
  [key: string]: string
}

/** API 统一返回 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
