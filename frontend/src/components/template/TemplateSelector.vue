<script setup lang="ts">
import { useCardStore } from '@/stores/card'

const store = useCardStore()
</script>

<template>
  <div class="space-y-2">
    <label class="text-sm font-medium text-gray-300">选择模板</label>

    <!-- 加载中 -->
    <div v-if="store.loading" class="text-sm text-gray-500 py-2">加载中...</div>

    <!-- 模板列表 -->
    <div v-else class="grid grid-cols-1 gap-2">
      <button
        v-for="tpl in store.templates"
        :key="tpl.id"
        @click="store.selectTemplate(tpl.id)"
        :class="[
          'flex items-center gap-3 px-4 py-3 rounded-lg border text-left transition-all',
          store.selectedTemplateId === tpl.id
            ? 'border-blue-500 bg-blue-500/10 text-blue-300'
            : 'border-gray-700 bg-gray-800/50 text-gray-300 hover:border-gray-500 hover:bg-gray-800',
        ]"
      >
        <!-- 模板缩略图占位 -->
        <div
          :class="[
            'w-10 h-10 rounded-md flex items-center justify-center text-lg font-bold shrink-0',
            store.selectedTemplateId === tpl.id ? 'bg-blue-500/20 text-blue-400' : 'bg-gray-700 text-gray-400',
          ]"
        >
          {{ tpl.name.charAt(0) }}
        </div>

        <div class="min-w-0 flex-1">
          <div class="text-sm font-medium truncate">{{ tpl.name }}</div>
          <div class="text-xs text-gray-500 truncate">{{ tpl.description }}</div>
        </div>

        <!-- 选中指示 -->
        <div
          v-if="store.selectedTemplateId === tpl.id"
          class="shrink-0 w-5 h-5 rounded-full bg-blue-500 flex items-center justify-center"
        >
          <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
          </svg>
        </div>
      </button>
    </div>
  </div>
</template>
