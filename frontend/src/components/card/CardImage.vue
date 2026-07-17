<script setup lang="ts">
/**
 * CardImage - 卡片图片组件
 */
defineProps<{
  image: string | null
  name: string
  /** 图片变换样式（缩放/位移，来自编辑器 pan 模式） */
  imgStyle?: Record<string, string>
}>()
</script>

<template>
  <!-- 有图片时：无背景遮罩，图片自然地浮在卡片上 -->
  <div
    v-if="image"
    class="relative mb-4"
  >
    <img
      :src="image"
      :alt="name || '角色图片'"
      class="w-full h-auto object-contain"
      :style="imgStyle"
    />
  </div>

  <!-- 无图片时：显示空状态占位 -->
  <div
    v-else
    class="relative rounded-xl overflow-hidden mb-4 bg-gray-800/50 border border-gray-700"
    style="aspect-ratio: 16 / 10;"
  >
    <div
      class="flex flex-col items-center justify-center h-full text-gray-600 gap-2"
    >
      <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
      </svg>
      <span class="text-xs">暂无图片</span>
    </div>
  </div>
</template>
