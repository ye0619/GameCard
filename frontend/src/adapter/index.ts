/**
 * 数据适配层
 *
 * 提供标准数据格式（CardViewModel）和数据适配器接口（CardDataAdapter），
 * 使模板系统与数据源解耦。
 *
 * 使用示例：
 *
 * ```ts
 * import { pokemonAdapter } from '@/adapter'
 *
 * // 从 API 获取的原始数据
 * const rawData = { name: '喷火龙', type: 'fire', wisdom: 80, ... }
 *
 * // 转换为模板标准格式
 * const vm = pokemonAdapter.transform(rawData)
 * // vm.nature → "暴躁"
 * // vm.stats  → { wisdom: 80, ... }
 * ```
 */

export type { CardViewModel, CardDataAdapter } from './types'
export { PokemonAdapter, pokemonAdapter } from './pokemon-adapter'
