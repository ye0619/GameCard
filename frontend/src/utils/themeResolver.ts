/**
 * 主题解析器
 *
 * 核心职责：根据模板配置和卡片数据，动态解析视觉主题。
 *
 * 设计原则：
 * - 零硬编码：禁止在组件中写 if(type==="fire") 条件判断
 * - 配置驱动：所有主题映射来自 template.themeMapping
 * - 可扩展：新增属性类型只需扩展 template.json，无需修改代码
 *
 * 使用示例：
 *   const theme = resolveTheme(template, cardData)
 *   // theme.color → "#EF4444"
 *   // theme.background → "fire-bg"
 *   // theme.icon → "fire"
 *   // theme.cssVars → { '--card-primary': '#EF4444', ... }
 */

import type { Template, CardData, ThemeMappingItem } from '@/types'

/** 解析后的主题结果 */
export interface ResolvedTheme {
  /** 命中的主题映射条目（可能为 null） */
  mapping: ThemeMappingItem | null
  /** 主色（有映射返回映射色，无映射返回默认色） */
  color: string
  /** 辅助色 */
  secondaryColor: string
  /** 图标标识 */
  icon: string
  /** 背景标识 */
  background: string
  /** 转换为 CSS 自定义属性 */
  cssVars: Record<string, string>
}

/** 默认主题（无映射时的后备值） */
const DEFAULT_THEME: ResolvedTheme = {
  mapping: null,
  color: '#6366F1',
  secondaryColor: '#4F46E5',
  icon: '',
  background: '',
  cssVars: {
    '--card-primary': '#6366F1',
    '--card-secondary': '#4F46E5',
    '--card-gradient-start': '#818CF8',
    '--card-gradient-end': '#4F46E5',
    '--card-glow': 'rgba(99, 102, 241, 0.3)',
    '--card-background': '#1F2937',
  },
}

/**
 * 根据模板配置和卡片数据解析主题
 *
 * 流程：
 * 1. 读取 template.themeField，确定用哪个字段做映射键（如 "type"）
 * 2. 从 cardData 中获取该字段的值（如 "火"）
 * 3. 在 template.themeMapping 中查找对应主题
 * 4. 将主题条目转换为 CSS 变量
 *
 * @param template  当前选中的模板
 * @param cardData  用户填写的卡片数据
 * @returns         解析后的主题
 */
export function resolveTheme(
  template: Template | null,
  cardData: CardData,
): ResolvedTheme {
  if (!template || !template.themeMapping || !template.themeField) {
    return DEFAULT_THEME
  }

  const fieldValue = cardData[template.themeField]
  if (!fieldValue || !template.themeMapping[fieldValue]) {
    return DEFAULT_THEME
  }

  const mapping = template.themeMapping[fieldValue]
  const styles = mapping.styles ?? {}

  return {
    mapping,
    color: mapping.color ?? DEFAULT_THEME.color,
    secondaryColor: mapping.secondaryColor ?? DEFAULT_THEME.secondaryColor,
    icon: mapping.icon ?? '',
    background: mapping.background ?? '',
    cssVars: {
      '--card-primary': mapping.color ?? DEFAULT_THEME.color,
      '--card-secondary': mapping.secondaryColor ?? DEFAULT_THEME.secondaryColor,
      '--card-gradient-start': styles['gradient-start'] ?? mapping.color ?? DEFAULT_THEME.color,
      '--card-gradient-end': styles['gradient-end'] ?? mapping.secondaryColor ?? DEFAULT_THEME.secondaryColor,
      '--card-glow': styles['glow-color'] ?? DEFAULT_THEME.cssVars['--card-glow'],
      '--card-background': '#1F2937',
    },
  }
}

/**
 * 获取模板资产的实际 URL
 *
 * 通过 template.assets 查找资源路径，并返回可访问的 URL。
 * Spring Boot 服务 static/ 目录下的资源到根路径，
 * 所以 relative path "backgrounds/fire.svg" → "/templates/pokemon/backgrounds/fire.svg"
 *
 * @param template  模板
 * @param assetKey  资产标识符（如 "fire-bg"）
 * @param category  资产类别（如 "backgrounds"、"icons"）
 * @returns         资产 URL 或空字符串
 */
export function getAssetUrl(
  template: Template | null,
  assetKey: string | undefined,
  category: 'backgrounds' | 'icons' | 'frames',
): string {
  if (!template || !assetKey || !template.assets) {
    return ''
  }

  const assetMap = template.assets[category]
  if (!assetMap || !assetMap[assetKey]) {
    return ''
  }

  const path = assetMap[assetKey]
  // 相对路径：拼接到 /templates/{templateId}/
  // Spring Boot 的 static/ 目录资源可直接通过根路径访问
  if (path.startsWith('backgrounds/') || path.startsWith('icons/') || path.startsWith('frames/')) {
    return `/templates/${template.id}/${path}`
  }
  return path
}

/**
 * 解析技能数据
 *
 * 支持两种格式：
 * 1. 逗号分隔字符串: "火焰吐息,烈焰领域"
 * 2. JSON 数组字符串: '[{"name":"火焰吐息"},{"name":"烈焰领域"}]'
 * 3. 纯文本换行分割
 *
 * @param raw  原始技能数据
 * @returns    技能名称数组
 */
export function parseSkills(raw: string | undefined): string[] {
  if (!raw || raw.trim() === '') return []

  const trimmed = raw.trim()

  // 尝试 JSON 数组解析
  if (trimmed.startsWith('[')) {
    try {
      const parsed = JSON.parse(trimmed)
      if (Array.isArray(parsed)) {
        return parsed.map((item: unknown) => {
          if (typeof item === 'string') return item
          if (typeof item === 'object' && item !== null && 'name' in item) {
            return String((item as Record<string, unknown>).name)
          }
          return String(item)
        }).filter(Boolean)
      }
    } catch {
      // fall through to other parsers
    }
  }

  // 逗号分隔
  if (trimmed.includes(',')) {
    return trimmed.split(',').map(s => s.trim()).filter(Boolean)
  }

  // 换行分割
  if (trimmed.includes('\n')) {
    return trimmed.split('\n').map(s => s.trim()).filter(Boolean)
  }

  // 单条技能
  return [trimmed]
}
