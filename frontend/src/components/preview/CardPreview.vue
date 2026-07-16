<script setup lang="ts">
/**
 * CardPreview — design-canvas card preview.
 *
 * Shows the rendered card centered on a design-tool-style canvas
 * with dot-grid background, shadow, and appropriate whitespace.
 *
 * All rendering logic stays in card/* components (frozen core).
 */
import { computed } from 'vue'
import { useCardStore } from '@/stores/card'
import { resolveTheme } from '@/utils/themeResolver'
import EmptyState from '@/components/common/EmptyState.vue'

import CardContainer from '@/components/card/CardContainer.vue'
import CardBackground from '@/components/card/CardBackground.vue'
import CardHeader from '@/components/card/CardHeader.vue'
import CardImage from '@/components/card/CardImage.vue'
import CardStats from '@/components/card/CardStats.vue'
import CardSkills from '@/components/card/CardSkills.vue'
import CardDescription from '@/components/card/CardDescription.vue'

const store = useCardStore()

const theme = computed(() => resolveTheme(store.selectedTemplate, store.cardData))

const hasData = computed(() => {
  const data = store.cardData
  return Object.keys(data).length > 0 &&
    Object.values(data).some(v => v && v.trim() !== '')
})

const statValues = computed(() => {
  const data = store.cardData
  const statKeys = ['hp', 'attack', 'defense', 'spatk', 'spdef', 'speed']
  const stats: Record<string, number> = {}
  for (const key of statKeys) {
    const val = data[key]
    stats[key] = val ? parseInt(String(val), 10) || 0 : 0
  }
  return stats
})

const name = computed(() => store.cardData['name'] ?? '')
const type = computed(() => store.cardData['type'] ?? '')
const level = computed(() => store.cardData['level'] ?? '')
const cp = computed(() => store.cardData['cp'] ?? '')
const skills = computed(() => store.cardData['skills'] ?? '')
const description = computed(() => store.cardData['description'] ?? '')
const image = computed(() => store.uploadedImage)
const templateName = computed(() => store.selectedTemplate?.name ?? '')
</script>

<template>
  <div class="canvas-area">
    <!-- Empty state: centered on canvas -->
    <EmptyState
      v-if="!hasData"
      title="等待卡片编辑"
      description="在左侧选择模板并上传图片，在右侧编辑属性"
    >
      <template #icon>
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8" stroke-linejoin="round">
          <rect x="3" y="3" width="7" height="7" rx="1" />
          <rect x="14" y="3" width="7" height="7" rx="1" />
          <rect x="3" y="14" width="7" height="7" rx="1" />
          <rect x="14" y="14" width="7" height="7" rx="1" />
        </svg>
      </template>
    </EmptyState>

    <!-- Rendered card on canvas -->
    <div v-else class="canvas-stage">
      <!-- Zoom indicator (reserved for future zoom control) -->
      <div class="canvas-zoom-badge">100%</div>

      <!-- Card with shadow -->
      <div class="canvas-card-wrapper">
        <div class="canvas-card">
          <CardContainer :theme="theme">
            <CardBackground
              :theme="theme"
              :template="store.selectedTemplate"
            />
            <div class="preview-content">
              <CardHeader
                :name="name"
                :type="type"
                :theme="theme"
                :template="store.selectedTemplate"
                :level="level"
                :cp="cp"
              />
              <CardImage
                :image="image"
                :name="name"
              />
              <CardStats
                :theme="theme"
                :stats="statValues"
              />
              <CardSkills
                :theme="theme"
                :skills="skills"
              />
              <CardDescription
                :description="description"
                :theme="theme"
              />
              <div class="preview-footer">
                <span>GameCard</span>
                <span>{{ templateName }}</span>
              </div>
            </div>
          </CardContainer>
        </div>
      </div>

      <!-- Dimension label -->
      <p class="canvas-dim">
        380 × auto
        <span class="canvas-dim-dot">·</span>
        实时预览
      </p>
    </div>
  </div>
</template>

<style scoped>
/* =============================================
   Canvas area — fills the center panel
   ============================================= */
.canvas-area {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100%;
  padding: var(--gc-space-xl);
  /* Subtle dot-grid background (design-tool canvas) */
  background-image:
    radial-gradient(circle, rgba(0, 0, 0, 0.06) 1px, transparent 1px);
  background-size: 20px 20px;
  position: relative;
}

/* =============================================
   Stage — centers the card vertically + horizontally
   ============================================= */
.canvas-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--gc-space-md);
}

/* =============================================
   Zoom badge (future zoom control placeholder)
   ============================================= */
.canvas-zoom-badge {
  position: absolute;
  bottom: var(--gc-space-md);
  right: var(--gc-space-md);
  font-family: var(--gc-font-mono);
  font-size: 11px;
  padding: 4px 10px;
  border-radius: var(--gc-radius-sm);
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  color: var(--gc-ink);
  opacity: 0.5;
  user-select: none;
}

/* =============================================
   Card wrapper — shadow + hover lift
   ============================================= */
.canvas-card-wrapper {
  display: flex;
  justify-content: center;
}

.canvas-card {
  width: 400px;
  border-radius: var(--gc-radius-lg);
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 8px 32px rgba(0, 0, 0, 0.06),
    0 24px 64px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}

/* Slight hover lift — feels like a physical card */
.canvas-card:hover {
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 12px 48px rgba(0, 0, 0, 0.10),
    0 32px 80px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* =============================================
   Card inner content (same as before)
   ============================================= */
.preview-content {
  position: relative;
  z-index: 10;
}

.preview-footer {
  margin-top: var(--gc-space-md);
  padding-top: var(--gc-space-sm);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
}

/* =============================================
   Dimension label
   ============================================= */
.canvas-dim {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  color: var(--gc-ink);
  opacity: 0.25;
  text-align: center;
}

.canvas-dim-dot {
  margin: 0 var(--gc-space-xxs);
}
</style>
