import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Template, CardData, ImageConfig } from '@/types'
import { fetchTemplates, fetchTemplate } from '@/api/template'

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

  /** 选择模板，重置卡片数据 */
  function selectTemplate(id: string) {
    selectedTemplateId.value = id
    const tpl = templates.value.find((t) => t.id === id)
    if (tpl) {
      const data: CardData = {}
      for (const field of tpl.fields) {
        data[field.key] = field.defaultValue ?? ''
      }
      // 如果 themeField 有默认值但对应字段没初始化，补上
      if (tpl.themeField && !data[tpl.themeField]) {
        data[tpl.themeField] = ''
      }
      cardData.value = data
    }
  }

  /** 更新某个字段的值 */
  function updateField(key: string, value: string) {
    cardData.value[key] = value
  }

  /** 设置上传的图片，同时重置图片配置 */
  function setImage(url: string | null) {
    uploadedImage.value = url
    resetImageConfig()
  }

  return {
    templates,
    selectedTemplateId,
    selectedTemplate,
    cardData,
    uploadedImage,
    imageConfig,
    loading,
    loadTemplates,
    selectTemplate,
    updateField,
    setImage,
    resetImageConfig,
  }
})
