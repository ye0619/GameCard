<script setup lang="ts">
/**
 * CardHeader - 卡片头部组件
 *
 * 显示角色名称和属性标签。
 * 名称颜色、属性标签样式由主题驱动。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'
import { getAssetUrl } from '@/utils/themeResolver'
import type { Template } from '@/types'

defineProps<{
  name: string
  type: string
  theme: ResolvedTheme
  template: Template | null
  level: string
  cp: string
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

    <!-- CP + Level -->
    <div class="flex items-center gap-3 shrink-0 ml-3">
      <div
        v-if="level"
        class="text-center"
      >
        <div class="text-[10px] text-gray-400 uppercase tracking-wider">Lv</div>
        <div
          class="text-lg font-bold"
          :style="{ color: theme.color }"
        >
          {{ level }}
        </div>
      </div>
      <div
        v-if="cp"
        class="text-center"
      >
        <div class="text-[10px] text-gray-400 uppercase tracking-wider">CP</div>
        <div
          class="text-lg font-bold"
          :style="{ color: theme.color }"
        >
          {{ cp }}
        </div>
      </div>
    </div>
  </div>
</template>
