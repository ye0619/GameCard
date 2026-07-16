<script setup lang="ts">
import { ref } from 'vue'
import { useCardStore } from '@/stores/card'

const store = useCardStore()

// ── 拖拽状态 ──
const isDragging = ref(false)

// ── 隐藏的 file input ──
const fileInput = ref<HTMLInputElement>()

/** 处理文件选择（拖拽或点击共用） */
function handleFile(file: File) {
  if (!file.type.startsWith('image/')) return
  const url = URL.createObjectURL(file)
  store.setImage(url)
}

/** 点击上传 */
function onPick() {
  fileInput.value?.click()
}

/** input change */
function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) handleFile(file)
  input.value = '' // 允许重复选择同一文件
}

/** 拖拽事件 */
function onDragOver(e: DragEvent) {
  e.preventDefault()
  isDragging.value = true
}
function onDragLeave() {
  isDragging.value = false
}
function onDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  const file = e.dataTransfer?.files[0]
  if (file) handleFile(file)
}

/** 移除图片 */
function removeImage() {
  if (store.uploadedImage) {
    URL.revokeObjectURL(store.uploadedImage)
  }
  store.setImage(null)
}
</script>

<template>
  <div class="space-y-2">
    <label class="text-sm font-medium text-gray-300">上传图片</label>

    <!-- 已有图片显示预览 -->
    <div v-if="store.uploadedImage" class="relative group">
      <img
        :src="store.uploadedImage"
        alt="预览"
        class="w-full h-40 object-cover rounded-lg border border-gray-700"
      />
      <button
        @click="removeImage"
        class="absolute top-2 right-2 bg-black/60 text-white rounded-full w-7 h-7 flex items-center justify-center opacity-0 group-hover:opacity-100 transition text-sm hover:bg-red-600"
        title="移除图片"
      >
        ✕
      </button>
    </div>

    <!-- 上传区域 -->
    <div
      v-else
      @click="onPick"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
      :class="[
        'border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-all',
        isDragging
          ? 'border-blue-400 bg-blue-400/10'
          : 'border-gray-600 hover:border-gray-400 bg-gray-800/50',
      ]"
    >
      <div class="flex flex-col items-center gap-2">
        <svg class="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>
        <p class="text-sm text-gray-400">
          <span class="text-blue-400 font-medium">点击上传</span> 或拖拽图片到此处
        </p>
        <p class="text-xs text-gray-500">支持 PNG、JPG、WebP</p>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/png,image/jpeg,image/webp"
        class="hidden"
        @change="onFileChange"
      />
    </div>

    <!-- image 字段联动：同步到 cardData -->
    <p v-if="store.uploadedImage" class="text-xs text-green-400">✅ 图片已上传</p>
  </div>
</template>
