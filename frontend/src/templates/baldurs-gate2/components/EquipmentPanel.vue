<script setup lang="ts">
/**
 * EquipmentPanel — 装备工具栏
 *
 * 基于 Baldur'sGate2.css 的 .toolbar 样式：
 * 左侧垂直排列的装备插槽，每个插槽可放图标。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  theme?: ResolvedTheme
}>()

const slots = [
  { name: '头盔', icon: '⛑' },
  { name: '武器', icon: '⚔' },
  { name: '护甲', icon: '🛡' },
  { name: '戒指', icon: '💍' },
  { name: '项链', icon: '📿' },
  { name: '靴子', icon: '👢' },
]
</script>

<template>
  <div class="toolbar">
    <div
      v-for="slot in slots"
      :key="slot.name"
      class="toolbar__slot"
      :title="slot.name"
    >
      <span class="toolbar__icon">{{ slot.icon }}</span>
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 4px;
}

.toolbar__slot {
  width: 50px;
  height: 50px;
  background: linear-gradient(145deg, #5a4a36, #21170d);
  border: 2px solid #806944;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.toolbar__slot:hover {
  border-color: #c8aa70;
}

.toolbar__icon {
  font-size: 22px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
  opacity: 0.8;
}
</style>
