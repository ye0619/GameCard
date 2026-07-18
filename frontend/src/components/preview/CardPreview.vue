<script setup lang="ts">
/**
 * CardPreview — design-canvas card preview with responsive scaling.
 *
 * 1280×720 卡片通过 CSS transform: scale() 自适应居中预览区域，
 * 确保任何屏幕尺寸下都能完整看到整张卡片。
 * 拖拽坐标已按缩放比例校正，保证操作手感一致。
 */
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useCardStore } from '@/stores/card'
import { resolveTheme } from '@/utils/themeResolver'
import EmptyState from '@/components/common/EmptyState.vue'
import TemplateRenderer from '@/renderer/TemplateRenderer.vue'

const CARD_W_DEFAULT = 1280
const CARD_H_DEFAULT = 720

const CARD_W = computed(() => store.selectedTemplate?.size?.width ?? CARD_W_DEFAULT)
const CARD_H = computed(() => store.selectedTemplate?.size?.height ?? CARD_H_DEFAULT)

const store = useCardStore()

const theme = computed(() => resolveTheme(store.selectedTemplate, store.cardData))

const hasData = computed(() => {
  const data = store.cardData
  return Object.keys(data).length > 0 &&
    Object.values(data).some(v => v && v.trim() !== '')
})

const image = computed(() => store.uploadedImage)

// ══════════════════════════════════════════════════
// 导出用的卡片 DOM 引用
// 通过 document.querySelector('.game-card') 动态查找，
// 脱离缩放变换树的干扰，确保 html-to-image 捕获到原生尺寸
// ══════════════════════════════════════════════════

const cardDomRef = ref<HTMLElement | null>(null)

/** 查找卡片 DOM 元素 */
function refreshCardRef() {
  if (!hasData.value) {
    cardDomRef.value = null
    return
  }
  const el = document.querySelector<HTMLElement>('.game-card')
  if (el && el !== cardDomRef.value) {
    cardDomRef.value = el
  }
}

/**
 * 对外暴露获取卡片元素的方法（比直接暴露 ref 更可靠）
 * 父组件通过 cardPreviewRef.getCardElement() 调用
 */
function getCardElement(): HTMLElement | null {
  // 先尝试验证已有的引用
  if (cardDomRef.value && cardDomRef.value.isConnected) {
    return cardDomRef.value
  }
  // 引用失效时重新查找
  refreshCardRef()
  return cardDomRef.value
}

defineExpose({
  cardElement: cardDomRef,
  getCardElement,
})

// 每次数据变化后重查卡片 DOM（使用更激进的检测策略）
watch(hasData, () => {
  // 立即尝试一次，然后 nextTick 再试一次
  refreshCardRef()
  nextTick(refreshCardRef)
  // 极端情况：再等一帧确保渲染完成
  nextTick(() => nextTick(refreshCardRef))
})
watch(theme, () => nextTick(refreshCardRef))

// ══════════════════════════════════════════════════
// 自适应缩放
// ══════════════════════════════════════════════════

const canvasAreaRef = ref<HTMLElement | null>(null)
const scale = ref(1)

function updateScale() {
  if (!canvasAreaRef.value) return
  const area = canvasAreaRef.value
  const pad = 48
  const availableW = area.clientWidth - pad
  const availableH = area.clientHeight - pad - 40
  // 计算缩放因子：让卡片尽可能利用预览空间，最大放大到 2× 以防过度模糊
  const s = Math.min(availableW / CARD_W.value, availableH / CARD_H.value)
  scale.value = Math.min(s, 2)
}

let resizeObserver: ResizeObserver | null = null

/** MutationObserver 兜底：当常规方式无法检测到卡片时轮询 DOM */
let domObserver: MutationObserver | null = null

onMounted(() => {
  updateScale()
  // 挂载时连续尝试 3 次查找卡片
  refreshCardRef()
  nextTick(refreshCardRef)
  nextTick(() => nextTick(refreshCardRef))

  if (canvasAreaRef.value) {
    resizeObserver = new ResizeObserver(updateScale)
    resizeObserver.observe(canvasAreaRef.value)
  }

  // 用 MutationObserver 监控 .canvas-area 子级变化，发现卡片时自动刷新引用
  const target = canvasAreaRef.value?.parentElement ?? document.querySelector('.editor-canvas')
  if (target) {
    domObserver = new MutationObserver(() => {
      if (!cardDomRef.value || !cardDomRef.value.isConnected) {
        refreshCardRef()
      }
    })
    domObserver.observe(target, { childList: true, subtree: true })
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  domObserver?.disconnect()
})

// ══════════════════════════════════════════════════
// 直接拖拽移动（在卡片预览上拖拽图片）
// ══════════════════════════════════════════════════

const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const dragOrigin = ref({ x: 0, y: 0 })
const cardEl = ref<HTMLElement | null>(null)

const canDrag = computed(() => !!store.uploadedImage)

function onPointerDown(e: PointerEvent) {
  if (!canDrag.value) return
  if (e.button !== 0) return

  isDragging.value = true
  const pos = store.imageConfig.position
  dragStart.value = { x: e.clientX, y: e.clientY }
  dragOrigin.value = { x: pos.x, y: pos.y }

  if (!store.imageConfig.applied) {
    store.imageConfig.applied = true
  }

  if (cardEl.value) {
    cardEl.value.setPointerCapture(e.pointerId)
  }
}

function onPointerMove(e: PointerEvent) {
  if (!isDragging.value) return

  // 除以 scale 将屏幕坐标转换为卡片空间坐标
  const dx = (e.clientX - dragStart.value.x) / scale.value
  const dy = (e.clientY - dragStart.value.y) / scale.value

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

  if (cfg.crop) {
    const t = cfg.crop.y
    const r = 1 - cfg.crop.x - cfg.crop.width
    const b = 1 - cfg.crop.y - cfg.crop.height
    const l = cfg.crop.x
    s['clipPath'] = `inset(${t * 100}% ${r * 100}% ${b * 100}% ${l * 100}%)`
    return s
  }

  if (cfg.shape === 'circle') {
    s['clipPath'] = 'circle(50%)'
  } else if (cfg.shape === 'rounded') {
    s['borderRadius'] = '12px'
  } else {
    s['borderRadius'] = '0'
  }

  return s
})
</script>

<template>
  <div ref="canvasAreaRef" class="canvas-area">
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
      <!-- Zoom badge -->
      <div class="canvas-zoom-badge">{{ Math.round(scale * 100) }}%</div>

      <!-- Card wrapper — visual size = CARD_W * scale -->
      <div
        ref="cardEl"
        class="canvas-card-wrapper"
        :class="{ 'is-dragging': isDragging, 'has-image': !!store.uploadedImage }"
        :style="{
          width: CARD_W * scale + 'px',
          height: CARD_H * scale + 'px',
        }"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="onPointerUp"
        @pointerleave="onPointerUp"
        @pointercancel="onPointerUp"
      >
        <!-- Card scaler — always CARD_W × CARD_H, visually scaled via transform -->
        <div
          class="canvas-card"
          :style="{
            transform: `scale(${scale})`,
            transformOrigin: 'top left',
            width: CARD_W + 'px',
            height: CARD_H + 'px',
          }"
        >
          <div>
            <TemplateRenderer />
          </div>
        </div>
      </div>

      <!-- Dimension label -->
      <p class="canvas-dim">
        {{ CARD_W }} × {{ CARD_H }}
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
  padding: 24px;
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
  gap: 16px;
}

/* =============================================
   Zoom badge
   ============================================= */
.canvas-zoom-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  font-family: var(--gc-font-mono);
  font-size: 11px;
  padding: 4px 10px;
  border-radius: var(--gc-radius-sm);
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  color: var(--gc-ink);
  opacity: 0.5;
  user-select: none;
  z-index: 1;
}

/* =============================================
   Card wrapper — sized container for the scaled card
   ============================================= */
.canvas-card-wrapper {
  position: relative;
  flex-shrink: 0;
  touch-action: none;
  user-select: none;
  border-radius: 12px;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 8px 32px rgba(0, 0, 0, 0.06),
    0 24px 64px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease, transform 0.3s ease;
  overflow: hidden;
}

/* Slight hover lift */
.canvas-card-wrapper:hover {
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 12px 48px rgba(0, 0, 0, 0.10),
    0 32px 80px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.canvas-card-wrapper.has-image {
  cursor: grab;
}

.canvas-card-wrapper.is-dragging {
  cursor: grabbing;
}

/* =============================================
   Card scaler — native size div, visually scaled
   ============================================= */
.canvas-card {
  overflow: hidden;
  border-radius: 12px;
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
  flex-shrink: 0;
}

.canvas-dim-dot {
  margin: 0 6px;
}
</style>
