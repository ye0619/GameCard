<script setup lang="ts">
/**
 * AppHeader — Top navigation bar.
 *
 * Per DESIGN-figma.md (top-nav):
 * - 56px fixed white bar
 * - Logo + nav links + CTA button pair
 * - Collapses to hamburger below 960px
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from './AppButton.vue'

const router = useRouter()
const mobileMenuOpen = ref(false)

const navLinks = [
  { label: '模板', to: '/templates' },
  { label: '编辑器', to: '/editor' },
  { label: '我的作品', to: '/works' },
]
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <!-- Logo -->
      <button
        class="app-header__logo"
        @click="router.push('/')"
      >
        <span class="app-header__logo-mark">GC</span>
        <span class="app-header__logo-text">GameCard</span>
      </button>

      <!-- Desktop Nav -->
      <nav class="app-header__nav">
        <button
          v-for="link in navLinks"
          :key="link.to"
          class="app-header__link"
          @click="router.push(link.to)"
        >
          {{ link.label }}
        </button>
      </nav>

      <!-- Desktop CTA -->
      <div class="app-header__actions">
        <AppButton
          variant="primary"
          @click="router.push('/editor')"
        >
          开始创作
        </AppButton>
      </div>

      <!-- Mobile hamburger -->
      <button
        class="app-header__hamburger"
        aria-label="菜单"
        @click="mobileMenuOpen = !mobileMenuOpen"
      >
        <span
          class="hamburger-line"
          :class="{ 'is-open': mobileMenuOpen }"
        />
        <span
          class="hamburger-line"
          :class="{ 'is-open': mobileMenuOpen }"
        />
        <span
          class="hamburger-line"
          :class="{ 'is-open': mobileMenuOpen }"
        />
      </button>
    </div>

    <!-- Mobile overlay -->
    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="app-header__mobile-overlay"
        @click="mobileMenuOpen = false"
      >
        <nav class="app-header__mobile-nav" @click.stop>
          <button
            v-for="link in navLinks"
            :key="link.to"
            class="app-header__mobile-link"
            @click="router.push(link.to); mobileMenuOpen = false"
          >
            {{ link.label }}
          </button>
          <AppButton
            variant="primary"
            style="width: 100%; margin-top: 16px;"
            @click="router.push('/editor'); mobileMenuOpen = false"
          >
            开始创作
          </AppButton>
        </nav>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  height: var(--gc-nav-height);
  background-color: var(--gc-canvas);
  border-bottom: 1px solid var(--gc-hairline);
}

.app-header__inner {
  max-width: var(--gc-max-width);
  margin: 0 auto;
  padding: 0 var(--gc-space-xxl);
  height: 100%;
  display: flex;
  align-items: center;
  gap: var(--gc-space-xl);
}

@media (max-width: 768px) {
  .app-header__inner {
    padding: 0 var(--gc-space-lg);
  }
}

.app-header__logo {
  display: flex;
  align-items: center;
  gap: var(--gc-space-sm);
  cursor: pointer;
}

.app-header__logo-mark {
  width: 32px;
  height: 32px;
  border-radius: var(--gc-radius-md);
  background: linear-gradient(135deg, var(--gc-primary) 50%, var(--gc-block-lilac));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--gc-on-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.app-header__logo-text {
  font-family: var(--gc-font-sans);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.3px;
}

/* Desktop nav */
.app-header__nav {
  display: flex;
  align-items: center;
  gap: var(--gc-space-xs);
  flex: 1;
}

@media (max-width: 960px) {
  .app-header__nav {
    display: none;
  }
}

.app-header__link {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  padding: var(--gc-space-xs) var(--gc-space-sm);
  border-radius: var(--gc-radius-full);
  color: var(--gc-ink);
  transition: background-color 0.15s ease;
  min-height: 36px;
}

.app-header__link:hover {
  background-color: var(--gc-surface-soft);
}

.app-header__actions {
  display: flex;
  align-items: center;
  gap: var(--gc-space-sm);
}

@media (max-width: 960px) {
  .app-header__actions {
    display: none;
  }
}

/* Hamburger */
.app-header__hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: 40px;
  height: 40px;
  padding: 8px;
  margin-left: auto;
}

@media (max-width: 960px) {
  .app-header__hamburger {
    display: flex;
  }
}

.hamburger-line {
  display: block;
  width: 100%;
  height: 2px;
  background-color: var(--gc-ink);
  border-radius: 2px;
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.hamburger-line.is-open:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.hamburger-line.is-open:nth-child(2) {
  opacity: 0;
}

.hamburger-line.is-open:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

/* Mobile overlay */
.app-header__mobile-overlay {
  position: fixed;
  top: var(--gc-nav-height);
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.98);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--gc-space-xxl);
}

.app-header__mobile-nav {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-xs);
  width: 100%;
  max-width: 400px;
}

.app-header__mobile-link {
  font-family: var(--gc-font-sans);
  font-size: 22px;
  font-weight: 480;
  padding: var(--gc-space-md) 0;
  text-align: center;
  color: var(--gc-ink);
  border-bottom: 1px solid var(--gc-hairline-soft);
}

/* Transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
