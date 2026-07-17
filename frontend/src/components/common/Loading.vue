<script setup lang="ts">
/**
 * Loading — Loading indicator.
 *
 * Usage:
 *   <Loading />
 *   <Loading text="正在加载模板..." />
 */

defineProps<{
  text?: string
}>()
</script>

<template>
  <div class="loading">
    <span class="loading__spinner" />
    <p v-if="text" class="loading__text">{{ text }}</p>
    <slot />
  </div>
</template>

<style scoped>
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--gc-space-md);
  padding: var(--gc-space-xxl);
}

.loading__spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--gc-hairline);
  border-top-color: var(--gc-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading__text {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  color: var(--gc-ink);
  opacity: 0.5;
}
</style>
