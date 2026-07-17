<script setup lang="ts">
/**
 * CardStats - 卡片属性统计组件
 *
 * 以数值条 + 六边形雷达图双模式展示角色属性。
 * 维度标签由模板配置驱动，不再硬编码。
 *
 * 设计：
 * - 接收 statFields 数组定义显示哪些属性及其标签
 * - 六边形雷达图颜色由主题驱动
 */
import { computed } from 'vue'
import type { ResolvedTheme } from '@/utils/themeResolver'
import type { TemplateField } from '@/types'

const props = defineProps<{
  /** 解析后的主题 */
  theme: ResolvedTheme
  /** 各属性值 */
  stats: Record<string, number>
  /** 属性字段定义列表（来自模板配置） */
  statFields: TemplateField[]
}>()

/** 是否有任何统计数据 */
const hasStats = computed(() =>
  props.statFields.some(s => props.stats[s.key] != null && props.stats[s.key] > 0),
)

/** 六边形雷达图的多边形点坐标（6 边形） */
function hexPoints(cx: number, cy: number, r: number, values: number[]): string {
  if (values.length < 6) return ''
  // 标准化值到 [0, 1]，最大 255
  const normalized = values.map(v => Math.min(v / 255, 1))
  return normalized
    .map((v, i) => {
      const angle = (Math.PI / 3) * i - Math.PI / 2
      const x = cx + r * v * Math.cos(angle)
      const y = cy + r * v * Math.sin(angle)
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

/** 六边形外框点 */
const OUTER_HEX = '110,10 200,55 200,145 110,190 20,145 20,55'

/** 数据六边形 */
const dataPolygon = computed(() => {
  const values = props.statFields.map(s => props.stats[s.key] ?? 0)
  return hexPoints(110, 100, 90, values)
})

/** 前 6 项属性（六边形雷达图最多支持 6 项） */
const displayFields = computed(() => props.statFields.slice(0, 6))
</script>

<template>
  <div v-if="hasStats" class="mb-4">
    <!-- 标题 -->
    <div class="flex items-center gap-2 mb-2">
      <div class="h-px flex-1" :style="{ backgroundColor: theme.color + '33' }" />
      <span class="text-xs font-medium text-gray-400 tracking-wider">基础属性</span>
      <div class="h-px flex-1" :style="{ backgroundColor: theme.color + '33' }" />
    </div>

    <!-- 数值条布局 -->
    <div class="grid grid-cols-2 gap-x-4 gap-y-1.5">
      <div
        v-for="field in displayFields"
        :key="field.key"
        class="flex items-center gap-2"
      >
        <span class="text-[10px] text-gray-400 w-8 shrink-0">{{ field.label }}</span>
        <div class="flex-1 h-1.5 bg-gray-700 rounded-full overflow-hidden">
          <div
            class="h-full rounded-full transition-all duration-500"
            :style="{
              width: Math.min((stats[field.key] ?? 0) / 255 * 100, 100) + '%',
              backgroundColor: theme.color,
            }"
          />
        </div>
        <span
          class="text-xs font-mono w-7 text-right"
          :style="{ color: theme.color }"
        >
          {{ stats[field.key] ?? 0 }}
        </span>
      </div>
    </div>

    <!-- 六边形雷达图（仅当恰好 6 项时展示） -->
    <div v-if="displayFields.length === 6" class="mt-3 flex justify-center">
      <svg
        class="w-44 h-36"
        viewBox="0 0 220 200"
        fill="none"
      >
        <!-- 外六边形背景 -->
        <polygon
          :points="OUTER_HEX"
          :fill="theme.color + '08'"
          :stroke="theme.color + '44'"
          stroke-width="2"
        />
        <!-- 数据六边形 -->
        <polygon
          v-if="dataPolygon"
          :points="dataPolygon"
          :fill="theme.color + '22'"
          :stroke="theme.color"
          stroke-width="2"
        />
        <!-- 中心点 -->
        <circle cx="110" cy="100" r="3" :fill="theme.color" />
      </svg>
    </div>
  </div>
</template>
