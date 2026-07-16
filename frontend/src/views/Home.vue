<script setup lang="ts">
import { onMounted } from 'vue'
import { useCardStore } from '@/stores/card'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import TemplateSelector from '@/components/template/TemplateSelector.vue'
import CardEditor from '@/components/editor/CardEditor.vue'
import CardPreview from '@/components/preview/CardPreview.vue'

const store = useCardStore()

onMounted(() => {
  store.loadTemplates()
})
</script>

<template>
  <!-- 顶栏 -->
  <header class="h-14 border-b border-gray-800 bg-gray-900/80 backdrop-blur flex items-center px-6 shrink-0">
    <div class="flex items-center gap-3">
      <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white text-xs font-bold">
        GC
      </div>
      <h1 class="text-lg font-bold text-white tracking-tight">GameCard</h1>
      <span class="text-[10px] text-gray-600 border border-gray-700 rounded px-1.5 py-0.5">MVP</span>
    </div>
  </header>

  <!-- 主区域 -->
  <div class="flex flex-1 overflow-hidden">
    <!-- 左侧面板（30%） -->
    <aside class="w-[30%] min-w-[300px] border-r border-gray-800 bg-gray-900/50 overflow-y-auto p-5 space-y-6">
      <ImageUploader />
      <TemplateSelector />
      <CardEditor />
    </aside>

    <!-- 右侧预览区（70%） -->
    <main class="flex-1 bg-gray-950 overflow-y-auto p-6">
      <CardPreview />
    </main>
  </div>
</template>
