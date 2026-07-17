<script setup lang="ts">
/**
 * AppButton — Design system pill buttons.
 *
 * Variants (per DESIGN-figma.md):
 *   primary   → black pill, white text (button-primary)
 *   secondary → white pill, black text, no border (button-secondary)
 *   tertiary  → text-only link style (button-tertiary-text)
 *   promo     → magenta pill (button-magenta-promo), use once per page
 *   icon      → circular 40px icon button (button-icon-circular)
 *
 * Usage:
 *   <AppButton variant="primary" @click="...">Get started</AppButton>
 *   <AppButton variant="icon" aria-label="Menu">
 *     <SomeIcon />
 *   </AppButton>
 */

defineProps<{
  variant?: 'primary' | 'secondary' | 'tertiary' | 'promo' | 'icon'
  /** Optional: render as <a> for external links */
  href?: string
  /** Open in new tab (only when href is set) */
  external?: boolean
}>()

defineEmits<{
  click: [e: MouseEvent]
}>()
</script>

<template>
  <!-- Icon button (circular) -->
  <button
    v-if="variant === 'icon'"
    class="app-btn-icon"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>

  <!-- Link-style button (tertiary) -->
  <a
    v-else-if="variant === 'tertiary' && href"
    :href="href"
    :target="external ? '_blank' : undefined"
    :rel="external ? 'noopener noreferrer' : undefined"
    class="app-btn-tertiary"
  >
    <slot />
  </a>
  <button
    v-else-if="variant === 'tertiary'"
    class="app-btn-tertiary"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>

  <!-- Promo (magenta) -->
  <a
    v-else-if="variant === 'promo' && href"
    :href="href"
    :target="external ? '_blank' : undefined"
    :rel="external ? 'noopener noreferrer' : undefined"
    class="app-btn-promo"
  >
    <slot />
  </a>
  <button
    v-else-if="variant === 'promo'"
    class="app-btn-promo"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>

  <!-- Secondary (white pill) -->
  <a
    v-else-if="variant === 'secondary' && href"
    :href="href"
    :target="external ? '_blank' : undefined"
    :rel="external ? 'noopener noreferrer' : undefined"
    class="app-btn-secondary"
  >
    <slot />
  </a>
  <button
    v-else-if="variant === 'secondary'"
    class="app-btn-secondary"
    @click="$emit('click', $event)"
  >
    <slot />
  </button>

  <!-- Primary (black pill) — default -->
  <a
    v-else
    :href="href || undefined"
    :target="external ? '_blank' : undefined"
    :rel="external ? 'noopener noreferrer' : undefined"
    class="app-btn-primary"
    @click="href ? undefined : $emit('click', $event)"
  >
    <slot />
  </a>
</template>

<style scoped>
/* Shared pill base */
.app-btn-primary,
.app-btn-secondary,
.app-btn-promo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--gc-space-xs);
  font-family: var(--gc-font-sans);
  font-size: var(--gc-button-size);
  font-weight: var(--gc-button-weight);
  line-height: var(--gc-button-line);
  letter-spacing: var(--gc-button-tracking);
  border-radius: var(--gc-radius-pill);
  padding: 10px 20px;
  min-height: 44px;
  transition: opacity 0.15s ease;
  text-decoration: none;
  white-space: nowrap;
}

.app-btn-primary:hover,
.app-btn-secondary:hover,
.app-btn-promo:hover {
  opacity: 0.8;
}

.app-btn-primary:active,
.app-btn-secondary:active,
.app-btn-promo:active {
  opacity: 0.65;
}

/* Primary */
.app-btn-primary {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

/* Secondary */
.app-btn-secondary {
  background-color: var(--gc-canvas);
  color: var(--gc-ink);
}

/* Promo (magenta, one per page) */
.app-btn-promo {
  background-color: var(--gc-accent-magenta);
  color: var(--gc-on-primary);
}

/* Tertiary — text link style */
.app-btn-tertiary {
  display: inline-flex;
  align-items: center;
  gap: var(--gc-space-xs);
  font-family: var(--gc-font-sans);
  font-size: var(--gc-link-size);
  font-weight: var(--gc-link-weight);
  line-height: var(--gc-link-line);
  letter-spacing: var(--gc-link-tracking);
  color: var(--gc-ink);
  padding: var(--gc-space-xs) var(--gc-space-sm);
  border-radius: var(--gc-radius-full);
  text-decoration: none;
  transition: opacity 0.15s ease;
  min-height: 44px;
}

.app-btn-tertiary:hover {
  opacity: 0.7;
}

/* Icon — circular 40px */
.app-btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--gc-radius-full);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  font-size: 18px;
  transition: opacity 0.15s ease;
  min-height: 44px;
  min-width: 44px;
}

.app-btn-icon:hover {
  opacity: 0.8;
}
</style>
