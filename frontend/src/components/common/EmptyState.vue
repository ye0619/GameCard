<script setup lang="ts">
/**
 * EmptyState — Placeholder for empty lists / no data.
 *
 * Usage:
 *   <EmptyState
 *     title="还没有作品"
 *     description="创建一个模板开始吧"
 *   >
 *     <template #action>
 *       <AppButton @click="...">创建</AppButton>
 *     </template>
 *   </EmptyState>
 */

defineProps<{
  title?: string
  description?: string
}>()
</script>

<template>
  <div class="empty-state">
    <!-- Icon slot -->
    <div v-if="$slots.icon" class="empty-state__icon">
      <slot name="icon" />
    </div>
    <div v-else class="empty-state__icon">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1" />
        <rect x="14" y="3" width="7" height="7" rx="1" />
        <rect x="3" y="14" width="7" height="7" rx="1" />
        <rect x="14" y="14" width="7" height="7" rx="1" />
      </svg>
    </div>

    <h3 v-if="title" class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>

    <div v-if="$slots.action" class="empty-state__action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--gc-space-xxl) var(--gc-space-lg);
  text-align: center;
}

.empty-state__icon {
  color: var(--gc-ink);
  opacity: 0.2;
  margin-bottom: var(--gc-space-lg);
}

.empty-state__title {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-headline-size);
  font-weight: var(--gc-headline-weight);
  line-height: var(--gc-headline-line);
  letter-spacing: var(--gc-headline-tracking);
  margin-bottom: var(--gc-space-xs);
}

.empty-state__desc {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-size);
  font-weight: var(--gc-body-weight);
  line-height: var(--gc-body-line);
  color: var(--gc-ink);
  opacity: 0.5;
  margin-bottom: var(--gc-space-lg);
}

.empty-state__action {
  margin-top: var(--gc-space-xs);
}
</style>
