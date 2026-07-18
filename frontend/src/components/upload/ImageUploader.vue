<script setup lang="ts">
/**
 * ImageUploader — drag-and-drop image upload with edit and AI processing.
 *
 * Handles image picking, preview, and provides entry points for:
 * - Basic editing (crop, scale, shape, bg remove)
 * - AI style transfer (if user has configured AI API)
 * - AI settings configuration
 */
import { ref } from 'vue'
import { useCardStore } from '@/stores/card'
import { useAiConfigStore } from '@/stores/aiConfig'
import ImageEditor from '@/components/image/ImageEditor.vue'
import AIImageProcessor from '@/components/image/AIImageProcessor.vue'
import AiSettingsPanel from '@/components/settings/AiSettingsPanel.vue'

const store = useCardStore()
const aiStore = useAiConfigStore()

const isDragging = ref(false)
const fileInput = ref<HTMLInputElement>()
const showEditor = ref(false)
const showAiSettings = ref(false)

function handleFile(file: File) {
  if (!file.type.startsWith('image/')) return
  const url = URL.createObjectURL(file)
  store.setImage(url, file)
  showEditor.value = true
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
  if (store.uploadedImage && !store.uploadedImage.startsWith('data:')) {
    URL.revokeObjectURL(store.uploadedImage)
  }
  store.setImage(null)
  showEditor.value = false
  showAiSettings.value = false
}

function closeEditor() {
  showEditor.value = false
}

/** AI 处理完成后的回调：用生成的新图片替换当前图片 */
function onAiDone(dataUri: string) {
  // 将 AI 生成的 data URI 转为 File 对象保留，以便后续抠图等操作
  const file = dataUriToFile(dataUri, 'ai-generated.png')
  store.setImage(dataUri, file)
  showEditor.value = false
}

/** 将 data URI 转为 File 对象 */
function dataUriToFile(dataUri: string, filename = 'image.png'): File {
  const [meta, data] = dataUri.split(',')
  const mime = meta.match(/:(.*?);/)?.[1] || 'image/png'
  const binary = atob(data)
  const array = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    array[i] = binary.charCodeAt(i)
  }
  return new File([array], filename, { type: mime })
}
</script>

<template>
  <div class="img-upload">
    <p class="img-upload__label">上传图片</p>

    <!-- Preview with actions -->
    <div v-if="store.uploadedImage" class="img-upload__preview">
      <img
        :src="store.uploadedImage"
        alt="预览"
        class="img-upload__preview-img"
      />
      <div class="img-upload__actions">
        <button
          class="img-upload__action-btn"
          title="编辑图片"
          @click="showEditor = !showEditor"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
            <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
          编辑
        </button>
        <button
          class="img-upload__action-btn"
          title="AI 处理"
          @click="showAiSettings = false; showEditor = false"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2l2.4 7.2L22 9l-6 5.4L18 22l-6-4.8L6 22l2-7.6L2 9l7.6-.4z" />
          </svg>
          风格
        </button>
        <button
          class="img-upload__action-btn img-upload__action-btn--remove"
          title="移除图片"
          @click="removeImage"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
          </svg>
          移除
        </button>
      </div>
    </div>

    <!-- AI 图像处理面板 -->
    <div v-if="store.uploadedImage" class="img-upload__section">
      <AIImageProcessor @done="onAiDone" />
    </div>

    <!-- AI 配置面板（可折叠） -->
    <div v-if="store.uploadedImage && !aiStore.configured" class="img-upload__section">
      <button
        class="img-upload__toggle-settings"
        @click="showAiSettings = !showAiSettings"
      >
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
        </svg>
        <span>{{ showAiSettings ? '收起 AI 配置' : '配置 AI API（使用风格转换）' }}</span>
      </button>
      <div v-if="showAiSettings" class="img-upload__settings-panel">
        <AiSettingsPanel />
      </div>
    </div>

    <!-- AI 配置状态提示（已配置时） -->
    <div
      v-if="store.uploadedImage && aiStore.configured"
      class="img-upload__ai-status"
    >
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
        <polyline points="20 6 9 17 4 12" />
      </svg>
      <span>AI API 已配置（{{ aiStore.model }}）</span>
      <button class="img-upload__ai-settings-link" @click="showAiSettings = !showAiSettings">
        修改
      </button>
      <!-- 点击修改后展开配置 -->
    </div>
    <div v-if="store.uploadedImage && aiStore.configured && showAiSettings" class="img-upload__settings-panel">
      <AiSettingsPanel />
    </div>

    <!-- Image Editor panel (inline) -->
    <div v-if="store.uploadedImage && showEditor" class="img-upload__editor-panel">
      <ImageEditor @close="closeEditor" />
    </div>

    <!-- Drop zone -->
    <div
      v-else-if="!store.uploadedImage"
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

    <!-- Sync status -->
    <p v-if="store.uploadedImage && store.imageConfig.applied" class="img-upload__synced">
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
        <polyline points="20 6 9 17 4 12" />
      </svg>
      编辑已应用
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

/* Sections between preview and editor */
.img-upload__section {
  /* natural gap from flex container */
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

/* Action buttons overlay */
.img-upload__actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 1px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.img-upload__action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 8px;
  font-size: 11px;
  font-weight: 500;
  color: #fff;
  transition: background-color 0.15s ease;
}

.img-upload__action-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.img-upload__action-btn--remove:hover {
  background-color: rgba(255, 61, 139, 0.3);
}

/* AI 配置折叠 */
.img-upload__toggle-settings {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 6px 10px;
  font-size: 11px;
  font-weight: 500;
  color: var(--gc-ink);
  opacity: 0.5;
  border-radius: var(--gc-radius-sm);
  transition: opacity 0.15s ease, background-color 0.15s ease;
}

.img-upload__toggle-settings:hover {
  opacity: 0.8;
  background-color: var(--gc-surface-soft);
}

.img-upload__settings-panel {
  padding: var(--gc-space-sm);
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-md);
  background-color: var(--gc-canvas);
}

/* AI 状态条 */
.img-upload__ai-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--gc-semantic-success);
  padding: 4px 0;
}

.img-upload__ai-settings-link {
  font-size: 11px;
  color: var(--gc-ink);
  opacity: 0.4;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.img-upload__ai-settings-link:hover {
  opacity: 0.7;
}

/* Image Editor panel */
.img-upload__editor-panel {
  padding: var(--gc-space-md);
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-md);
  background-color: var(--gc-canvas);
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

.img-upload__input {
  display: none;
}

/* Sync status */
.img-upload__synced {
  display: flex;
  align-items: center;
  gap: var(--gc-space-xxs);
  font-size: 12px;
  font-weight: 480;
  color: var(--gc-semantic-success);
}
</style>
