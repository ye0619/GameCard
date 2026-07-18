<script setup lang="ts">
/**
 * StatusBar — 底部状态栏
 *
 * 基于 Baldur'sGate2.css 的 .status-panel 和 .status-box 样式：
 * 显示 HP、AC、Level 等关键数值。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'
import { computed } from 'vue'

const props = defineProps<{
  hp: string | number
  ac: string | number
  level: string | number
  theme: ResolvedTheme
}>()

const items = computed(() => [
  { label: 'HP', value: props.hp },
  { label: 'AC', value: props.ac },
  { label: 'LEVEL', value: props.level },
])
</script>

<template>
  <div class="status-bar">
    <div
      v-for="item in items"
      :key="item.label"
      class="status-box"
      :style="{ borderColor: theme.color + '66' }"
    >
      <div class="status-label">{{ item.label }}</div>
      <div class="status-number" :style="{ color: theme.color }">
        {{ item.value || '—' }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.status-bar {
  position: absolute;
  bottom: 38px;
  left: 110px;
  right: 25px;
  height: 38px;
  display: flex;
  justify-content: center;
  gap: 16px;
  z-index: 5;
}

.status-box {
  width: 120px;
  background: rgba(20, 15, 10, 0.85);
  border: 1px solid;
  text-align: center;
  padding: 3px 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.status-label {
  font-size: 12px;
  font-weight: bold;
  letter-spacing: 2px;
  opacity: 0.6;
}

.status-number {
  font-size: 20px;
  font-weight: bold;
}
</style>
