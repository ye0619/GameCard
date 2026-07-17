<script setup lang="ts">
/**
 * CardHeader - 卡片头部组件
 *
 * 名称和主题标签为核心展示，右侧徽章（age/cp/level 等）
 * 从 badges 数组动态渲染，由模板配置驱动。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'
import { getAssetUrl } from '@/utils/themeResolver'
import type { Template } from '@/types'

defineProps<{
  name: string
  type: string
  theme: ResolvedTheme
  template: Template | null
  /** 右侧徽章列表（如 age, cp 等，由模板配置驱动） */
  badges: { label: string; value: string }[]
}>()
</script>

<template>
  <div class="flex items-start justify-between mb-3">
    <!-- 名称 + 属性 -->
    <div class="min-w-0 flex-1">
      <h2
        class="text-2xl font-bold tracking-tight truncate"
        :style="{ color: theme.color }"
      >
        {{ name }}
      </h2>

      <!-- 属性标签 -->
      <div v-if="type" class="flex items-center gap-1.5 mt-1">
        <span
          class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold text-white shadow-sm"
          :style="{ backgroundColor: theme.color }"
        >
          <!-- 图标 -->
          <img
            v-if="theme.icon && getAssetUrl(template, theme.icon, 'icons')"
            :src="getAssetUrl(template, theme.icon, 'icons')"
            class="w-3.5 h-3.5"
            alt=""
          />
          {{ type }}
        </span>
      </div>
    </div>

    <!-- 右侧徽章（动态：age, cp, level 等来自模板配置） -->
    <div
      v-if="badges.length > 0"
      class="flex items-center gap-3 shrink-0 ml-3"
    >
      <div
        v-for="badge in badges"
        :key="badge.label"
        class="text-center"
      >
        <div class="text-[10px] text-gray-400 uppercase tracking-wider">{{ badge.label }}</div>
        <div
          class="text-lg font-bold"
          :style="{ color: theme.color }"
        >
          {{ badge.value }}
        </div>
      </div>
    </div>
  </div>
</template>
