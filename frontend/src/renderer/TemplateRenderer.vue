<script setup lang="ts">
/**
 * TemplateRenderer — 模板动态加载器
 *
 * 核心职责：
 * 根据 templateId 动态加载 templates/{id}/layout.vue，
 * 传入标准 props，让模板自主决定渲染结构。
 *
 * 默认从 store 读取模板和数据。
 * 外部可通过 props 覆盖，用于作品画廊等场景。
 *
 * 使用 Vite 的 import.meta.glob 实现完全动态加载：
 * - Vite 在构建时扫描 glob 模式，确定所有可能的模板
 * - 运行时根据模板 ID 从预构建的模块映射中选取
 */
import { computed, ref, shallowRef, watch, type Component } from 'vue'
import { useCardStore } from '@/stores/card'
import { resolveTheme } from '@/utils/themeResolver'
import type { Template, CardData, ImageConfig } from '@/types'
import EmptyState from '@/components/common/EmptyState.vue'
import Loading from '@/components/common/Loading.vue'
import layoutModules from './layoutModules'

const store = useCardStore()

/**
 * 外部可传入这些 props 覆盖 store 中的数据。
 * 留空则自动使用 store 的值。
 */
const props = defineProps<{
  template?: Template | null
  cardData?: CardData | null
  imageUrl?: string | null
  imageConfig?: ImageConfig | null
}>()

// 选择数据源：props 优先，回退到 store
const activeTemplate = computed(() => props.template ?? store.selectedTemplate)
const activeCardData = computed(() => props.cardData ?? store.cardData)
const activeImageUrl = computed(() => props.imageUrl ?? store.uploadedImage)
const activeImageConfig = computed(() => props.imageConfig ?? store.imageConfig)

const theme = computed(() => resolveTheme(activeTemplate.value, activeCardData.value))
const hasData = computed(() => {
  const data = activeCardData.value
  return Object.keys(data).length > 0 &&
    Object.values(data).some(v => v && v.trim() !== '')
})

// ==================== 动态模板加载 ====================

/**
 * layoutModules 由 Vite 构建时扫描生成，
 * 包含 { '/src/templates/{id}/layout.vue': () => import(...) } 映射
 */

/** 当前激活的模板布局组件 */
const currentLayout = shallowRef<Component | null>(null)
const loadingError = ref<string | null>(null)
const isLoading = ref(false)

watch(
  () => activeTemplate.value?.id,
  async (id) => {
    currentLayout.value = null
    loadingError.value = null

    if (!id) {
      return
    }

    isLoading.value = true

    try {
      // 在 glob 映射中查找
      const modulePath = `/src/templates/${id}/layout.vue`
      const loader = layoutModules[modulePath]

      if (!loader) {
        // 模板无对应 layout.vue，回退到无状态
        loadingError.value = `模板 "${id}" 没有布局文件`
        return
      }

      const mod = await loader()
      currentLayout.value = mod.default ?? null
    } catch (err) {
      loadingError.value = `加载模板布局失败: ${err instanceof Error ? err.message : String(err)}`
    } finally {
      isLoading.value = false
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="game-card-wrapper">
    <!-- 加载中 -->
    <div v-if="isLoading" class="renderer-loading">
      <Loading text="加载模板..." />
    </div>

    <!-- 加载错误 -->
    <EmptyState
      v-else-if="loadingError"
      title="模板加载失败"
      :description="loadingError"
    />

    <!-- 无数据 -->
    <EmptyState
      v-else-if="!hasData"
      title="等待卡片编辑"
      description="在左侧选择模板并上传图片，在右侧编辑属性"
    />

    <!-- 渲染模板 layout -->
    <component
      :is="currentLayout"
      v-else-if="currentLayout"
      :card-data="activeCardData"
      :theme="theme"
      :image-url="activeImageUrl"
      :image-config="activeImageConfig"
      :template="activeTemplate"
    />
  </div>
</template>

<style scoped>
.game-card-wrapper {
  position: relative;
}

.renderer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
</style>
