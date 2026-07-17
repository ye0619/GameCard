<script setup lang="ts">
/**
 * CardBackground - 卡片背景组件
 */
import { computed } from 'vue'
import type { ResolvedTheme } from '@/utils/themeResolver'
import { getAssetUrl } from '@/utils/themeResolver'
import type { Template } from '@/types'

const props = defineProps<{
  theme: ResolvedTheme
  template: Template | null
}>()

const bgImageUrl = computed(() => {
  if (!props.theme.mapping?.background) return ''
  return getAssetUrl(props.template, props.theme.mapping.background, 'backgrounds')
})

const bgStyle = computed(() => {
  const vars = props.theme.cssVars
  if (bgImageUrl.value) {
    return {
      backgroundImage: `url(${bgImageUrl.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  return {
    background: `linear-gradient(135deg, ${vars['--card-gradient-start']}, ${vars['--card-gradient-end']})`,
    opacity: 0.08,
  }
})
</script>

<template>
  <div class="absolute inset-0 overflow-hidden pointer-events-none">
    <div
      class="absolute -top-20 -right-20 w-60 h-60 rounded-full blur-3xl"
      :style="{ backgroundColor: theme.cssVars['--card-glow'] }"
    />
    <div
      class="absolute -bottom-20 -left-20 w-60 h-60 rounded-full blur-3xl"
      :style="{ backgroundColor: theme.cssVars['--card-glow'] }"
    />
    <div
      class="absolute inset-0"
      :style="bgStyle"
    />
  </div>
</template>
