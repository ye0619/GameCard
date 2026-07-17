/**
 * 数据适配层类型定义
 *
 * 职责：定义模板系统消费的标准数据格式，屏蔽原始数据来源差异。
 *
 * 数据流：
 *   原始数据（API/DB/Import）
 *     → CardDataAdapter.transform()
 *       → CardViewModel（模板消费的标准格式）
 *         → CardRenderer 渲染
 */

/**
 * CardViewModel - 模板消费的标准数据格式
 *
 * 模板只认识这个接口，不关心原始数据是从哪里来的。
 * 不同的数据源通过各自的 Adapter 转换为该格式。
 */
export interface CardViewModel {
  /** 角色/卡片名称 */
  name: string

  /** 主题映射标识（对应 template.themeField 的值，如 "暴躁"） */
  nature: string

  /** 属性值（key → 数值，key 与 template.statFields 对应） */
  stats: Record<string, number>

  /** 技能描述列表 */
  skills: string[]

  /** 描述/背景文本 */
  description: string

  /** 图片 URL */
  image: string | null

  /** 额外展示字段（如 CP、Level 等非统计类字段） */
  extras: Record<string, string>
}

/**
 * CardDataAdapter - 数据适配器接口
 *
 * 每种数据源/格式实现一个适配器。
 * 适配器负责：
 * 1. 字段名映射（如 raw.type → nature）
 * 2. 值转换（如 "fire" → "暴躁"）
 * 3. 数据归一化（如 skills 解析）
 *
 * @typeParam T 原始数据类型，默认为扁平 Record
 */
export interface CardDataAdapter<T = Record<string, unknown>> {
  /** 适配器唯一标识 */
  readonly id: string

  /** 适配器名称（用于展示） */
  readonly name: string

  /**
   * 将原始数据转换为模板可消费的标准格式
   *
   * @param raw   原始数据（可能来自 API、DB、导入等）
   * @returns     标准化的 CardViewModel
   */
  transform(raw: T): CardViewModel

  /**
   * 判断该适配器是否能处理给定的原始数据
   *
   * @param raw   原始数据
   * @returns     是否可以处理
   */
  canHandle(raw: unknown): raw is T
}
