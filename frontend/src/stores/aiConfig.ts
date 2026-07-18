/**
 * AI 配置 Store
 *
 * 管理用户自有的 AI API 配置（端点、密钥、模型）。
 *
 * 设计原则：
 * - 配置仅存储在前端 localStorage，后端不持久化
 * - 每次 AI 请求由前端传递凭据，后端做无状态代理
 * - 除非用户主动清除，配置在会话间持久保留
 */
import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { AiUserConfig } from '@/types'

/** localStorage 持久化 key */
const STORAGE_KEY = 'gamecard-ai-config'

/**
 * 从 localStorage 读取持久化的 AI 配置
 */
function loadConfig(): AiUserConfig | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as AiUserConfig
  } catch {
    /* ignore corrupt data */
  }
  return null
}

/**
 * 写入 AI 配置到 localStorage
 */
function saveConfig(config: AiUserConfig): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  } catch {
    /* storage full or unavailable, silently fail */
  }
}

/**
 * 清除持久化的 AI 配置
 */
function clearSavedConfig(): void {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore */
  }
}

export const useAiConfigStore = defineStore('aiConfig', () => {
  // ── 状态 ──
  const endpoint = ref('')
  const apiKey = ref('')
  const model = ref('')

  /** 是否已配置完成（三个字段均非空） */
  const configured = computed(() => {
    return endpoint.value.trim() !== ''
      && apiKey.value.trim() !== ''
      && model.value.trim() !== ''
  })

  /** 获取完整的配置对象（用于发送请求） */
  const config = computed<AiUserConfig>(() => ({
    endpoint: endpoint.value,
    apiKey: apiKey.value,
    model: model.value,
    configured: configured.value,
  }))

  // ── 初始化：从 localStorage 恢复 ──
  const saved = loadConfig()
  if (saved) {
    endpoint.value = saved.endpoint
    apiKey.value = saved.apiKey
    model.value = saved.model
  }

  // ── 自动持久化 ──
  watch(
    [endpoint, apiKey, model],
    () => {
      if (endpoint.value || apiKey.value || model.value) {
        saveConfig({
          endpoint: endpoint.value,
          apiKey: apiKey.value,
          model: model.value,
          configured: configured.value,
        })
      }
    },
    { deep: true },
  )

  // ── 操作 ──

  /** 更新 AI 配置 */
  function update(newConfig: { endpoint: string; apiKey: string; model: string }) {
    endpoint.value = newConfig.endpoint
    apiKey.value = newConfig.apiKey
    model.value = newConfig.model
  }

  /** 清除 AI 配置（用户主动删除） */
  function clear() {
    endpoint.value = ''
    apiKey.value = ''
    model.value = ''
    clearSavedConfig()
  }

  return {
    endpoint,
    apiKey,
    model,
    configured,
    config,
    update,
    clear,
  }
})
