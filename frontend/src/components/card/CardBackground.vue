<script setup lang="ts">
/**
 * CardBackground - 卡片背景组件
 *
 * 基于 pokemon.css 的渐变色背景 + 主题背景图 + 辉光效果
 * 使用 absolute 铺满整个卡片容器
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
  if (bgImageUrl.value) {
    return {
      backgroundImage: `url(${bgImageUrl.value})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  return {
    background: `linear-gradient(135deg, ${props.theme.cssVars['--card-gradient-start']}, ${props.theme.cssVars['--card-gradient-end']})`,
    opacity: 0.15,
  }
})
</script>

<template>
  <!-- 主背景图 -->
  <div
    class="absolute inset-0 pointer-events-none z-0"
    :style="bgStyle"
  />

  <!-- 主题辉光效果（左上 + 右下） -->
  <div
    class="absolute -top-32 -right-24 w-80 h-80 rounded-full blur-3xl pointer-events-none z-[1]"
    :style="{ backgroundColor: theme.cssVars['--card-glow'] }"
  />
  <div
    class="absolute -bottom-32 -left-24 w-80 h-80 rounded-full blur-3xl pointer-events-none z-[1]"
    :style="{ backgroundColor: theme.cssVars['--card-glow'] }"
  />
</template>
