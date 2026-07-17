<script setup lang="ts">
/**
 * CardHeader - 卡片头部信息组件
 *
 * 基于 pokemon.css 的右上角信息区域：
 * - `.top-info` — CP / LV 数据面板
 *   固定在卡片右上角，显示战斗力(CP)和等级(LV=age)
 *
 * badges 格式: [{ label, value }]
 * 模板中 cp → "CP" 标签, age → "LV" 标签
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  name: string
  type: string
  theme: ResolvedTheme
  /** 右上角徽章列表（如 cp, age 等） */
  badges: { label: string; value: string }[]
}>()
</script>

<template>
  <!-- 右上角信息面板 -->
  <div class="top-info">
    <div
      v-for="badge in badges"
      :key="badge.label"
      class="info-box"
      :class="{ 'is-cp': badge.label.toLowerCase() === 'cp' }"
    >
      <div class="info-box__label">{{ badge.label }}</div>
      <div class="info-box__value" :style="{ color: theme.color }">
        {{ badge.value }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.top-info {
  position: absolute;
  right: 32px;
  top: 18px;
  display: flex;
  gap: 24px;
  z-index: 20;
}

.info-box {
  text-align: center;
}

.info-box__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.5);
  margin-bottom: 2px;
}

.info-box__value {
  font-size: 30px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.info-box.is-cp .info-box__value {
  background: rgba(255, 255, 255, 0.7);
  color: #555 !important;
  padding: 2px 16px;
  border-radius: 30px;
  font-size: 28px;
}
</style>
