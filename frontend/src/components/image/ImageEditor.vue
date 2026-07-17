<script setup lang="ts">
/**
 * ImageEditor — image cropping and shaping tool.
 *
 * Two interaction modes:
 *   移动 (pan)  — drag to reposition + scale slider to zoom
 *   裁剪 (crop) — drag/resize a crop rectangle, then apply actually crops the image
 *
 * On "apply":
 * - If a crop region exists → image is physically cropped via canvas
 * - Scale/position are saved as CSS transforms for card rendering
 * - Shape is saved as clip-path/border-radius
 */
import { ref, computed, onMounted } from 'vue'
import { useCardStore } from '@/stores/card'
import type { ImageShape, CropRegion } from '@/types'
import ShapeSelector from './ShapeSelector.vue'

const store = useCardStore()
const emit = defineEmits<{ close: [] }>()

// ── Local editor state ──
const localShape = ref<ImageShape>(store.imageConfig.shape)
const localScale = ref<number>(store.imageConfig.scale)
const localPosX = ref<number>(store.imageConfig.position.x)
const localPosY = ref<number>(store.imageConfig.position.y)
const localCrop = ref<CropRegion | null>(store.imageConfig.crop)

type EditMode = 'pan' | 'crop'
const editMode = ref<EditMode>('pan')

// ── Drag state (shared by pan and crop) ──
const previewEl = ref<HTMLElement | null>(null)
const isDragging = ref(false)
const dragOrigin = ref({ x: 0, y: 0 })

// ── Crop-specific drag state ──
const activeHandle = ref<string | null>(null)
const cropOrig = ref<CropRegion>({ x: 0, y: 0, width: 1, height: 1 })

// Image natural size (used for canvas cropping)
const imgNatural = ref({ w: 800, h: 600 })

onMounted(() => {
  if (store.uploadedImage) {
    const img = new Image()
    img.onload = () => { imgNatural.value = { w: img.naturalWidth, h: img.naturalHeight } }
    img.src = store.uploadedImage
  }
  if (!localCrop.value) {
    localCrop.value = { x: 0, y: 0, width: 1, height: 1 }
  }
})

// ── Preview container bounding rect ──
function getPreviewRect() {
  const el = previewEl.value
  if (!el) return { w: 400, h: 250 }
  const r = el.getBoundingClientRect()
  return { w: r.width, h: r.height }
}

/** Convert crop (0-1) to pixel values */
function cropToPx(c: CropRegion, p: { w: number; h: number }) {
  return { x: c.x * p.w, y: c.y * p.h, w: c.width * p.w, h: c.height * p.h }
}

// ── Crop overlay style (box-shadow mask) ──
const cropBoxStyle = computed(() => {
  if (!localCrop.value) return {}
  const p = getPreviewRect()
  const px = cropToPx(localCrop.value, p)
  return {
    left: `${px.x}px`,
    top: `${px.y}px`,
    width: `${px.w}px`,
    height: `${px.h}px`,
    boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.45)',
    cursor: activeHandle.value === 'move' ? 'grabbing' : 'move',
  }
})

// ── Preview container style ──
const previewStyle = computed(() => {
  const s: Record<string, string> = { overflow: 'hidden' }
  if (editMode.value === 'pan') {
    s['cursor'] = isDragging.value ? 'grabbing' : 'grab'
    if (localShape.value === 'circle') s['clipPath'] = 'circle(50%)'
    else if (localShape.value === 'rounded') s['borderRadius'] = '12px'
    else s['borderRadius'] = '0'
  }
  return s
})

/** Image transform in pan mode */
const imgTransform = computed(() => {
  if (editMode.value !== 'pan') return {}
  return {
    transform: `translate(${localPosX.value}px, ${localPosY.value}px) scale(${localScale.value})`,
  }
})

// ══════════════════════════════════════════════════
// UNIFIED POINTER HANDLERS (dispatch by mode)
// ══════════════════════════════════════════════════

function onPointerDown(e: PointerEvent) {
  if (editMode.value === 'pan') {
    onPanDown(e)
  }
  // Crop mode handles are initiated by handle elements (.crop-box / .crop-hnd)
  // via startCropDrag(), which uses the same drag state mechanism
}

function onPointerMove(e: PointerEvent) {
  if (!isDragging.value) return
  if (editMode.value === 'pan') {
    onPanMove(e)
  } else if (editMode.value === 'crop') {
    doCropDrag(e)
  }
}

function onPointerUp() {
  if (editMode.value === 'pan') {
    onPanUp()
  } else if (editMode.value === 'crop') {
    stopCropDrag()
  }
}

// ══════════════════════════════════════════════════
// PAN MODE
// ══════════════════════════════════════════════════

function onPanDown(e: PointerEvent) {
  isDragging.value = true
  dragOrigin.value = { x: e.clientX - localPosX.value, y: e.clientY - localPosY.value }
  if (previewEl.value) previewEl.value.setPointerCapture(e.pointerId)
}

function onPanMove(e: PointerEvent) {
  localPosX.value = e.clientX - dragOrigin.value.x
  localPosY.value = e.clientY - dragOrigin.value.y
}

function onPanUp() { isDragging.value = false }

// ══════════════════════════════════════════════════
// CROP MODE
// ══════════════════════════════════════════════════

function startCropDrag(e: PointerEvent, handle: string) {
  if (editMode.value !== 'crop' || !localCrop.value) return
  e.stopPropagation()
  isDragging.value = true
  activeHandle.value = handle
  cropOrig.value = { ...localCrop.value }
  dragOrigin.value = { x: e.clientX, y: e.clientY }
  if (previewEl.value) previewEl.value.setPointerCapture(e.pointerId)
}

function doCropDrag(e: PointerEvent) {
  if (!activeHandle.value || !localCrop.value) return
  const p = getPreviewRect()
  const dx = (e.clientX - dragOrigin.value.x) / p.w
  const dy = (e.clientY - dragOrigin.value.y) / p.h
  const o = cropOrig.value
  let { x, y, w, h } = { x: o.x, y: o.y, w: o.width, h: o.height }
  const hdl = activeHandle.value

  if (hdl === 'move') {
    x = Math.max(0, Math.min(1 - w, o.x + dx))
    y = Math.max(0, Math.min(1 - h, o.y + dy))
  } else {
    // Corner / edge resize
    if (hdl.includes('l')) { w = o.width - dx; x = o.x + dx }
    if (hdl.includes('r')) { w = o.width + dx }
    if (hdl.includes('t')) { h = o.height - dy; y = o.y + dy }
    if (hdl.includes('b')) { h = o.height + dy }

    // Clamp
    const MIN = 0.03
    if (w < MIN || h < MIN) return
    if (x < 0) { w += x; x = 0 }
    if (y < 0) { h += y; y = 0 }
    if (x + w > 1) { w = 1 - x }
    if (y + h > 1) { h = 1 - y }
  }
  localCrop.value = { x, y, width: w, height: h }
}

function stopCropDrag() {
  isDragging.value = false
  activeHandle.value = null
}

function resetCrop() {
  localCrop.value = { x: 0, y: 0, width: 1, height: 1 }
}

// ══════════════════════════════════════════════════
// IMAGE CROPPING ENGINE (canvas-based)
// ══════════════════════════════════════════════════

/**
 * 使用 Canvas 实际裁剪图片
 * 将裁剪区域（归一化坐标）应用到原始图片，生成新的图片 URL
 */
function cropImageToDataUrl(src: string, region: CropRegion): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.onload = () => {
      const sx = img.naturalWidth * region.x
      const sy = img.naturalHeight * region.y
      const sw = img.naturalWidth * region.width
      const sh = img.naturalHeight * region.height

      const canvas = document.createElement('canvas')
      canvas.width = sw
      canvas.height = sh
      const ctx = canvas.getContext('2d')
      if (!ctx) { reject(new Error('Canvas 2D context not available')); return }
      ctx.drawImage(img, sx, sy, sw, sh, 0, 0, sw, sh)
      resolve(canvas.toDataURL('image/png'))
    }
    img.onerror = () => reject(new Error('Failed to load image for cropping'))
    img.src = src
  })
}

/** 判断裁剪区域是否为"全图"（无实际裁剪） */
function isFullCrop(c: CropRegion | null): boolean {
  if (!c) return true
  return c.x <= 0.01 && c.y <= 0.01 && c.width >= 0.99 && c.height >= 0.99
}

// ══════════════════════════════════════════════════
// ACTIONS
// ══════════════════════════════════════════════════

async function apply() {
  // 非破坏性裁剪：保存裁剪区域配置，不替换原始图片
  // 预览通过 CSS clip-path 实现视觉效果
  // 实际 canvas 裁剪留待导出时执行
  store.imageConfig = {
    shape: localShape.value,
    scale: localScale.value,
    position: { x: localPosX.value, y: localPosY.value },
    crop: localCrop.value && !isFullCrop(localCrop.value)
      ? { ...localCrop.value }
      : null,
    applied: true,
  }
  emit('close')
}

function cancel() {
  localShape.value = store.imageConfig.shape
  localScale.value = store.imageConfig.scale
  localPosX.value = store.imageConfig.position.x
  localPosY.value = store.imageConfig.position.y
  localCrop.value = store.imageConfig.crop
  emit('close')
}
</script>

<template>
  <div class="img-editor">
    <!-- Header -->
    <div class="img-editor__header">
      <p class="img-editor__title">图片编辑</p>
      <button class="img-editor__close" @click="cancel" title="关闭">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" /></svg>
      </button>
    </div>

    <!-- Mode toggle -->
    <div class="img-editor__mode-toggle">
      <button class="img-editor__mode-btn" :class="{ 'is-active': editMode === 'pan' }" @click="editMode = 'pan'">移动</button>
      <button class="img-editor__mode-btn" :class="{ 'is-active': editMode === 'crop' }" @click="editMode = 'crop'">裁剪</button>
    </div>

    <!-- Preview container (unified handler for both modes) -->
    <div
      ref="previewEl"
      class="img-editor__preview"
      :style="previewStyle"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointerleave="onPointerUp"
      @pointercancel="onPointerUp"
    >
      <img
        v-if="store.uploadedImage"
        :src="store.uploadedImage"
        alt="编辑预览"
        class="img-editor__preview-img"
        :style="imgTransform"
        draggable="false"
      />

      <!-- ════ CROP MODE OVERLAY ════ -->
      <template v-if="editMode === 'crop' && localCrop">
        <div
          class="crop-box"
          :style="cropBoxStyle"
          @pointerdown.stop="e => startCropDrag(e, 'move')"
        >
          <!-- 8 resize handles -->
          <span class="crop-hnd crop-hnd--tl" @pointerdown.stop="e => startCropDrag(e, 'tl')" />
          <span class="crop-hnd crop-hnd--tr" @pointerdown.stop="e => startCropDrag(e, 'tr')" />
          <span class="crop-hnd crop-hnd--bl" @pointerdown.stop="e => startCropDrag(e, 'bl')" />
          <span class="crop-hnd crop-hnd--br" @pointerdown.stop="e => startCropDrag(e, 'br')" />
          <span class="crop-hnd crop-hnd--top" @pointerdown.stop="e => startCropDrag(e, 'top')" />
          <span class="crop-hnd crop-hnd--bottom" @pointerdown.stop="e => startCropDrag(e, 'bottom')" />
          <span class="crop-hnd crop-hnd--left" @pointerdown.stop="e => startCropDrag(e, 'left')" />
          <span class="crop-hnd crop-hnd--right" @pointerdown.stop="e => startCropDrag(e, 'right')" />
        </div>

        <button class="crop-reset-btn" @click="resetCrop">重置</button>
      </template>

      <!-- Pan hint -->
      <div v-if="editMode === 'pan'" class="img-editor__hint" :class="{ 'is-hidden': isDragging }">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M5 9l-3 3 3 3M9 5l3-3 3 3M15 19l-3 3-3-3M19 9l3 3-3 3M2 12h20M12 2v20" /></svg>
        <span>拖拽移动 · 滚轮缩放</span>
      </div>
    </div>

    <!-- Controls -->
    <div class="img-editor__controls">
      <ShapeSelector v-model="localShape" />
      <div v-if="editMode === 'pan'" class="img-editor__slider-group">
        <label class="img-editor__slider-label">
          缩放 <span class="img-editor__slider-value">{{ Math.round(localScale * 100) }}%</span>
        </label>
        <input v-model.number="localScale" type="range" min="0.5" max="3" step="0.05" class="img-editor__slider" />
        <div class="img-editor__slider-marks"><span>50%</span><span>100%</span><span>300%</span></div>
      </div>
      <div v-if="editMode === 'crop'" class="img-editor__crop-info">
        拖拽选区边缘调整大小 · 内部移动选区 · 点击"应用"后执行实际裁剪
      </div>
    </div>

    <!-- Actions -->
    <div class="img-editor__actions">
      <button class="img-editor__btn img-editor__btn--secondary" @click="cancel">取消</button>
      <button class="img-editor__btn img-editor__btn--primary" @click="apply">应用</button>
    </div>
  </div>
</template>

<style scoped>
.img-editor {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-md);
}

/* ── Header ── */
.img-editor__header {
  display: flex; align-items: center; justify-content: space-between;
}
.img-editor__title {
  font-family: var(--gc-font-mono); font-size: var(--gc-caption-size);
  letter-spacing: var(--gc-caption-tracking); text-transform: uppercase; color: var(--gc-ink);
}
.img-editor__close {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  border-radius: var(--gc-radius-full); color: var(--gc-ink); opacity: 0.4; transition: opacity 0.15s ease;
}
.img-editor__close:hover { opacity: 0.8; }

/* ── Mode toggle ── */
.img-editor__mode-toggle {
  display: flex; border-radius: var(--gc-radius-pill);
  background-color: var(--gc-surface-soft); padding: 2px;
}
.img-editor__mode-btn {
  flex: 1; padding: 4px 16px; border-radius: var(--gc-radius-pill);
  font-size: 12px; font-weight: 500; color: var(--gc-ink); opacity: 0.6;
  transition: all 0.15s ease; min-height: 30px;
}
.img-editor__mode-btn.is-active { background-color: var(--gc-primary); color: var(--gc-on-primary); opacity: 1; }

/* ── Preview ── */
.img-editor__preview {
  position: relative; width: 100%; aspect-ratio: 16 / 10;
  background-color: var(--gc-surface-soft);
  background-image: repeating-conic-gradient(var(--gc-hairline) 0 25%, transparent 0 50%);
  background-size: 16px 16px; user-select: none; touch-action: none;
}
.img-editor__preview-img {
  width: 100%; height: 100%; object-fit: contain; pointer-events: none;
}
.img-editor__hint {
  position: absolute; bottom: 8px; left: 50%; transform: translateX(-50%);
  display: flex; align-items: center; gap: 4px; padding: 4px 10px;
  border-radius: var(--gc-radius-pill); background-color: rgba(0,0,0,0.5);
  color: #fff; font-size: 11px; opacity: 0.7; pointer-events: none;
  transition: opacity 0.2s ease; white-space: nowrap;
}
.img-editor__hint.is-hidden { opacity: 0; }

/* ── Crop overlay ── */
.crop-box {
  position: absolute; z-index: 5; border: 1.5px solid #fff;
  cursor: move;
}

/* 8 resize handles */
.crop-hnd {
  position: absolute; z-index: 8;
  width: 12px; height: 12px; background: #fff;
  border: 1.5px solid var(--gc-primary); border-radius: 50%;
}
.crop-hnd--tl { top: -6px; left: -6px; cursor: nwse-resize; }
.crop-hnd--tr { top: -6px; right: -6px; cursor: nesw-resize; }
.crop-hnd--bl { bottom: -6px; left: -6px; cursor: nesw-resize; }
.crop-hnd--br { bottom: -6px; right: -6px; cursor: nwse-resize; }
.crop-hnd--top { top: -6px; left: 50%; margin-left: -6px; cursor: ns-resize; }
.crop-hnd--bottom { bottom: -6px; left: 50%; margin-left: -6px; cursor: ns-resize; }
.crop-hnd--left { left: -6px; top: 50%; margin-top: -6px; cursor: ew-resize; }
.crop-hnd--right { right: -6px; top: 50%; margin-top: -6px; cursor: ew-resize; }

/* Reset button */
.crop-reset-btn {
  position: absolute; top: 8px; right: 8px; z-index: 10;
  padding: 3px 10px; border-radius: var(--gc-radius-pill);
  background-color: rgba(0,0,0,0.5); color: #fff;
  font-size: 10px; letter-spacing: 0.3px;
  transition: background-color 0.15s ease;
}
.crop-reset-btn:hover { background-color: rgba(0,0,0,0.7); }

/* ── Controls ── */
.img-editor__controls { display: flex; flex-direction: column; gap: var(--gc-space-md); }
.img-editor__slider-group { display: flex; flex-direction: column; gap: 4px; }
.img-editor__slider-label {
  display: flex; justify-content: space-between;
  font-family: var(--gc-font-mono); font-size: 11px; letter-spacing: 0.3px;
  text-transform: uppercase; color: var(--gc-ink); opacity: 0.5;
}
.img-editor__slider-value { opacity: 0.7; }
.img-editor__slider {
  width: 100%; height: 4px; -webkit-appearance: none; appearance: none;
  background: var(--gc-hairline); border-radius: 2px; outline: none; cursor: pointer;
}
.img-editor__slider::-webkit-slider-thumb {
  -webkit-appearance: none; width: 16px; height: 16px;
  border-radius: 50%; background: var(--gc-primary); cursor: pointer; border: none;
}
.img-editor__slider-marks { display: flex; justify-content: space-between; font-size: 10px; color: var(--gc-ink); opacity: 0.3; }
.img-editor__crop-info { font-size: 11px; color: var(--gc-ink); opacity: 0.4; text-align: center; }

/* ── Actions ── */
.img-editor__actions { display: flex; gap: var(--gc-space-sm); }
.img-editor__btn {
  flex: 1; padding: 8px 16px; border-radius: var(--gc-radius-pill);
  font-family: var(--gc-font-sans); font-size: 14px; font-weight: 500;
  transition: opacity 0.15s ease; min-height: 40px;
}
.img-editor__btn:hover { opacity: 0.8; }
.img-editor__btn--primary { background-color: var(--gc-primary); color: var(--gc-on-primary); }
.img-editor__btn--secondary { background-color: var(--gc-surface-soft); color: var(--gc-ink); }
</style>
