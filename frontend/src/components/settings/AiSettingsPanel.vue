<script setup lang="ts">
/**
 * AiSettingsPanel — 用户 AI API 配置面板
 *
 * 用户可以在此配置自有的 AI 服务（端点 / API Key / 模型）。
 * 配置持久化在 localStorage，关闭后自动保存。
 * 除非用户主动点击"清除配置"，否则跨会话保留。
 */
import { ref } from 'vue'
import { useAiConfigStore } from '@/stores/aiConfig'

const store = useAiConfigStore()

/** 本地编辑副本（避免输入过程中影响 store 的 configured 状态） */
const localEndpoint = ref(store.endpoint)
const localApiKey = ref(store.apiKey)
const localModel = ref(store.model)

const showKey = ref(false)
const saveSuccess = ref(false)

function save() {
  store.update({
    endpoint: localEndpoint.value.trim(),
    apiKey: localApiKey.value.trim(),
    model: localModel.value.trim(),
  })
  saveSuccess.value = true
  setTimeout(() => { saveSuccess.value = false }, 2000)
}

function clearConfig() {
  store.clear()
  localEndpoint.value = ''
  localApiKey.value = ''
  localModel.value = ''
}

/** 测试配置是否有效：通过后端代理验证连接 */
const testing = ref(false)
const testResult = ref<'idle' | 'ok' | 'fail'>('idle')
const testError = ref('')

async function testConnection() {
  if (!localEndpoint.value || !localApiKey.value) return
  testing.value = true
  testResult.value = 'idle'
  testError.value = ''
  try {
    // 先保存再测试
    store.update({
      endpoint: localEndpoint.value.trim(),
      apiKey: localApiKey.value.trim(),
      model: localModel.value.trim(),
    })
    // 通过后端代理验证，避免浏览器 CORS 限制
    const res = await fetch('/api/image/verify-ai', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        endpoint: localEndpoint.value.trim(),
        apiKey: localApiKey.value.trim(),
        model: localModel.value.trim(),
      }),
    })
    const data = await res.json()
    if (data.code === 200 && data.data?.ok) {
      testResult.value = 'ok'
    } else {
      testResult.value = 'fail'
      testError.value = data.data?.error || data.message || '连接失败，请检查端点地址和 Key'
    }
  } catch (e: any) {
    testResult.value = 'fail'
    testError.value = e?.message || '网络错误，无法连接到后端服务'
  } finally {
    testing.value = false
  }
}
</script>

<template>
  <div class="ai-settings">
    <div class="ai-settings__header">
      <p class="ai-settings__title">
        AI 配置
        <span
          v-if="store.configured"
          class="ai-settings__status ai-settings__status--ok"
          title="已配置"
        >已连接</span>
        <span
          v-else
          class="ai-settings__status ai-settings__status--none"
          title="未配置"
        >未配置</span>
      </p>
      <p class="ai-settings__desc">
        配置你的 AI API 以使用风格转换功能。Key 仅存储在你的浏览器中。
      </p>
    </div>

    <!-- 配置表单 -->
    <div class="ai-settings__form">
      <label class="ai-settings__field">
        <span class="ai-settings__label">API 端点地址</span>
        <input
          v-model="localEndpoint"
          type="url"
          placeholder="https://api.agnes.com/v1/images/generations"
          class="ai-settings__input"
        />
      </label>

      <label class="ai-settings__field">
        <span class="ai-settings__label">API Key</span>
        <div class="ai-settings__key-row">
          <input
            v-model="localApiKey"
            :type="showKey ? 'text' : 'password'"
            placeholder="sk-..."
            class="ai-settings__input"
          />
          <button
            class="ai-settings__toggle-key"
            :title="showKey ? '隐藏' : '显示'"
            @click="showKey = !showKey"
          >
            <svg v-if="showKey" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24" />
              <line x1="1" y1="1" x2="23" y2="23" />
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          </button>
        </div>
      </label>

      <label class="ai-settings__field">
        <span class="ai-settings__label">模型</span>
        <input
          v-model="localModel"
          type="text"
          placeholder="dall-e-3"
          class="ai-settings__input"
        />
      </label>
    </div>

    <!-- 操作按钮 -->
    <div class="ai-settings__actions">
      <button
        class="ai-settings__btn ai-settings__btn--primary"
        :disabled="!localEndpoint.trim() || !localApiKey.trim()"
        @click="save"
      >
        <span v-if="saveSuccess">✓ 已保存</span>
        <span v-else>保存配置</span>
      </button>

      <button
        class="ai-settings__btn ai-settings__btn--secondary"
        :disabled="!localEndpoint.trim() || !localApiKey.trim() || testing"
        @click="testConnection"
      >
        <span v-if="testing">测试中...</span>
        <span v-else-if="testResult === 'ok'">✓ 连接正常</span>
        <span v-else-if="testResult === 'fail'">✗ 连接失败</span>
        <span v-else>测试连接</span>
      </button>

      <button
        v-if="store.configured"
        class="ai-settings__btn ai-settings__btn--danger"
        @click="clearConfig"
      >
        清除配置
      </button>
    </div>

    <!-- 测试失败详情 -->
    <div v-if="testResult === 'fail' && testError" class="ai-settings__test-error">
      <p class="ai-settings__test-error-title">连接失败详情：</p>
      <p class="ai-settings__test-error-msg">{{ testError }}</p>
    </div>
  </div>
</template>

<style scoped>
.ai-settings {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-md);
}

.ai-settings__header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ai-settings__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
}

.ai-settings__status {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: var(--gc-radius-pill);
  text-transform: none;
  letter-spacing: normal;
}

.ai-settings__status--ok {
  background-color: #10b981;
  color: #fff;
}

.ai-settings__status--none {
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  opacity: 0.5;
}

.ai-settings__desc {
  font-size: 12px;
  color: var(--gc-ink);
  opacity: 0.5;
  line-height: 1.5;
  margin: 0;
}

.ai-settings__form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ai-settings__field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ai-settings__label {
  font-size: 11px;
  font-weight: 500;
  color: var(--gc-ink);
  opacity: 0.6;
}

.ai-settings__input {
  width: 100%;
  padding: 6px 10px;
  font-family: var(--gc-font-sans);
  font-size: 13px;
  color: var(--gc-ink);
  background-color: var(--gc-surface-soft);
  border: 1px solid transparent;
  border-radius: var(--gc-radius-sm);
  outline: none;
  transition: border-color 0.15s ease;
}

.ai-settings__input:focus {
  border-color: var(--gc-hairline);
}

.ai-settings__input::placeholder {
  color: var(--gc-ink);
  opacity: 0.2;
}

.ai-settings__key-row {
  display: flex;
  gap: 4px;
}

.ai-settings__key-row .ai-settings__input {
  flex: 1;
}

.ai-settings__toggle-key {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--gc-radius-sm);
  color: var(--gc-ink);
  opacity: 0.35;
  flex-shrink: 0;
  transition: opacity 0.15s ease;
}

.ai-settings__toggle-key:hover {
  opacity: 0.7;
}

.ai-settings__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ai-settings__btn {
  padding: 6px 14px;
  border-radius: var(--gc-radius-pill);
  font-size: 12px;
  font-weight: 500;
  min-height: 32px;
  transition: opacity 0.15s ease;
}

.ai-settings__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.ai-settings__btn--primary {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

.ai-settings__btn--secondary {
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
}

.ai-settings__btn--danger {
  background-color: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
  opacity: 0.7;
}

.ai-settings__btn--danger:hover {
  opacity: 1;
}

/* ── Test error ── */
.ai-settings__test-error {
  padding: 8px 10px;
  border-radius: var(--gc-radius-sm);
  background-color: rgba(239, 68, 68, 0.06);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.ai-settings__test-error-title {
  font-size: 11px;
  font-weight: 600;
  color: #ef4444;
  margin: 0 0 4px;
}

.ai-settings__test-error-msg {
  font-size: 11px;
  color: #ef4444;
  opacity: 0.8;
  margin: 0;
  line-height: 1.5;
  word-break: break-all;
}
</style>
