<script setup lang="ts">
/**
 * CardStats - 卡片六维属性雷达图组件
 *
 * 基于 pokemon.css 的雷达图样式：
 * - 420×420 六边形雷达（由 CardRenderer 定位）
 * - 网格线 + 数据填充 + 高亮描边
 * - 六维属性标签环绕
 * - 属性数值显示在雷达图顶点附近
 */
import { computed } from 'vue'
import type { ResolvedTheme } from '@/utils/themeResolver'
import type { TemplateField } from '@/types'

const props = defineProps<{
  theme: ResolvedTheme
  stats: Record<string, number>
  statFields: TemplateField[]
  /** 强制显示雷达图（默认：恰好 6 项时自动显示） */
  showRadar?: boolean
  /** 数值最大值刻度（默认 100） */
  maxValue?: number
}>()

/** 归一化到 0-1 */
const normalized = (v: number, max: number) => Math.min(v / max, 1)

/** 前 6 项属性（雷达图最多支持 6 项） */
const displayFields = computed(() => props.statFields.slice(0, 6))

/** 是否有任何统计数据 */
const hasStats = computed(() =>
  displayFields.value.some(s => props.stats[s.key] != null && props.stats[s.key] > 0),
)

/** 是否显示雷达图 */
const showRadarChart = computed(() => {
  if (props.showRadar === false) return false          // 显式禁用
  if (props.showRadar === true) return true             // 显式启用
  return displayFields.value.length === 6                // 默认：恰好 6 项时显示
})

/** 雷达图参数 */
const CX = 210
const CY = 210
const RADIUS = 150

/** 六边形网格层数（由外到内 3 层） */
const GRID_LAYERS = [1, 0.66, 0.33]

/** 生成六边形点集 */
function hexPoints(cx: number, cy: number, r: number): string {
  return Array.from({ length: 6 }, (_, i) => {
    const angle = (Math.PI / 3) * i - Math.PI / 2
    const x = cx + r * Math.cos(angle)
    const y = cy + r * Math.sin(angle)
    return `${x.toFixed(1)},${y.toFixed(1)}`
  }).join(' ')
}

/** 数据多边形（归一化到 0-1） */
const maxVal = computed(() => props.maxValue ?? 100)

const dataPolygon = computed(() => {
  if (displayFields.value.length < 6) return ''
  const values = displayFields.value.map(s => {
    const v = props.stats[s.key] ?? 0
    return normalized(v, maxVal.value)
  })
  return values.map((v, i) => {
    const angle = (Math.PI / 3) * i - Math.PI / 2
    const x = CX + RADIUS * v * Math.cos(angle)
    const y = CY + RADIUS * v * Math.sin(angle)
    return `${x.toFixed(1)},${y.toFixed(1)}`
  }).join(' ')
})

/** 标签位置（顶点外扩） */
const labelPositions = computed(() => {
  return displayFields.value.map((field, i) => {
    const angle = (Math.PI / 3) * i - Math.PI / 2
    const offset = RADIUS + 28
    const x = CX + offset * Math.cos(angle)
    const y = CY + offset * Math.sin(angle)
    const raw = props.stats[field.key] ?? 0
    const val = Math.min(raw, maxVal.value)
    // 数值位置（沿半径向内偏移）
    const vOffset = 30
    const vx = CX + vOffset * Math.cos(angle)
    const vy = CY + vOffset * Math.sin(angle)
    return { label: field.label, value: val, x, y, vx, vy, angle }
  })
})
</script>

<template>
  <div v-if="hasStats && showRadarChart">
    <svg
      class="radar-svg"
      viewBox="0 0 420 420"
      fill="none"
    >
      <!-- 3 层六边形网格 -->
      <polygon
        v-for="layer in GRID_LAYERS"
        :key="layer"
        :points="hexPoints(CX, CY, RADIUS * layer)"
        class="radar-grid"
        :style="{ stroke: theme.color + '55' }"
      />

      <!-- 从中心到顶点的放射线 -->
      <line
        v-for="i in 6"
        :key="'line-' + i"
        :x1="CX"
        :y1="CY"
        :x2="CX + RADIUS * Math.cos((Math.PI / 3) * (i - 1) - Math.PI / 2)"
        :y2="CY + RADIUS * Math.sin((Math.PI / 3) * (i - 1) - Math.PI / 2)"
        class="radar-grid"
        :style="{ stroke: theme.color + '44' }"
      />

      <!-- 数据多边形（填充） -->
      <polygon
        v-if="dataPolygon"
        :points="dataPolygon"
        class="radar-value"
        :style="{
          fill: theme.color + '35',
          stroke: theme.color,
        }"
      />

      <!-- 数据多边形描边（发光边框效果） -->
      <polygon
        v-if="dataPolygon"
        :points="dataPolygon"
        class="radar-border"
        :style="{ stroke: theme.color }"
      />

      <!-- 顶点圆点 -->
      <circle
        v-for="i in 6"
        :key="'dot-' + i"
        :cx="CX + RADIUS * Math.cos((Math.PI / 3) * (i - 1) - Math.PI / 2)"
        :cy="CY + RADIUS * Math.sin((Math.PI / 3) * (i - 1) - Math.PI / 2)"
        r="5"
        :fill="theme.color"
        opacity="0.8"
      />

      <!-- 数据顶点圆点 -->
      <circle
        v-for="(pos, i) in labelPositions"
        :key="'data-dot-' + i"
        :cx="pos.vx"
        :cy="pos.vy"
        r="6"
        :fill="theme.color"
        stroke="white"
        stroke-width="2"
      />

      <!-- 属性标签环绕 -->
      <text
        v-for="pos in labelPositions"
        :key="'label-' + pos.label"
        :x="pos.x"
        :y="pos.y"
        class="stat-text"
        :fill="theme.color"
        text-anchor="middle"
        dominant-baseline="middle"
      >
        {{ pos.label }}
      </text>

      <!-- 属性数值 -->
      <text
        v-for="pos in labelPositions"
        :key="'val-' + pos.label"
        :x="pos.vx"
        :y="pos.vy - 14"
        class="stat-number"
        fill="white"
        text-anchor="middle"
        dominant-baseline="middle"
      >
        {{ pos.value }}
      </text>
    </svg>

    <!-- 图例：属性名 → 值列表（紧凑横排） -->
    <div class="radar-legend">
      <div
        v-for="pos in labelPositions"
        :key="'legend-' + pos.label"
        class="radar-legend__item"
      >
        <span class="radar-legend__dot" :style="{ backgroundColor: theme.color }" />
        <span class="radar-legend__label">{{ pos.label }}</span>
        <span class="radar-legend__value" :style="{ color: theme.color }">{{ pos.value }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.radar-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3));
}

.radar-grid {
  fill: none;
  stroke-width: 2;
}

.radar-value {
  stroke-width: 4;
  transition: all 0.5s ease;
}

.radar-border {
  fill: none;
  stroke-width: 6;
  opacity: 0.6;
}

.stat-text {
  font-size: 18px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.stat-number {
  font-size: 22px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.9);
}

/* 图例横排 */
.radar-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 8px;
  justify-content: center;
}

.radar-legend__item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.radar-legend__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.radar-legend__label {
  color: rgba(255, 255, 255, 0.6);
}

.radar-legend__value {
  font-weight: bold;
  font-family: monospace;
}
</style>
