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
import { useCardStore } from '@/stores/card'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import TemplateSelector from '@/components/template/TemplateSelector.vue'
import CardEditor from '@/components/editor/CardEditor.vue'
import CardPreview from '@/components/preview/CardPreview.vue'
import ExportButton from '@/components/export/ExportButton.vue'

const store = useCardStore()

const cardPreviewRef = ref<InstanceType<typeof CardPreview> | null>(null)

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
        <ExportButton
          :card-element="cardPreviewRef?.cardElement ?? null"
          :get-card-element="cardPreviewRef?.getCardElement ?? null"
        />
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
