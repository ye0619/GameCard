<script setup lang="ts">
/**
 * CardPreview — design-canvas card preview.
 *
 * Shows the rendered card centered on a design-tool-style canvas
 * with dot-grid background, shadow, and appropriate whitespace.
 *
 * Supports direct image drag-to-move on the card:
 * - When an image is present and imageConfig is applied, user can
 *   drag the image directly on the card to reposition it.
 * - Cursor changes to grab/grabbing for visual feedback.
 *
 * Canvas-level concerns only:
 * - Empty state
 * - Zoom badge
 * - Card wrapper with shadow
 * - Image shape/crop styling (canvas-level editing)
 * - Direct image drag (position update)
 *
 * All card rendering is delegated to CardRenderer.
 */
import { ref, computed } from 'vue'
import { useCardStore } from '@/stores/card'
import { resolveTheme } from '@/utils/themeResolver'
import EmptyState from '@/components/common/EmptyState.vue'
import CardRenderer from '@/components/card/CardRenderer.vue'

const store = useCardStore()

const theme = computed(() => resolveTheme(store.selectedTemplate, store.cardData))

const hasData = computed(() => {
  const data = store.cardData
  return Object.keys(data).length > 0 &&
    Object.values(data).some(v => v && v.trim() !== '')
})

const image = computed(() => store.uploadedImage)

// ══════════════════════════════════════════════════
// 直接拖拽移动（在卡片预览上拖拽图片）
// ══════════════════════════════════════════════════

const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const dragOrigin = ref({ x: 0, y: 0 })
const cardEl = ref<HTMLElement | null>(null)

/** 是否可以拖拽（有图片且已应用编辑配置） */
const canDrag = computed(() => !!store.uploadedImage)

function onPointerDown(e: PointerEvent) {
  if (!canDrag.value) return
  // 忽略非左键点击
  if (e.button !== 0) return

  isDragging.value = true
  const pos = store.imageConfig.position
  dragStart.value = { x: e.clientX, y: e.clientY }
  dragOrigin.value = { x: pos.x, y: pos.y }

  // 确保 imageConfig 已启用，使变换生效
  if (!store.imageConfig.applied) {
    store.imageConfig.applied = true
  }

  if (cardEl.value) {
    cardEl.value.setPointerCapture(e.pointerId)
  }
}

function onPointerMove(e: PointerEvent) {
  if (!isDragging.value) return

  const dx = e.clientX - dragStart.value.x
  const dy = e.clientY - dragStart.value.y

  store.imageConfig = {
    ...store.imageConfig,
    position: {
      x: dragOrigin.value.x + dx,
      y: dragOrigin.value.y + dy,
    },
  }
}

function onPointerUp() {
  isDragging.value = false
}

/** Shape mask + crop style from imageConfig */
const imageShapeStyle = computed(() => {
  const s: Record<string, string> = {}
  const cfg = store.imageConfig
  if (!cfg.applied) return s

  s['overflow'] = 'hidden'

  // Crop: 用 inset clip-path 遮罩（非破坏性，原始图不变）
  if (cfg.crop) {
    const t = cfg.crop.y
    const r = 1 - cfg.crop.x - cfg.crop.width
    const b = 1 - cfg.crop.y - cfg.crop.height
    const l = cfg.crop.x
    s['clipPath'] = `inset(${t * 100}% ${r * 100}% ${b * 100}% ${l * 100}%)`
    // 有 crop 时不叠加 shape（clip-path 无法叠加）
    return s
  }

  // 无 crop：用形状 clip-path 或 borderRadius
  if (cfg.shape === 'circle') {
    s['clipPath'] = 'circle(50%)'
  } else if (cfg.shape === 'rounded') {
    s['borderRadius'] = '12px'
  } else {
    s['borderRadius'] = '0'
  }

  return s
})

/** Image transform (scale + position pan) from editor */
const imageTransform = computed((): Record<string, string> => {
  const cfg = store.imageConfig
  if (!cfg.applied) return {}
  if (cfg.scale === 1 && cfg.position.x === 0 && cfg.position.y === 0) return {}
  return {
    transform: `translate(${cfg.position.x}px, ${cfg.position.y}px) scale(${cfg.scale})`,
  }
})
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

      <!-- Card with shadow — image drag-to-move handlers -->
      <div
        ref="cardEl"
        class="canvas-card-wrapper"
        :class="{ 'is-dragging': isDragging, 'has-image': !!store.uploadedImage }"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointerleave="onPointerUp"
        @pointercancel="onPointerUp"
      >
        <div class="canvas-card">
          <CardRenderer
            :template="store.selectedTemplate"
            :card-data="store.cardData"
            :theme="theme"
            :image-url="image"
            :image-style="imageShapeStyle"
            :image-transform="imageTransform"
          />
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
   Card wrapper — shadow + hover lift + drag
   ============================================= */
.canvas-card-wrapper {
  display: flex;
  justify-content: center;
  touch-action: none;
  user-select: none;
}

/* 有图片时显示拖拽光标 */
.canvas-card-wrapper.has-image {
  cursor: grab;
}

.canvas-card-wrapper.is-dragging {
  cursor: grabbing;
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
