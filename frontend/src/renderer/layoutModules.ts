/**
 * 模板布局模块动态加载映射
 *
 * 将 import.meta.glob 放在单独的 .ts 文件中，
 * 避免 vue-tsc 在 .vue 文件中解析 glob 语法时的类型错误。
 *
 * 运行时，Vite 将此 glob 替换为一个 Record<path, loader> 对象，
 * keys 为匹配的文件路径，values 为动态 import 函数。
 */
const modules = import.meta.glob('/src/templates/*/layout.vue')

export default modules as Record<string, () => Promise<{ default: any }>>
