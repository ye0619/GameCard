<script setup lang="ts">
/**
 * EditorPage — design-workspace-style card editor.
 *
 * Layout (per design-tool convention):
 *   Left 20%   → assets / templates (ImageUploader + TemplateSelector)
 *   Center 60% → card preview canvas (CardPreview)
 *   Right 280px → property panel (CardEditor)
 *
 * Full viewport height, each panel scrolls independently.
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCardStore } from '@/stores/card'
import { useWorksStore } from '@/stores/works'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import TemplateSelector from '@/components/template/TemplateSelector.vue'
import CardEditor from '@/components/editor/CardEditor.vue'
import CardPreview from '@/components/preview/CardPreview.vue'
import ExportButton from '@/components/export/ExportButton.vue'

const store = useCardStore()
const worksStore = useWorksStore()
const router = useRouter()

const cardPreviewRef = ref<InstanceType<typeof CardPreview> | null>(null)

// ── 保存作品 ──
const saveStatus = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')

async function handleSave() {
  if (!store.selectedTemplate || !store.cardData['name']?.trim()) {
    saveStatus.value = 'error'
    setTimeout(() => { saveStatus.value = 'idle' }, 2000)
    return
  }

  saveStatus.value = 'saving'

  try {
    worksStore.save({
      name: store.cardData['name']?.trim() || '未命名卡片',
      templateId: store.selectedTemplate.id,
      templateName: store.selectedTemplate.name,
      cardData: { ...store.cardData },
      imageData: store.uploadedImage,
      imageConfig: { ...store.imageConfig },
    })
    saveStatus.value = 'saved'
    setTimeout(() => { saveStatus.value = 'idle' }, 2000)
  } catch {
    saveStatus.value = 'error'
    setTimeout(() => { saveStatus.value = 'idle' }, 3000)
  }
}

onMounted(() => {
  if (store.templates.length === 0) {
    store.loadTemplates()
  }
})
</script>

<template>
  <div class="editor-workspace">
    <!-- ======== Left Panel: Assets & Templates ======== -->
    <aside class="editor-panel editor-panel--left">
      <div class="editor-panel__inner">
        <ImageUploader />
        <div class="editor-panel__divider" />
        <TemplateSelector />
      </div>
    </aside>

    <!-- ======== Center: Preview Canvas ======== -->
    <main class="editor-canvas">
      <CardPreview ref="cardPreviewRef" />
    </main>

    <!-- ======== Right Panel: Properties ======== -->
    <aside class="editor-panel editor-panel--right">
      <div class="editor-panel__header">
        <p class="editor-panel__title">属性</p>
        <div class="editor-panel__header-actions">
          <button
            class="editor-panel__save-btn"
            :class="{
              'is-saving': saveStatus === 'saving',
              'is-saved': saveStatus === 'saved',
              'is-error': saveStatus === 'error',
            }"
            :disabled="saveStatus === 'saving'"
            :title="saveStatus === 'saved' ? '已保存' : '保存作品'"
            @click="handleSave"
          >
            <svg v-if="saveStatus === 'saving'" class="save-spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round" />
            </svg>
            <svg v-else-if="saveStatus === 'saved'" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <polyline points="20 6 9 17 4 12" />
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z" />
              <polyline points="17 21 17 13 7 13 7 21" />
              <polyline points="7 3 7 8 15 8" />
            </svg>
            <span>{{ saveStatus === 'saved' ? '已保存' : saveStatus === 'saving' ? '保存中' : '保存' }}</span>
          </button>
          <ExportButton
            :card-element="cardPreviewRef?.cardElement ?? null"
            :get-card-element="cardPreviewRef?.getCardElement ?? null"
          />
        </div>
      </div>
      <div class="editor-panel__inner">
        <CardEditor />
      </div>
    </aside>
  </div>
</template>

<style scoped>
/* =============================================
   Three-panel workspace — full viewport height
   ============================================= */
.editor-workspace {
  display: flex;
  height: calc(100vh - var(--gc-nav-height));
  overflow: hidden;
  background-color: var(--gc-surface-soft);
}

/* =============================================
   Side panels (left + right)
   ============================================= */
.editor-panel {
  display: flex;
  flex-direction: column;
  background-color: var(--gc-canvas);
  overflow: hidden;
}

.editor-panel--left {
  width: 20%;
  min-width: 220px;
  max-width: 280px;
  border-right: 1px solid var(--gc-hairline);
}

.editor-panel--right {
  width: 280px;
  border-left: 1px solid var(--gc-hairline);
}

.editor-panel__header {
  padding: var(--gc-space-sm) var(--gc-space-md);
  border-bottom: 1px solid var(--gc-hairline-soft);
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--gc-space-sm);
}

.editor-panel__header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

.editor-panel__save-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: var(--gc-radius-pill);
  font-family: var(--gc-font-sans);
  font-size: 12px;
  font-weight: 500;
  min-height: 32px;
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid var(--gc-hairline);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  white-space: nowrap;
}

.editor-panel__save-btn:hover:not(:disabled) {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  border-color: var(--gc-primary);
}

.editor-panel__save-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.editor-panel__save-btn.is-saved {
  background-color: #10b981;
  color: #fff;
  border-color: #10b981;
  cursor: default;
}

.editor-panel__save-btn.is-error {
  border-color: #ef4444;
  color: #ef4444;
}

@keyframes save-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.save-spinner {
  animation: save-spin 1s linear infinite;
}

.editor-panel__title {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  font-weight: var(--gc-caption-weight);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
  flex-shrink: 0;
}

.editor-panel__inner {
  flex: 1;
  overflow-y: auto;
  padding: var(--gc-space-md);
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-md);
}

.editor-panel__divider {
  height: 1px;
  background-color: var(--gc-hairline-soft);
  flex-shrink: 0;
}

/* =============================================
   Center canvas — main work area
   ============================================= */
.editor-canvas {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
  position: relative;
}

/* =============================================
   Responsive: collapse side panels on narrow screens
   ============================================= */
@media (max-width: 1100px) {
  .editor-panel--left {
    width: 240px;
  }

  .editor-panel--right {
    width: 240px;
  }
}

@media (max-width: 900px) {
  .editor-workspace {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - var(--gc-nav-height));
  }

  .editor-panel--left {
    width: 100%;
    max-width: 100%;
    min-width: unset;
    border-right: none;
    border-bottom: 1px solid var(--gc-hairline);
    max-height: 260px;
  }

  .editor-panel--right {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--gc-hairline);
    max-height: 50vh;
  }

  .editor-canvas {
    min-height: 400px;
  }
}
</style>
