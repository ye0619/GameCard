<script setup lang="ts">
/**
 * TemplateSelector — template selection list.
 *
 * Displays available templates from the store.
 * UI uses design tokens per DESIGN-figma.md.
 */
import { useCardStore } from '@/stores/card'
import Loading from '@/components/common/Loading.vue'

const store = useCardStore()
</script>

<template>
  <div class="tpl-selector">
    <p class="tpl-selector__label">选择模板</p>

    <!-- Loading -->
    <div v-if="store.loading" class="tpl-selector__loading">
      <Loading text="加载中..." />
    </div>

    <!-- List -->
    <div v-else class="tpl-selector__list">
      <button
        v-for="tpl in store.templates"
        :key="tpl.id"
        class="tpl-selector__card"
        :class="{ 'is-selected': store.selectedTemplateId === tpl.id }"
        @click="store.selectTemplate(tpl.id)"
      >
        <!-- Icon / initial -->
        <div
          class="tpl-selector__icon"
          :class="{ 'is-selected': store.selectedTemplateId === tpl.id }"
        >
          {{ tpl.name.charAt(0) }}
        </div>

        <!-- Info -->
        <div class="tpl-selector__info">
          <div class="tpl-selector__name">{{ tpl.name }}</div>
          <div class="tpl-selector__desc">{{ tpl.description }}</div>
        </div>

        <!-- Check mark -->
        <div
          v-if="store.selectedTemplateId === tpl.id"
          class="tpl-selector__check"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
      </button>
    </div>

    <!-- Empty -->
    <p v-if="!store.loading && store.templates.length === 0" class="tpl-selector__empty">
      暂无可用模板
    </p>
  </div>
</template>

<style scoped>
.tpl-selector {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-sm);
}

.tpl-selector__label {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  font-weight: var(--gc-caption-weight);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
}

.tpl-selector__loading {
  display: flex;
  justify-content: center;
  padding: var(--gc-space-md) 0;
}

.tpl-selector__list {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-xs);
}

/* Card */
.tpl-selector__card {
  display: flex;
  align-items: center;
  gap: var(--gc-space-sm);
  width: 100%;
  padding: 8px var(--gc-space-sm);
  border-radius: var(--gc-radius-md);
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  text-align: left;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.tpl-selector__card:hover {
  border-color: var(--gc-ink);
  opacity: 0.85;
}

.tpl-selector__card.is-selected {
  background-color: var(--gc-primary);
  border-color: var(--gc-primary);
}

/* Icon */
.tpl-selector__icon {
  width: 40px;
  height: 40px;
  border-radius: var(--gc-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--gc-font-sans);
  font-size: 18px;
  font-weight: 600;
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  flex-shrink: 0;
  transition: background-color 0.15s ease, color 0.15s ease;
}

.tpl-selector__icon.is-selected {
  background-color: rgba(255, 255, 255, 0.2);
  color: var(--gc-on-primary);
}

/* Info */
.tpl-selector__info {
  flex: 1;
  min-width: 0;
}

.tpl-selector__name {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: 540;
  color: var(--gc-ink);
  transition: color 0.15s ease;
}

.tpl-selector__card.is-selected .tpl-selector__name {
  color: var(--gc-on-primary);
}

.tpl-selector__desc {
  font-family: var(--gc-font-sans);
  font-size: 12px;
  font-weight: var(--gc-body-sm-weight);
  color: var(--gc-ink);
  opacity: 0.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity 0.15s ease;
}

.tpl-selector__card.is-selected .tpl-selector__desc {
  color: var(--gc-on-primary);
  opacity: 0.6;
}

/* Check */
.tpl-selector__check {
  width: 24px;
  height: 24px;
  border-radius: var(--gc-radius-full);
  background-color: var(--gc-on-primary);
  color: var(--gc-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* Empty */
.tpl-selector__empty {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  color: var(--gc-ink);
  opacity: 0.4;
  padding: var(--gc-space-md) 0;
  text-align: center;
}
</style>
