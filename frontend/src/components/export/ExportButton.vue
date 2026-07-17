<script setup lang="ts">
/**
 * ExportButton — 卡片导出按钮
 *
 * 提供"导出图片"一键下载功能。
 * 包含 idle / exporting / success / error 四态。
 *
 * 职责仅限 UI 交互，导出逻辑委托给 useCardExport。
 */
import { computed } from 'vue'
import { useCardStore } from '@/stores/card'
import { useCardExport } from '@/export'

const store = useCardStore()
const { exportStatus, exportCard } = useCardExport()

const props = defineProps<{
  /** 要导出的卡片 DOM 元素 */
  cardElement: HTMLElement | null
}>()

/** 卡片名称（用于文件名） */
const cardName = computed(() => store.cardData['name'] ?? '')

/** 是否可导出 */
const canExport = computed(() => {
  return props.cardElement !== null && exportStatus.value !== 'exporting'
})

const isExporting = computed(() => exportStatus.value === 'exporting')
const isSuccess = computed(() => exportStatus.value === 'success')
const isError = computed(() => exportStatus.value === 'error')

/** 按钮文案 */
const buttonLabel = computed(() => {
  if (isExporting.value) return '导出中...'
  if (isSuccess.value) return '导出成功 ✓'
  return '导出图片'
})

async function handleExport() {
  if (!canExport.value) return
  await exportCard(props.cardElement, cardName.value, {
    format: 'png',
    scale: 2,
  })
}
</script>

<template>
  <div class="export-btn">
    <button
      class="export-btn__button"
      :class="{
        'is-exporting': isExporting,
        'is-success': isSuccess,
        'is-error': isError,
        'is-disabled': !canExport,
      }"
      :disabled="!canExport"
      @click="handleExport"
    >
      <!-- 导出中：旋转动画 -->
      <svg
        v-if="isExporting"
        class="export-btn__spinner"
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
      </svg>

      <!-- 成功：对勾 -->
      <svg
        v-else-if="isSuccess"
        class="export-btn__check"
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2.5"
      >
        <polyline points="20 6 9 17 4 12" />
      </svg>

      <!-- 默认：下载图标 -->
      <svg
        v-else
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="1.5"
      >
        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
        <polyline points="7 10 12 15 17 10" />
        <line x1="12" y1="15" x2="12" y2="3" />
      </svg>

      <span class="export-btn__label">{{ buttonLabel }}</span>
    </button>
  </div>
</template>

<style scoped>
.export-btn__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px 16px;
  border-radius: var(--gc-radius-pill);
  font-family: var(--gc-font-sans);
  font-size: 13px;
  font-weight: 500;
  min-height: 36px;
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid var(--gc-hairline);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
}

.export-btn__button:hover:not(:disabled) {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  border-color: var(--gc-primary);
}

.export-btn__button.is-disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.export-btn__button.is-exporting {
  opacity: 0.7;
  cursor: wait;
  background-color: var(--gc-surface-soft);
  border-color: var(--gc-hairline);
}

.export-btn__button.is-success {
  background-color: #10b981;
  color: #fff;
  border-color: #10b981;
  cursor: default;
}

.export-btn__button.is-error {
  border-color: #ef4444;
  color: #ef4444;
}

/* 旋转动画 */
@keyframes export-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.export-btn__spinner {
  animation: export-spin 1s linear infinite;
}

.export-btn__label {
  white-space: nowrap;
}
</style>
