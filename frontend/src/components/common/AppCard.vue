<script setup lang="ts">
/**
 * AppCard — Generic card container.
 *
 * Variants:
 *   default  → white card with hairline border (pricing-card style)
 *   soft     → surface-soft background, no border (template-card style)
 *   block    → color-block style (requires gc-block--* class via class prop)
 *
 * Usage:
 *   <AppCard>
 *     <template #image>...</template>
 *     <template #title>Card title</template>
 *     <template #body>Card content</template>
 *   </AppCard>
 */

defineProps<{
  variant?: 'default' | 'soft' | 'block'
  /** Click handler for interactive cards */
  hoverable?: boolean
}>()
</script>

<template>
  <div
    class="app-card"
    :class="[
      `app-card--${variant || 'default'}`,
      { 'app-card--hoverable': hoverable },
    ]"
  >
    <!-- Image slot -->
    <div v-if="$slots.image" class="app-card__image">
      <slot name="image" />
    </div>

    <!-- Eyebrow -->
    <div v-if="$slots.eyebrow" class="app-card__eyebrow">
      <slot name="eyebrow" />
    </div>

    <!-- Title -->
    <div v-if="$slots.title" class="app-card__title">
      <slot name="title" />
    </div>

    <!-- Body -->
    <div v-if="$slots.body" class="app-card__body">
      <slot name="body" />
    </div>

    <!-- Default slot (fallback) -->
    <slot v-if="!$slots.title && !$slots.body && !$slots.image && !$slots.eyebrow" />
  </div>
</template>

<style scoped>
.app-card {
  border-radius: var(--gc-radius-md);
  overflow: hidden;
}

/* Default — white with hairline border */
.app-card--default {
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  padding: var(--gc-space-lg);
}

/* Soft — surface-soft background */
.app-card--soft {
  background-color: var(--gc-surface-soft);
  padding: var(--gc-space-md);
}

/* Block — color-block style (add gc-block--* via class) */
.app-card--block {
  padding: var(--gc-space-xxl);
}

/* Hoverable */
.app-card--hoverable {
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.app-card--hoverable:hover {
  box-shadow: var(--gc-elevation-soft);
  transform: translateY(-2px);
}

/* Slots */
.app-card__image {
  margin: calc(-1 * var(--gc-space-lg));
  margin-bottom: var(--gc-space-md);
}

.app-card__eyebrow {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-eyebrow-size);
  font-weight: var(--gc-eyebrow-weight);
  letter-spacing: var(--gc-eyebrow-tracking);
  text-transform: uppercase;
  margin-bottom: var(--gc-space-xs);
}

.app-card__title {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-card-title-size);
  font-weight: var(--gc-card-title-weight);
  line-height: var(--gc-card-title-line);
  margin-bottom: var(--gc-space-sm);
}

.app-card__body {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  line-height: var(--gc-body-sm-line);
}
</style>
