<script setup lang="ts">
/**
 * AIImageProcessor — AI 图像风格转换面板
 *
 * 流程：
 *   用户选择处理模式 → 查看/编辑 prompt → 点击"生成风格化图片"
 *   → 将自定义 prompt 发送到后端 → 后端透传到用户配置的 AI 端点
 *   → 返回风格化图片 → 替换当前预览图
 *
 *  AI 配置要求：
 *   - 用户必须先在 AiSettingsPanel 中配置 API
 *   - 未配置时显示引导，不展示 AI 处理选项
 */
import { ref, computed, watch } from 'vue'
import { useCardStore } from '@/stores/card'
import { useAiConfigStore } from '@/stores/aiConfig'
import { processImageWithAi } from '@/api/aiImage'
import type { ImageProcessMode } from '@/types'

const cardStore = useCardStore()
const aiStore = useAiConfigStore()

const emit = defineEmits<{
  /** AI 处理完成，返回新的图片 data URI */
  done: [dataUri: string]
}>()

// ── 处理模式 ──
const processMode = ref<ImageProcessMode>('original')

// ── AI 处理状态 ──
const processing = ref(false)
const error = ref('')

/** 是否有图片可处理 */
const hasImage = computed(() => !!cardStore.uploadedImage)

/** 当前模板的默认 imagePrompt */
const defaultPrompt = computed(() => {
  return cardStore.selectedTemplate?.imageConfig?.imagePrompt ?? ''
})

/** 用户编辑的提示词，初始加载模板的默认值 */
const customPrompt = ref('')

/** 当模板切换或默认 prompt 变化时，重置用户编辑区 */
watch(defaultPrompt, (val) => {
  if (val && !customPrompt.value) {
    customPrompt.value = val
  }
}, { immediate: true })

/** AI 是否可用（已配置 + 已有图片 + 已选模板） */
const aiAvailable = computed(() => {
  return aiStore.configured
    && hasImage.value
    && !!cardStore.selectedTemplateId
})

/** 根据处理模式显示不同的描述文本 */
const modeDescriptions: Record<ImageProcessMode, string> = {
  original: '直接使用原始图片，不做任何处理',
  basic: '可进行裁剪、缩放、形状调整、抠图',
  ai: 'AI 风格化转换，生成符合当前模板风格的角色图片',
}

async function handleAiProcess() {
  if (!aiAvailable.value || processing.value) return

  processing.value = true
  error.value = ''

  try {
    const result = await processImageWithAi(
      cardStore.uploadedImage!,
      cardStore.selectedTemplateId!,
      aiStore.config,
      // 如果用户修改过 prompt，传入自定义值
      customPrompt.value !== defaultPrompt.value ? customPrompt.value : undefined,
    )
    emit('done', result)
    // 切换回 original 表示处理完成
    processMode.value = 'original'
  } catch (e: any) {
    error.value = e?.message || 'AI 处理失败，请检查配置后重试'
  } finally {
    processing.value = false
  }
}

function resetPrompt() {
  customPrompt.value = defaultPrompt.value
}
</script>

<template>
  <div class="ai-processor">
    <p class="ai-processor__title">图片处理方式</p>

    <!-- 模式选择 -->
    <div class="ai-processor__modes">
      <label
        class="ai-processor__mode"
        :class="{ 'is-selected': processMode === 'original' }"
      >
        <input
          v-model="processMode"
          type="radio"
          value="original"
          class="ai-processor__radio"
        />
        <div class="ai-processor__mode-content">
          <span class="ai-processor__mode-name">原始图片</span>
          <span class="ai-processor__mode-desc">{{ modeDescriptions.original }}</span>
        </div>
      </label>

      <label
        class="ai-processor__mode"
        :class="{ 'is-selected': processMode === 'basic' }"
      >
        <input
          v-model="processMode"
          type="radio"
          value="basic"
          class="ai-processor__radio"
        />
        <div class="ai-processor__mode-content">
          <span class="ai-processor__mode-name">基础优化</span>
          <span class="ai-processor__mode-desc">{{ modeDescriptions.basic }}</span>
        </div>
      </label>

      <label
        class="ai-processor__mode"
        :class="{
          'is-selected': processMode === 'ai',
          'is-disabled': !aiAvailable,
        }"
      >
        <input
          v-model="processMode"
          type="radio"
          value="ai"
          class="ai-processor__radio"
          :disabled="!aiAvailable"
        />
        <div class="ai-processor__mode-content">
          <span class="ai-processor__mode-name">AI 风格转换</span>
          <span class="ai-processor__mode-desc">{{ modeDescriptions.ai }}</span>
        </div>
        <!-- 未配置 AI 时显示锁图标 -->
        <svg
          v-if="!aiStore.configured"
          class="ai-processor__lock"
          width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
        >
          <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
          <path d="M7 11V7a5 5 0 0110 0v4" />
        </svg>
      </label>
    </div>

    <!-- AI 未配置提示 -->
    <div
      v-if="processMode === 'ai' && !aiStore.configured"
      class="ai-processor__config-hint"
    >
      <p class="ai-processor__config-text">
        使用 AI 风格转换需要先配置 AI API。
      </p>
    </div>

    <!-- AI 模式：提示词编辑 -->
    <div v-if="processMode === 'ai' && aiStore.configured" class="ai-processor__prompt">
      <div class="ai-processor__prompt-header">
        <label class="ai-processor__prompt-label">提示词（Prompt）</label>
        <button
          v-if="customPrompt !== defaultPrompt"
          class="ai-processor__prompt-reset"
          @click="resetPrompt"
        >
          恢复默认
        </button>
      </div>

      <!-- 默认提示词来源 -->
      <p v-if="defaultPrompt" class="ai-processor__prompt-source">
        模板默认:
        <span class="ai-processor__prompt-source-text">{{ defaultPrompt }}</span>
      </p>

      <textarea
        v-model="customPrompt"
        class="ai-processor__prompt-input"
        rows="4"
        placeholder="输入自定义提示词，描述你想要的图片风格..."
      />

      <!-- 提示词变量提示 -->
      <p class="ai-processor__prompt-hint">
        修改提示词可自定义 AI 生成风格。恢复默认可使用模板预设。
      </p>
    </div>

    <!-- AI 处理按钮 -->
    <div v-if="processMode === 'ai'" class="ai-processor__actions">
      <button
        class="ai-processor__btn"
        :class="{ 'is-processing': processing }"
        :disabled="!aiAvailable || processing"
        @click="handleAiProcess"
      >
        <svg
          v-if="processing"
          class="ai-processor__spinner"
          width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
        >
          <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
        </svg>
        <svg
          v-else
          width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
        >
          <path d="M12 2l2.4 7.2L22 9l-6 5.4L18 22l-6-4.8L6 22l2-7.6L2 9l7.6-.4z" />
        </svg>
        <span>{{ processing ? 'AI 处理中...' : '生成风格化图片' }}</span>
      </button>
    </div>

    <!-- 错误提示 -->
    <p v-if="error" class="ai-processor__error">{{ error }}</p>
  </div>
</template>

<style scoped>
.ai-processor {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-sm);
}

.ai-processor__title {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
  margin: 0;
}

/* ── Mode options ── */
.ai-processor__modes {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ai-processor__mode {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 10px;
  border-radius: var(--gc-radius-sm);
  border: 1px solid transparent;
  background-color: var(--gc-canvas);
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.ai-processor__mode:hover {
  border-color: var(--gc-hairline);
}

.ai-processor__mode.is-selected {
  border-color: var(--gc-primary);
  background-color: var(--gc-surface-soft);
}

.ai-processor__mode.is-disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.ai-processor__radio {
  margin-top: 2px;
  accent-color: var(--gc-primary);
}

.ai-processor__mode-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.ai-processor__mode-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--gc-ink);
}

.ai-processor__mode-desc {
  font-size: 11px;
  color: var(--gc-ink);
  opacity: 0.45;
  line-height: 1.4;
}

.ai-processor__lock {
  color: var(--gc-ink);
  opacity: 0.3;
  flex-shrink: 0;
  margin-top: 2px;
}

/* ── Prompt editing ── */
.ai-processor__prompt {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ai-processor__prompt-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ai-processor__prompt-label {
  font-family: var(--gc-font-mono);
  font-size: 11px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  color: var(--gc-ink);
  opacity: 0.5;
}

.ai-processor__prompt-reset {
  font-size: 10px;
  color: var(--gc-primary);
  opacity: 0.6;
  text-decoration: underline;
  text-underline-offset: 2px;
  transition: opacity 0.15s ease;
}

.ai-processor__prompt-reset:hover {
  opacity: 1;
}

.ai-processor__prompt-source {
  font-size: 11px;
  color: var(--gc-ink);
  opacity: 0.35;
  margin: 0;
  line-height: 1.4;
}

.ai-processor__prompt-source-text {
  opacity: 0.5;
}

.ai-processor__prompt-input {
  width: 100%;
  padding: 8px 10px;
  font-family: var(--gc-font-sans);
  font-size: 12px;
  line-height: 1.5;
  color: var(--gc-ink);
  background-color: var(--gc-surface-soft);
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-sm);
  outline: none;
  resize: vertical;
  min-height: 72px;
  transition: border-color 0.15s ease;
}

.ai-processor__prompt-input:focus {
  border-color: var(--gc-primary);
  background-color: var(--gc-canvas);
}

.ai-processor__prompt-input::placeholder {
  color: var(--gc-ink);
  opacity: 0.2;
}

.ai-processor__prompt-hint {
  font-size: 10px;
  color: var(--gc-ink);
  opacity: 0.35;
  margin: 0;
  line-height: 1.4;
}

/* ── Config hint ── */
.ai-processor__config-hint {
  padding: 8px 10px;
  border-radius: var(--gc-radius-sm);
  background-color: rgba(251, 191, 36, 0.08);
  border: 1px solid rgba(251, 191, 36, 0.2);
}

.ai-processor__config-text {
  font-size: 12px;
  color: var(--gc-ink);
  opacity: 0.7;
  margin: 0;
}

/* ── Actions ── */
.ai-processor__actions {
  display: flex;
}

.ai-processor__btn {
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
  background: linear-gradient(135deg, #8B5CF6, #6366F1);
  color: #fff;
  border: none;
}

.ai-processor__btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.ai-processor__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.ai-processor__btn.is-processing {
  opacity: 0.7;
  cursor: wait;
}

@keyframes ai-processor-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.ai-processor__spinner {
  animation: ai-processor-spin 1s linear infinite;
}

/* ── Error ── */
.ai-processor__error {
  font-size: 11px;
  color: #ef4444;
  margin: 0;
  line-height: 1.4;
}
</style>
