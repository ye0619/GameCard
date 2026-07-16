<script setup lang="ts">
/**
 * App shell — wraps all pages with header, content area, and footer.
 */
import { RouterView } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
</script>

<template>
  <div class="app-shell">
    <AppHeader />

    <main class="app-main">
      <RouterView />
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--gc-canvas);
}

.app-main {
  flex: 1;
  padding-top: var(--gc-nav-height);
}
</style>
