<script setup lang="ts">
/**
 * WorksPage — Saved cards gallery.
 *
 * Gallery of saved works with export and delete.
 * Export renders the full card in a modal overlay, captures it with html-to-image.
 */
import { ref, computed, onMounted, nextTick } from 'vue'
import { useCardStore } from '@/stores/card'
import { useWorksStore } from '@/stores/works'
import { resolveTheme } from '@/utils/themeResolver'
import type { ResolvedTheme } from '@/utils/themeResolver'
import type { SavedWork } from '@/types'
import CardRenderer from '@/components/card/CardRenderer.vue'
import { useCardExport } from '@/export'
import EmptyState from '@/components/common/EmptyState.vue'

const cardStore = useCardStore()
const worksStore = useWorksStore()
const { exportCard } = useCardExport()

onMounted(() => {
  if (cardStore.templates.length === 0) {
    cardStore.loadTemplates()
  }
})

// ── 作品列表 ──
const workList = computed(() => worksStore.getWorks())

// ── 删除 ──
function confirmDelete(work: SavedWork) {
  if (window.confirm(`确定要删除「${work.name}」吗？此操作不可恢复。`)) {
    worksStore.remove(work.id)
  }
}

// ── 导出模态窗口 ──
const exportWork = ref<SavedWork | null>(null)
const exportReady = ref(false)
const isExporting = ref(false)
const exportSuccess = ref(false)

function openExport(work: SavedWork) {
  const tpl = cardStore.templates.find(t => t.id === work.templateId)
  if (!tpl) {
    alert('找不到模板「' + work.templateName + '」，无法导出')
    return
  }
  exportWork.value = work
  exportReady.value = false
  isExporting.value = false
  exportSuccess.value = false

  // 等待 CardRenderer 渲染完成
  nextTick(() => nextTick(() => {
    // 等待子组件和字体加载
    setTimeout(() => {
      exportReady.value = true
    }, 500)
  }))
}

function closeExport() {
  exportWork.value = null
  exportReady.value = false
  isExporting.value = false
  exportSuccess.value = false
}

async function handleExport() {
  if (!exportWork.value || !exportReady.value || isExporting.value) return

  isExporting.value = true

  // 模态窗口中 CardRenderer 的 .game-card
  const el = document.querySelector<HTMLElement>('.works-export-modal .game-card')
  if (!el) {
    alert('无法找到卡片元素，请重试')
    isExporting.value = false
    return
  }

  await exportCard(el, exportWork.value.name, { format: 'png', scale: 2 })

  exportSuccess.value = true
  setTimeout(() => {
    closeExport()
  }, 1500)
}

// ── 模板 / 主题工具函数 ──
function getTemplateForWork(work: SavedWork) {
  return cardStore.templates.find(t => t.id === work.templateId) ?? null
}

function getThemeForWork(work: SavedWork): ResolvedTheme {
  const tpl = getTemplateForWork(work)
  return tpl ? resolveTheme(tpl, work.cardData) : emptyTheme
}

/** 空主题兜底（模板不存在时的 fallback） */
const emptyTheme: ResolvedTheme = {
  mapping: null,
  color: '#6366F1',
  secondaryColor: '#4F46E5',
  icon: '',
  background: '',
  cssVars: {
    '--card-primary': '#6366F1',
    '--card-secondary': '#4F46E5',
    '--card-gradient-start': '#818CF8',
    '--card-gradient-end': '#4F46E5',
    '--card-glow': 'rgba(99, 102, 241, 0.3)',
    '--card-background': '#1F2937',
  },
}

function getImageStyle(config: SavedWork['imageConfig']) {
  const s: Record<string, string> = {}
  if (!config?.applied) return s
  if (config.crop) {
    const t = config.crop.y
    const r = 1 - config.crop.x - config.crop.width
    const b = 1 - config.crop.y - config.crop.height
    const l = config.crop.x
    s['clipPath'] = `inset(${t * 100}% ${r * 100}% ${b * 100}% ${l * 100}%)`
    return s
  }
  if (config.shape === 'circle') s['clipPath'] = 'circle(50%)'
  else if (config.shape === 'rounded') s['borderRadius'] = '12px'
  else s['borderRadius'] = '0'
  return s
}

function getImageTransform(config: SavedWork['imageConfig']): Record<string, string> | undefined {
  if (!config?.applied) return undefined
  if (config.scale === 1 && config.position.x === 0 && config.position.y === 0) return undefined
  return { transform: `translate(${config.position.x}px, ${config.position.y}px) scale(${config.scale})` }
}

function formatDate(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<template>
  <div class="works-page">
    <div class="gc-container">
      <div class="works-page__header">
        <p class="gc-eyebrow works-page__eyebrow">Gallery</p>
        <h1 class="gc-headline">我的作品</h1>
        <p class="gc-body-sm works-page__sub" v-if="workList.length">
          共 {{ workList.length }} 个作品
        </p>
      </div>

      <!-- Empty state -->
      <div v-if="workList.length === 0" class="works-page__empty">
        <EmptyState title="还没有作品" description="在编辑器中创作并保存你的第一张游戏卡片">
          <template #icon>
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="7" height="7" rx="1" />
            </svg>
          </template>
          <template #action>
            <router-link to="/editor" class="works-page__cta">开始创作</router-link>
          </template>
        </EmptyState>
      </div>

      <!-- Gallery grid -->
      <div v-else class="works-page__grid">
        <div v-for="work in workList" :key="work.id" class="works-card">
          <!-- Thumbnail -->
          <div class="works-card__thumb">
            <img v-if="work.imageData" :src="work.imageData" :alt="work.name" class="works-card__img" />
            <div v-else class="works-card__no-img">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3">
                <rect x="3" y="3" width="18" height="18" rx="2" />
                <circle cx="8.5" cy="8.5" r="1.5" />
                <path d="M21 15l-5-5L5 21" />
              </svg>
            </div>
          </div>
          <!-- Info -->
          <div class="works-card__body">
            <p class="works-card__name">{{ work.name }}</p>
            <p class="works-card__meta">
              <span class="works-card__template">{{ work.templateName }}</span>
              <span class="works-card__date">{{ formatDate(work.updatedAt) }}</span>
            </p>
          </div>
          <!-- Actions -->
          <div class="works-card__actions">
            <button class="works-card__btn works-card__btn--export" @click="openExport(work)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              <span>导出</span>
            </button>
            <button class="works-card__btn works-card__btn--delete" @click="confirmDelete(work)" title="删除">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ════════════════════════════════════════════ -->
    <!-- Export modal — renders the card full-size    -->
    <!-- ════════════════════════════════════════════ -->
    <Teleport to="body">
      <Transition name="modal">
        <div v-if="exportWork" class="works-export-modal" @click.self="closeExport">
          <div class="works-export-modal__inner">
            <!-- Card preview -->
            <div class="works-export-modal__card">
              <CardRenderer
                v-if="exportWork"
                :template="getTemplateForWork(exportWork)"
                :card-data="exportWork.cardData"
                :theme="getThemeForWork(exportWork)"
                :image-url="exportWork.imageData"
                :image-style="getImageStyle(exportWork.imageConfig)"
                :image-transform="getImageTransform(exportWork.imageConfig)"
              />
            </div>

            <!-- Controls -->
            <div class="works-export-modal__controls">
              <button class="works-export-modal__btn works-export-modal__btn--close" @click="closeExport">
                关闭
              </button>
              <button
                class="works-export-modal__btn works-export-modal__btn--export"
                :disabled="!exportReady || isExporting"
                @click="handleExport"
              >
                <svg v-if="isExporting" class="export-spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
                </svg>
                <svg v-else-if="exportSuccess" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                <span>{{ isExporting ? '导出中...' : exportSuccess ? '导出成功' : '导出图片 (2x)' }}</span>
              </button>
            </div>

            <p v-if="exportWork" class="works-export-modal__name">{{ exportWork.name }}</p>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.works-page {
  padding: var(--gc-space-section) 0;
  min-height: calc(100vh - var(--gc-nav-height));
}

.works-page__header { margin-bottom: var(--gc-space-xxl); }
.works-page__eyebrow { opacity: 0.4; margin-bottom: var(--gc-space-xs); }
.works-page__sub { opacity: 0.5; margin-top: var(--gc-space-xs); }

.works-page__empty {
  display: flex;
  justify-content: center;
  padding: var(--gc-space-xxl) 0;
}

.works-page__cta {
  display: inline-block;
  padding: 10px 24px;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: opacity 0.15s ease;
}
.works-page__cta:hover { opacity: 0.85; }

/* ──── Grid ──── */
.works-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--gc-space-md);
}

/* ──── Work card ──── */
.works-card {
  display: flex;
  flex-direction: column;
  border-radius: var(--gc-radius-md);
  border: 1px solid var(--gc-hairline);
  background-color: var(--gc-canvas);
  overflow: hidden;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.works-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04), 0 8px 24px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}

.works-card__thumb {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: var(--gc-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.works-card__img { width: 100%; height: 100%; object-fit: cover; }
.works-card__no-img { display: flex; align-items: center; justify-content: center; color: var(--gc-ink); }

.works-card__body {
  padding: var(--gc-space-sm) var(--gc-space-md);
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.works-card__name {
  font-size: 15px;
  font-weight: 540;
  color: var(--gc-ink);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.works-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  font-size: 11px;
  color: var(--gc-ink);
  opacity: 0.45;
  margin: 0;
}
.works-card__template {
  padding: 1px 8px;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-surface-soft);
  font-weight: 480;
}
.works-card__date { opacity: 0.6; }

.works-card__actions {
  display: flex;
  gap: 1px;
  border-top: 1px solid var(--gc-hairline-soft);
  background-color: var(--gc-surface-soft);
}
.works-card__btn {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--gc-ink);
  transition: background-color 0.15s ease;
  cursor: pointer;
  min-height: 36px;
}
.works-card__btn--export:hover { background-color: var(--gc-primary); color: var(--gc-on-primary); }
.works-card__btn--delete {
  flex: 0 0 auto;
  width: 44px;
  opacity: 0.35;
}
.works-card__btn--delete:hover { opacity: 1; color: #ef4444; background-color: rgba(239,68,68,0.08); }

/* ════════════════════════════════════════════ */
/* Export modal                                 */
/* ════════════════════════════════════════════ */
.works-export-modal {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background-color: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow: auto;
}

.works-export-modal__inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.works-export-modal__card {
  width: 1280px;
  height: 720px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.works-export-modal__controls {
  display: flex;
  gap: 12px;
}

.works-export-modal__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  border-radius: var(--gc-radius-pill);
  font-family: var(--gc-font-sans);
  font-size: 14px;
  font-weight: 500;
  min-height: 42px;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
}

.works-export-modal__btn--close {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}
.works-export-modal__btn--close:hover { background-color: rgba(255, 255, 255, 0.2); }

.works-export-modal__btn--export {
  background: linear-gradient(135deg, #8B5CF6, #6366F1);
  color: #fff;
}
.works-export-modal__btn--export:hover:not(:disabled) { opacity: 0.9; }
.works-export-modal__btn--export:disabled { opacity: 0.4; cursor: not-allowed; }

.works-export-modal__name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

@keyframes export-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.export-spinner { animation: export-spin 1s linear infinite; }

/* ── Modal transition ── */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
</style>
