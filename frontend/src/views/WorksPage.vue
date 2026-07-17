<script setup lang="ts">
/**
 * WorksPage — User's card works gallery.
 *
 * Currently shows empty state; reserved for future implementation.
 */
import { useRouter } from 'vue-router'
import AppButton from '@/components/common/AppButton.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
</script>

<template>
  <div class="works-page">
    <div class="gc-container">
      <!-- Header -->
      <div class="works-page__header">
        <p class="gc-eyebrow works-page__eyebrow">Gallery</p>
        <h1 class="gc-headline">我的作品</h1>
        <p class="gc-body-sm works-page__sub">
          管理你创建的游戏卡片
        </p>
      </div>

      <!-- Empty state -->
      <div class="works-page__content">
        <EmptyState
          title="还没有作品"
          description="开始创作你的第一张游戏卡片"
        >
          <template #icon>
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7" rx="1" />
              <rect x="14" y="3" width="7" height="7" rx="1" />
              <rect x="3" y="14" width="7" height="7" rx="1" />
              <rect x="14" y="14" width="7" height="7" rx="1" />
            </svg>
          </template>
          <template #action>
            <AppButton
              variant="primary"
              @click="router.push('/editor')"
            >
              开始创作
            </AppButton>
          </template>
        </EmptyState>
      </div>
    </div>
  </div>
</template>

<style scoped>
.works-page {
  padding: var(--gc-space-section) 0;
}

.works-page__header {
  margin-bottom: var(--gc-space-xxl);
}

.works-page__eyebrow {
  opacity: 0.4;
  margin-bottom: var(--gc-space-xs);
}

.works-page__sub {
  opacity: 0.5;
  margin-top: var(--gc-space-xs);
}

.works-page__content {
  display: flex;
  justify-content: center;
  padding: var(--gc-space-xxl) 0;
}
</style>
