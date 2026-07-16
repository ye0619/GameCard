<script setup lang="ts">
/**
 * ImageUploader — drag-and-drop image upload.
 *
 * Handles image picking and preview. Stores result in cardStore.uploadedImage.
 * UI uses design tokens per DESIGN-figma.md.
 */
import { ref } from 'vue'
import { useCardStore } from '@/stores/card'

const store = useCardStore()

const isDragging = ref(false)
const fileInput = ref<HTMLInputElement>()

function handleFile(file: File) {
  if (!file.type.startsWith('image/')) return
  const url = URL.createObjectURL(file)
  store.setImage(url)
}

function onPick() {
  fileInput.value?.click()
}

function onFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) handleFile(file)
  input.value = ''
}

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

function removeImage() {
  if (store.uploadedImage) {
    URL.revokeObjectURL(store.uploadedImage)
  }
  store.setImage(null)
}
</script>

<template>
  <div class="img-upload">
    <p class="img-upload__label">上传图片</p>

    <!-- Preview -->
    <div v-if="store.uploadedImage" class="img-upload__preview">
      <img
        :src="store.uploadedImage"
        alt="预览"
        class="img-upload__preview-img"
      />
      <button
        class="img-upload__remove"
        title="移除图片"
        @click="removeImage"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
    </div>

    <!-- Drop zone -->
    <div
      v-else
      class="img-upload__zone"
      :class="{ 'is-dragging': isDragging }"
      @click="onPick"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <svg class="img-upload__zone-icon" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
        <path d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9" />
        <line x1="15" y1="13" x2="12" y2="10" />
        <line x1="12" y1="10" x2="9" y2="13" />
        <line x1="12" y1="10" x2="12" y2="18" />
      </svg>
      <p class="img-upload__zone-text">
        <span class="img-upload__zone-link">点击上传</span> 或拖拽图片到此处
      </p>
      <p class="img-upload__zone-hint">支持 PNG、JPG、WebP</p>
      <input
        ref="fileInput"
        type="file"
        accept="image/png,image/jpeg,image/webp"
        class="img-upload__input"
        @change="onFileChange"
      />
    </div>

    <!-- Sync hint -->
    <p v-if="store.uploadedImage" class="img-upload__synced">
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12" />
      </svg>
      图片已上传
    </p>
  </div>
</template>

<style scoped>
.img-upload {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-sm);
}

.img-upload__label {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  font-weight: var(--gc-caption-weight);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
}

/* Preview */
.img-upload__preview {
  position: relative;
  border-radius: var(--gc-radius-md);
  overflow: hidden;
  border: 1px solid var(--gc-hairline);
}

.img-upload__preview-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  display: block;
}

.img-upload__remove {
  position: absolute;
  top: var(--gc-space-xs);
  right: var(--gc-space-xs);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--gc-radius-full);
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  opacity: 0;
  transition: opacity 0.15s ease, background-color 0.15s ease;
}

.img-upload__preview:hover .img-upload__remove {
  opacity: 1;
}

.img-upload__remove:hover {
  background-color: var(--gc-accent-magenta);
}

/* Drop zone */
.img-upload__zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: var(--gc-space-lg) var(--gc-space-md);
  border: 1.5px dashed var(--gc-hairline);
  border-radius: var(--gc-radius-md);
  background-color: var(--gc-canvas);
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.img-upload__zone:hover {
  border-color: var(--gc-ink);
  opacity: 0.8;
}

.img-upload__zone.is-dragging {
  border-color: var(--gc-primary);
  background-color: var(--gc-surface-soft);
}

.img-upload__zone-icon {
  color: var(--gc-ink);
  opacity: 0.25;
}

.img-upload__zone-text {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  color: var(--gc-ink);
  opacity: 0.6;
}

.img-upload__zone-link {
  font-weight: 540;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.img-upload__zone-hint {
  font-family: var(--gc-font-mono);
  font-size: 10px;
  letter-spacing: 0.3px;
  color: var(--gc-ink);
  opacity: 0.3;
}

/* Hidden file input */
.img-upload__input {
  display: none;
}

/* Sync status */
.img-upload__synced {
  display: flex;
  align-items: center;
  gap: var(--gc-space-xxs);
  font-family: var(--gc-font-sans);
  font-size: 12px;
  font-weight: 480;
  color: var(--gc-semantic-success);
}
</style>
