<script setup lang="ts">
/**
 * StoneFrame — 石质装饰外框
 *
 * 基于 Baldur'sGate2.css 的 .character-sheet 边框样式：
 * - 外框 18px 石头质感
 * - 内嵌金线
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  theme?: ResolvedTheme
}>()
</script>

<template>
  <div class="stone-frame">
    <!-- 内嵌装饰线 -->
    <div class="stone-frame__inner" />
  </div>
</template>

<style scoped>
.stone-frame {
  position: absolute;
  inset: 0;
  border: 18px solid #3a2a1b;
  pointer-events: none;
  z-index: 30;
  box-shadow:
    inset 0 0 40px rgba(0, 0, 0, 0.8),
    0 0 20px rgba(0, 0, 0, 0.6);
}

.stone-frame__inner {
  position: absolute;
  inset: 5px;
  border: 2px solid #9b8050;
  pointer-events: none;
}
</style>
