<script setup lang="ts">
/**
 * AttributePanel — D&D 六维属性面板
 *
 * 基于 Baldur'sGate2.css 的 .attributes 和 .attribute-row 样式：
 * 左侧属性名 + 右侧数值条
 * 颜色根据 theme.color 动态变化
 */
import { computed } from 'vue'
import type { ResolvedTheme } from '@/utils/themeResolver'

const props = defineProps<{
  stats: { key: string; label: string; value: number }[]
  theme: ResolvedTheme
  maxValue?: number
}>()

const maxVal = computed(() => props.maxValue ?? 25)

/** 能力值缩写映射 */
const abbrMap: Record<string, string> = {
  strength: 'STR',
  dexterity: 'DEX',
  constitution: 'CON',
  intelligence: 'INT',
  wisdom: 'WIS',
  charisma: 'CHA',
}
</script>

<template>
  <div class="attributes-panel" :style="{ borderColor: theme.color + '66' }">
    <div class="attributes-title" :style="{ color: theme.color }">
      属性
    </div>
    <div
      v-for="stat in stats"
      :key="stat.key"
      class="attribute-row"
    >
      <span class="attribute-label" :style="{ color: theme.color }">
        {{ abbrMap[stat.key] ?? stat.label }}
      </span>
      <span class="attribute-value">{{ stat.value }}</span>
      <div class="attribute-bar-bg">
        <div
          class="attribute-bar-fill"
          :style="{
            width: Math.min(100, (stat.value / maxVal) * 100) + '%',
            backgroundColor: theme.color,
          }"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.attributes-panel {
  background: rgba(20, 15, 10, 0.75);
  border: 2px solid;
  padding: 10px 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.attributes-title {
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 4px;
  text-align: center;
  margin-bottom: 6px;
  border-bottom: 1px solid;
  padding-bottom: 4px;
  opacity: 0.8;
}

.attribute-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 15px;
}

.attribute-label {
  font-weight: bold;
  width: 36px;
  flex-shrink: 0;
}

.attribute-value {
  width: 22px;
  text-align: right;
  font-weight: bold;
  color: #e54b35;
  flex-shrink: 0;
}

.attribute-bar-bg {
  flex: 1;
  height: 8px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  overflow: hidden;
}

.attribute-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.4s ease;
  opacity: 0.8;
}
</style>
