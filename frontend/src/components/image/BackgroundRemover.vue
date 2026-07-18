<script setup lang="ts">
/**
 * BackgroundRemover - 自动去背景按钮组件
 *
 * 提供"自动去除背景"按钮，包含：
 * - 空闲状态（可点击）
 * - 处理中状态（加载动画）
 * - 成功状态（带 ✓ 反馈）
 * - 失败状态（错误提示，可重试）
 */
import { ref, computed } from 'vue'
import { removeBackground } from '@/api/image'

type BgState = 'idle' | 'processing' | 'success' | 'error'

const props = defineProps<{
  /** 要处理的图片 File 对象（优先级高） */
  imageFile: File | null
  /** 图片 data URI 兜底（当 imageFile 为 null 时使用，如 AI 生成的图片） */
  imageDataUri?: string | null
}>()

const emit = defineEmits<{
  /** 抠图完成，返回透明 PNG data URI */
  done: [dataUri: string]
}>()

const state = ref<BgState>('idle')
const errorMsg = ref('')

/** 是否可以执行去背景 */
const canProcess = computed(() => {
  return (props.imageFile !== null || props.imageDataUri != null)
    && state.value !== 'processing'
})

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

async function handleRemoveBg() {
  if (state.value === 'processing') return

  // 确定要处理的文件：优先 imageFile，其次从 dataUri 创建
  let file = props.imageFile
  if (!file && props.imageDataUri) {
    file = dataUriToFile(props.imageDataUri, 'ai-processed.png')
  }
  if (!file) return

  state.value = 'processing'
  errorMsg.value = ''

  try {
    const dataUri = await removeBackground(file)
    state.value = 'success'
    emit('done', dataUri)

    // 3 秒后恢复 idle，允许再次点击
    setTimeout(() => {
      if (state.value === 'success') state.value = 'idle'
    }, 3000)
  } catch (e: any) {
    state.value = 'error'
    errorMsg.value = e?.message || '去背景处理失败，请重试'
  }
}

function retry() {
  state.value = 'idle'
  errorMsg.value = ''
  handleRemoveBg()
}
</script>

<template>
  <div class="bg-remover">
    <!-- 主按钮 -->
    <button
      class="bg-remover__btn"
      :class="{
        'is-processing': state === 'processing',
        'is-success': state === 'success',
        'is-disabled': !canProcess,
      }"
      :disabled="!canProcess"
      @click="handleRemoveBg"
    >
      <!-- 处理中：旋转加载 -->
      <svg v-if="state === 'processing'" class="bg-remover__spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
      </svg>

      <!-- 成功：对勾 -->
      <svg v-else-if="state === 'success'" class="bg-remover__check" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <polyline points="20 6 9 17 4 12" />
      </svg>

      <!-- 空闲 / 失败：魔法棒图标 -->
      <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M15 4V2m0 2v2m0-2h2m-2 0h-2m-4.5 7.5L15 4" />
        <path d="M4 15l5 5" />
        <path d="M8.5 11.5L11 14" />
        <path d="M13 16l2.5 2.5" />
      </svg>

      <span class="bg-remover__label">
        {{ state === 'processing' ? '处理中...' : state === 'success' ? '已完成' : state === 'error' ? '重试' : '自动去除背景' }}
      </span>
    </button>

    <!-- 错误提示 -->
    <p v-if="state === 'error' && errorMsg" class="bg-remover__error">
      {{ errorMsg }}
    </p>
  </div>
</template>

<style scoped>
.bg-remover {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bg-remover__btn {
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

.bg-remover__btn:hover:not(:disabled) {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  border-color: var(--gc-primary);
}

.bg-remover__btn.is-disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.bg-remover__btn.is-processing {
  opacity: 0.7;
  cursor: wait;
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  border-color: var(--gc-hairline);
}

.bg-remover__btn.is-success {
  background-color: #10b981;
  color: #fff;
  border-color: #10b981;
  cursor: default;
}

/* 旋转动画 */
@keyframes bg-remover-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bg-remover__spinner {
  animation: bg-remover-spin 1s linear infinite;
}

.bg-remover__check {
  /* 静态对勾，无需动画 */
}

.bg-remover__label {
  white-space: nowrap;
}

.bg-remover__error {
  font-size: 11px;
  color: #ef4444;
  text-align: center;
  margin: 0;
  line-height: 1.4;
}
</style>
