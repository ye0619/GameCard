<script setup lang="ts">
/**
 * CardDescription - 卡片描述组件
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  description: string
  theme: ResolvedTheme
}>()
</script>

<template>
  <div v-if="description" class="mb-3">
    <div class="flex items-center gap-1.5 mb-1.5">
      <span class="text-xs font-medium tracking-wider" :style="{ color: theme.color + '99' }">图鉴描述</span>
      <div class="h-px flex-1" :style="{ backgroundColor: theme.color + '33' }" />
    </div>
    <p
      class="text-xs leading-relaxed pl-3 border-l-2 italic"
      :style="{
        color: theme.color + 'CC',
        borderColor: theme.color + '66',
      }"
    >
      {{ description }}
    </p>
  </div>
</template>
