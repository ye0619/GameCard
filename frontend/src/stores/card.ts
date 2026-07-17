import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import type { Template, CardData, ImageConfig } from '@/types'
import { fetchTemplates, fetchTemplate } from '@/api/template'

/** localStorage 持久化 key */
const STORAGE_KEY = 'gamecard-editor-state'

/** 持久化的数据结构 */
interface PersistedState {
  selectedTemplateId: string | null
  cardData: CardData
  imageConfig: ImageConfig
  /** base64 编码的图片数据（data URI 或 base64 blob） */
  imageBase64: string | null
}

/** 将 File 对象转换为 base64 data URI */
function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/** 写入持久化存储 */
function saveState(state: PersistedState): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch {
    // localStorage 不可用或已满，静默失败
  }
}

/** 读取持久化存储 */
function loadState(): PersistedState | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch {
    /* ignore */
  }
  return null
}

/** 清除持久化存储 */
function clearState(): void {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore */
  }
}

/** 卡片编辑器的全局状态 */
export const useCardStore = defineStore('card', () => {
  // ── 模板 ──
  const templates = ref<Template[]>([])
  const selectedTemplateId = ref<string | null>(null)
  const selectedTemplate = computed(() =>
    templates.value.find((t) => t.id === selectedTemplateId.value) ?? null,
  )

  // ── 卡片数据 ──
  const cardData = ref<CardData>({})
  const uploadedImage = ref<string | null>(null)

  /** 原始上传的 File 对象（用于抠图等需要上传原始文件的操作） */
  const originalFile = ref<File | null>(null)

  /** base64 编码的图片数据，用于跨会话持久化 */
  const savedImageBase64 = ref<string | null>(null)

  // ── 图片编辑配置 ──
  const imageConfig = ref<ImageConfig>({
    shape: 'rounded',
    scale: 1,
    position: { x: 0, y: 0 },
    crop: null,
    applied: false,
  })

  /** 重置图片配置 */
  function resetImageConfig() {
    imageConfig.value = {
      shape: 'rounded',
      scale: 1,
      position: { x: 0, y: 0 },
      crop: null,
      applied: false,
    }
  }

  // ── 加载状态 ──
  const loading = ref(false)

  /** 从后端加载模板列表 */
  async function loadTemplates() {
    loading.value = true
    try {
      const list = await fetchTemplates()
      templates.value = list
      if (list.length > 0 && !selectedTemplateId.value) {
        selectTemplate(list[0].id)
      }
    } finally {
      loading.value = false
    }
  }

  /** 选择模板，重置卡片数据（若已从持久化恢复则合并数据） */
  function selectTemplate(id: string) {
    selectedTemplateId.value = id
    const tpl = templates.value.find((t) => t.id === id)
    if (tpl) {
      const data: CardData = {}
      for (const field of tpl.fields) {
        data[field.key] = field.defaultValue ?? ''
      }
      if (tpl.themeField && !data[tpl.themeField]) {
        data[tpl.themeField] = ''
      }
      // 合并已恢复的 cardData（保留用户之前填写的内容）
      cardData.value = { ...data, ...cardData.value }
    }
  }

  /** 更新某个字段的值 */
  function updateField(key: string, value: string) {
    cardData.value[key] = value
  }

  /** 设置上传的图片，同时重置图片配置 */
  function setImage(url: string | null, file?: File | null) {
    uploadedImage.value = url
    if (file !== undefined) {
      originalFile.value = file
    }
    resetImageConfig()

    // 异步保存图片数据用于持久化
    if (file) {
      fileToBase64(file).then((base64) => {
        savedImageBase64.value = base64
      })
    } else if (url && url.startsWith('data:')) {
      // 去背景接口返回的 data URI
      savedImageBase64.value = url
    } else {
      // blob URL 无法跨会话持久化，清空
      savedImageBase64.value = null
    }
  }

  // ── 持久化 ──

  /** 持久化任意变更到 localStorage */
  function persistAll() {
    saveState({
      selectedTemplateId: selectedTemplateId.value,
      cardData: cardData.value,
      imageConfig: imageConfig.value,
      imageBase64: savedImageBase64.value,
    })
  }

  /** 从 localStorage 恢复状态 */
  function restoreState(): boolean {
    const saved = loadState()
    if (!saved) return false

    selectedTemplateId.value = saved.selectedTemplateId

    // 恢复卡片数据（selectTemplate 会做合并）
    cardData.value = saved.cardData

    // 恢复图片编辑配置
    if (saved.imageConfig) {
      imageConfig.value = saved.imageConfig
    }

    // 恢复图片
    if (saved.imageBase64) {
      savedImageBase64.value = saved.imageBase64
      uploadedImage.value = saved.imageBase64
    }

    return true
  }

  // ── 初始恢复 ──
  restoreState()

  // ── 自动持久化 ──
  watch(
    [cardData, imageConfig, selectedTemplateId, savedImageBase64],
    () => {
      persistAll()
    },
    { deep: true },
  )

  return {
    templates,
    selectedTemplateId,
    selectedTemplate,
    cardData,
    uploadedImage,
    originalFile,
    imageConfig,
    loading,
    loadTemplates,
    selectTemplate,
    updateField,
    setImage,
    resetImageConfig,
    persistAll,
    clearState,
  }
})
