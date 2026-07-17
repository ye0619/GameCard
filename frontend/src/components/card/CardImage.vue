<script setup lang="ts">
/**
 * CardImage - 卡片图片组件
 */
defineProps<{
  image: string | null
  name: string
}>()
</script>

<template>
  <div
    class="relative rounded-xl overflow-hidden mb-4 bg-gray-800/50 border border-gray-700"
    style="aspect-ratio: 16 / 10;"
  >
    <img
      v-if="image"
      :src="image"
      :alt="name || '角色图片'"
      class="w-full h-full object-contain p-4"
    />

    <div
      v-else
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
