<script setup lang="ts">
/**
 * AppFooter — Site footer.
 *
 * Per DESIGN-figma.md (footer):
 * - White canvas background
 * - Caption-size mono headings
 * - body-sm links
 * - section-size top/bottom padding
 */

const footerLinks = [
  {
    heading: '产品',
    links: [
      { label: '模板', to: '/templates' },
      { label: '编辑器', to: '/editor' },
      { label: '我的作品', to: '/works' },
    ],
  },
  {
    heading: '资源',
    links: [
      { label: '帮助中心', to: '#' },
      { label: '更新日志', to: '#' },
    ],
  },
  {
    heading: '关于',
    links: [
      { label: '团队介绍', to: '#' },
      { label: '联系我们', to: '#' },
    ],
  },
]
</script>

<template>
  <footer class="app-footer">
    <div class="app-footer__inner">
      <!-- Wordmark -->
      <div class="app-footer__brand">
        <span class="app-footer__logo-mark">GC</span>
        <span class="app-footer__logo-text">GameCard</span>
        <p class="app-footer__tagline">
          游戏卡片生成平台
        </p>
      </div>

      <!-- Link grid -->
      <div class="app-footer__grid">
        <div
          v-for="group in footerLinks"
          :key="group.heading"
          class="app-footer__group"
        >
          <h4 class="app-footer__heading">{{ group.heading }}</h4>
          <ul class="app-footer__list">
            <li v-for="link in group.links" :key="link.label">
              <router-link
                v-if="link.to.startsWith('/')"
                :to="link.to"
                class="app-footer__link"
              >
                {{ link.label }}
              </router-link>
              <a
                v-else
                :href="link.to"
                class="app-footer__link"
              >
                {{ link.label }}
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="app-footer__bottom">
      <span>© {{ new Date().getFullYear() }} GameCard</span>
    </div>
  </footer>
</template>

<style scoped>
.app-footer {
  background-color: var(--gc-canvas);
  border-top: 1px solid var(--gc-hairline-soft);
}

.app-footer__inner {
  max-width: var(--gc-max-width);
  margin: 0 auto;
  padding: var(--gc-space-section) var(--gc-space-xxl) var(--gc-space-xxl);
  display: flex;
  gap: var(--gc-space-section);
}

@media (max-width: 960px) {
  .app-footer__inner {
    flex-direction: column;
    gap: var(--gc-space-xxl);
    padding: var(--gc-space-xxl) var(--gc-space-lg);
  }
}

/* Brand */
.app-footer__brand {
  min-width: 200px;
}

.app-footer__logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--gc-radius-md);
  background: linear-gradient(135deg, var(--gc-primary) 50%, var(--gc-block-lilac));
  color: var(--gc-on-primary);
  font-size: 14px;
  font-weight: 700;
  margin-bottom: var(--gc-space-sm);
}

.app-footer__logo-text {
  display: block;
  font-family: var(--gc-font-sans);
  font-size: 22px;
  font-weight: 600;
  letter-spacing: -0.3px;
  margin-top: var(--gc-space-xs);
}

.app-footer__tagline {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  color: var(--gc-ink);
  opacity: 0.6;
  margin-top: var(--gc-space-xxs);
}

/* Link grid */
.app-footer__grid {
  display: flex;
  gap: var(--gc-space-xxl);
  flex: 1;
}

@media (max-width: 560px) {
  .app-footer__grid {
    flex-direction: column;
    gap: var(--gc-space-xl);
  }
}

.app-footer__group {
  flex: 1;
}

.app-footer__heading {
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  font-weight: var(--gc-caption-weight);
  line-height: var(--gc-caption-line);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  margin-bottom: var(--gc-space-md);
}

.app-footer__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-sm);
}

.app-footer__link {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  color: var(--gc-ink);
  text-decoration: none;
  transition: opacity 0.15s ease;
}

.app-footer__link:hover {
  opacity: 0.6;
}

/* Bottom */
.app-footer__bottom {
  text-align: center;
  padding: var(--gc-space-lg) var(--gc-space-xxl);
  font-family: var(--gc-font-mono);
  font-size: var(--gc-caption-size);
  font-weight: var(--gc-caption-weight);
  letter-spacing: var(--gc-caption-tracking);
  text-transform: uppercase;
  color: var(--gc-ink);
  opacity: 0.4;
  border-top: 1px solid var(--gc-hairline-soft);
}
</style>
