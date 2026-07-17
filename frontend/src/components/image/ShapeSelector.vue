<script setup lang="ts">
/**
 * ShapeSelector — image shape preset picker.
 *
 * Lets the user choose how the image will be masked:
 *   rectangle → square crop, no rounding
 *   circle    → circular mask (avatar style)
 *   rounded   → rounded rectangle (card default)
 */
import type { ImageShape } from '@/types'

defineProps<{
  modelValue: ImageShape
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ImageShape]
}>()

const shapes: { id: ImageShape; label: string; icon: string }[] = [
  { id: 'circle', label: '圆形', icon: '○' },
  { id: 'rounded', label: '圆角', icon: '▢' },
  { id: 'rectangle', label: '矩形', icon: '▭' },
]
</script>

<template>
  <div class="shape-selector">
    <p class="shape-selector__label">形状</p>
    <div class="shape-selector__grid">
      <button
        v-for="s in shapes"
        :key="s.id"
        class="shape-selector__btn"
        :class="{ 'is-selected': modelValue === s.id }"
        @click="emit('update:modelValue', s.id)"
      >
        <span class="shape-selector__icon">{{ s.icon }}</span>
        <span class="shape-selector__text">{{ s.label }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.shape-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.shape-selector__label {
  font-family: var(--gc-font-mono);
  font-size: 11px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  color: var(--gc-ink);
  opacity: 0.5;
}

.shape-selector__grid {
  display: flex;
  gap: 8px;
}

.shape-selector__btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border-radius: var(--gc-radius-md);
  background-color: var(--gc-surface-soft);
  border: 1px solid transparent;
  color: var(--gc-ink);
  transition: border-color 0.15s ease, background-color 0.15s ease;
  min-height: 56px;
}

.shape-selector__btn:hover {
  border-color: var(--gc-hairline);
}

.shape-selector__btn.is-selected {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  border-color: var(--gc-primary);
}

.shape-selector__icon {
  font-size: 22px;
  line-height: 1;
}

.shape-selector__text {
  font-size: 11px;
  font-weight: 500;
}
</style>
