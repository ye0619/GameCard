/**
 * PokemonAdapter - 宝可梦数据适配器
 *
 * 将宝可梦格式的原始数据转换为模板标准格式 CardViewModel。
 *
 * 处理：
 * - type/nature → nature 映射（"fire" → "暴躁"）
 * - 六维属性提取（wisdom, constitution, perception, luck, charm, strength）
 * - skills 多格式解析
 *
 * 使用场景：
 * - 从数据库加载已保存的宝可梦卡片数据
 * - 从外部 API 导入宝可梦数据
 * - 模板预览时生成示例数据
 */
import type { CardViewModel, CardDataAdapter } from './types'

/**
 * 性格映射表：元素类型 → 性格名称
 *
 * 当原始数据使用旧版元素键（如 "fire"）时，
 * 适配器自动将其映射为模板使用的性格名（如 "暴躁"）。
 */
const NATURE_MAP: Record<string, string> = {
  fire: '暴躁',
  water: '温柔',
  grass: '活泼',
  electric: '体贴',
  psychic: '偏激',
  fighting: '乐观',
  rock: '沉稳',
  ground: '可靠',
  flying: '随性',
  bug: '平和',
  poison: '优雅',
  normal: '中庸',
  ghost: '高尚',
  ice: '高冷',
  dragon: '深沉',
  dark: '懦弱',
  steel: '阴郁',
  fairy: '呆萌',
}

/** 六维属性字段列表 */
const STAT_KEYS = ['wisdom', 'constitution', 'perception', 'luck', 'charm', 'strength'] as const

export class PokemonAdapter implements CardDataAdapter {
  readonly id = 'pokemon'
  readonly name = '宝可梦适配器'

  /**
   * 判断原始数据是否为宝可梦格式
   */
  canHandle(raw: unknown): raw is Record<string, unknown> {
    if (typeof raw !== 'object' || raw === null) return false
    const data = raw as Record<string, unknown>
    // 有 name 字段且含 type 或 nature 字段，判定为宝可梦数据
    return typeof data.name === 'string' && (
      typeof data.type === 'string' || typeof data.nature === 'string'
    )
  }

  /**
   * 转换为标准 CardViewModel
   */
  transform(raw: Record<string, unknown>): CardViewModel {
    return {
      name: String(raw.name ?? ''),
      nature: this.resolveNature(raw),
      stats: this.extractStats(raw),
      skills: this.parseSkills(raw.skills),
      description: String(raw.description ?? ''),
      image: this.resolveImage(raw),
      extras: this.extractExtras(raw),
    }
  }

  /**
   * 解析性格值：优先用 nature 字段，回退用 type 字段映射
   */
  private resolveNature(raw: Record<string, unknown>): string {
    // 如果已有 nature 直接使用
    if (raw.nature && typeof raw.nature === 'string') {
      return raw.nature
    }
    // 如果是旧版 type 字段，查映射表
    if (raw.type && typeof raw.type === 'string') {
      return NATURE_MAP[raw.type.toLowerCase()] ?? raw.type
    }
    return ''
  }

  /**
   * 提取六维属性值
   */
  private extractStats(raw: Record<string, unknown>): Record<string, number> {
    const stats: Record<string, number> = {}
    for (const key of STAT_KEYS) {
      const val = raw[key]
      stats[key] = typeof val === 'number' ? val : (Number(val) || 0)
    }
    return stats
  }

  /**
   * 解析技能数据（支持多种格式）
   */
  private parseSkills(raw: unknown): string[] {
    if (!raw) return []

    // JSON 数组字符串
    if (typeof raw === 'string') {
      if (raw.startsWith('[')) {
        try {
          return JSON.parse(raw).map((item: unknown) =>
            typeof item === 'string' ? item : String((item as Record<string, unknown>).name ?? ''),
          ).filter(Boolean)
        } catch { /* fall through */ }
      }
      // 逗号分隔
      return raw.split(',').map(s => s.trim()).filter(Boolean)
    }

    // 已经是数组
    if (Array.isArray(raw)) {
      return raw.map(item =>
        typeof item === 'string' ? item : String((item as Record<string, unknown>).name ?? ''),
      ).filter(Boolean)
    }

    return []
  }

  /**
   * 解析图片 URL
   */
  private resolveImage(raw: Record<string, unknown>): string | null {
    const image = raw.image
    if (typeof image === 'string' && image.length > 0) return image
    return null
  }

  /**
   * 提取额外展示字段
   */
  private extractExtras(raw: Record<string, unknown>): Record<string, string> {
    const extras: Record<string, string> = {}
    if (raw.level) extras['level'] = String(raw.level)
    if (raw.cp) extras['cp'] = String(raw.cp)
    return extras
  }
}

/** 单例导出 */
export const pokemonAdapter = new PokemonAdapter()
