<script setup lang="ts">
/**
 * CardContainer - 卡片容器组件
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  theme: ResolvedTheme
}>()
</script>

<template>
  <div
    class="relative rounded-2xl overflow-hidden shadow-2xl border-2 transition-all duration-300"
    :style="{ borderColor: theme.color }"
  >
    <!-- 顶部装饰条 -->
    <div
      class="h-2 relative z-10"
      :style="{
        background: `linear-gradient(90deg, ${theme.cssVars['--card-gradient-start']}, ${theme.cssVars['--card-gradient-end']})`,
      }"
    />

    <!-- 卡片内层 -->
    <div class="relative bg-gradient-to-b from-gray-900 to-gray-950 p-5">
      <slot />
    </div>
  </div>
</template>
