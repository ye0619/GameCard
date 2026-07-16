<script setup lang="ts">
/**
 * Home — Product landing page.
 *
 * Content sections (per DESIGN-figma.md rhythm):
 * 1. Hero (white canvas)
 * 2. Template showcase (white canvas)
 * 3. Color block section (lime)
 * 4. Tech / AI block (navy inverse)
 * 5. Recent works (white canvas)
 */
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCardStore } from '@/stores/card'
import AppButton from '@/components/common/AppButton.vue'
import AppCard from '@/components/common/AppCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Loading from '@/components/common/Loading.vue'

const router = useRouter()
const store = useCardStore()

onMounted(() => {
  store.loadTemplates()
})

const templates = computed(() => store.templates)
const loading = computed(() => store.loading)

/** Navigate to editor with a pre-selected template */
function startWithTemplate(templateId: string) {
  store.selectTemplate(templateId)
  router.push('/editor')
}
</script>

<template>
  <div class="home">
    <!-- ============================================
         Section 1: Hero
         ============================================ -->
    <section class="home-hero">
      <div class="gc-container">
        <div class="home-hero__inner">
          <p class="gc-eyebrow home-hero__eyebrow">GameCard</p>

          <h1 class="gc-display-xl home-hero__title">
            Create your own<br />
            <span class="home-hero__highlight">game universe</span>
          </h1>

          <p class="gc-body-lg home-hero__desc">
            Generate unique game cards with dynamic themes,<br class="hide-mobile" />
            powered by templates and AI-assisted creation.
          </p>

          <div class="home-hero__actions">
            <AppButton
              variant="primary"
              @click="router.push('/editor')"
            >
              开始创作
            </AppButton>
            <AppButton
              variant="secondary"
              @click="router.push('/templates')"
            >
              浏览模板
            </AppButton>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================================
         Section 2: Template showcase
         ============================================ -->
    <section class="home-section">
      <div class="gc-container">
        <div class="home-section__header">
          <p class="gc-eyebrow">Templates</p>
          <h2 class="gc-headline">选择模板开始设计</h2>
          <p class="gc-body-sm home-section__sub">
            多种游戏风格模板，一键切换主题
          </p>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="home-section__loading">
          <Loading text="加载模板中..." />
        </div>

        <!-- Template grid -->
        <div
          v-else-if="templates.length > 0"
          class="home-template-grid"
        >
          <AppCard
            v-for="tpl in templates"
            :key="tpl.id"
            variant="soft"
            hoverable
            class="home-template-card"
            @click="startWithTemplate(tpl.id)"
          >
            <template #eyebrow>
              <span class="gc-caption">{{ tpl.version }}</span>
            </template>
            <template #title>
              {{ tpl.name }}
            </template>
            <template #body>
              <p class="home-template-desc">{{ tpl.description }}</p>
              <div class="home-template-tags">
                <span
                  v-for="tag in tpl.tags"
                  :key="tag"
                  class="home-template-tag"
                >{{ tag }}</span>
              </div>
            </template>
          </AppCard>
        </div>

        <!-- Empty -->
        <EmptyState
          v-else
          title="暂无模板"
          description="模板加载失败，请稍后重试"
        />
      </div>
    </section>

    <!-- ============================================
         Section 3: Color block — 创作理念
         ============================================ -->
    <section class="home-section home-section--full">
      <div class="gc-container">
        <div class="gc-block gc-block--lime home-block">
          <p class="gc-eyebrow" style="margin-bottom: 12px;">AI-Powered</p>
          <h2 class="gc-headline" style="margin-bottom: 16px;">
            主题驱动 · 一键生成
          </h2>
          <p class="gc-subhead" style="max-width: 600px; opacity: 0.85;">
            输入角色数据，选择属性类型，卡片自动匹配对应主题。
            火属性自动使用红色主题，水属性自动切换蓝色主题。
          </p>
          <div style="margin-top: 24px;">
            <AppButton
              variant="primary"
              @click="router.push('/editor')"
            >
              体验主题系统
            </AppButton>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================================
         Section 4: Dark block — 技术能力
         ============================================ -->
    <section class="home-section home-section--full">
      <div class="gc-container">
        <div class="gc-block gc-block--navy home-block">
          <p class="gc-eyebrow" style="margin-bottom: 12px; color: rgba(255,255,255,0.6);">Extensible</p>
          <h2 class="gc-headline" style="margin-bottom: 16px; color: #fff;">
            可扩展的模板引擎
          </h2>
          <p class="gc-subhead" style="max-width: 600px; opacity: 0.75; color: rgba(255,255,255,0.85);">
            新增游戏主题无需修改代码。只需创建新的 template.json 和资源文件，
            系统自动发现并加载。
          </p>
          <div style="margin-top: 24px;">
            <AppButton
              variant="secondary"
              @click="router.push('/templates')"
            >
              查看所有模板
            </AppButton>
          </div>
        </div>
      </div>
    </section>

    <!-- ============================================
         Section 5: Recent works
         ============================================ -->
    <section class="home-section">
      <div class="gc-container">
        <div class="home-section__header">
          <p class="gc-eyebrow">Gallery</p>
          <h2 class="gc-headline">最近作品</h2>
          <p class="gc-body-sm home-section__sub">
            你的创作将在这里展示
          </p>
        </div>

        <EmptyState
          title="还没有作品"
          description="开始创作第一张游戏卡片"
        >
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
    </section>
  </div>
</template>

<style scoped>
/* =============================================
   Hero
   ============================================= */
.home-hero {
  padding: var(--gc-space-section) 0;
}

.home-hero__inner {
  max-width: 800px;
}

.home-hero__eyebrow {
  margin-bottom: var(--gc-space-lg);
  opacity: 0.5;
}

.home-hero__title {
  margin-bottom: var(--gc-space-lg);
}

.home-hero__highlight {
  display: inline-block;
  background: linear-gradient(135deg, var(--gc-block-lilac), var(--gc-block-lime));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.home-hero__desc {
  margin-bottom: var(--gc-space-xl);
  opacity: 0.7;
}

.home-hero__actions {
  display: flex;
  gap: var(--gc-space-sm);
  flex-wrap: wrap;
}

/* =============================================
   Shared section
   ============================================= */
.home-section {
  padding: var(--gc-space-section) 0;
}

.home-section--full {
  padding: var(--gc-space-xxl) 0;
}

.home-section__header {
  margin-bottom: var(--gc-space-xxl);
}

.home-section__sub {
  opacity: 0.5;
  margin-top: var(--gc-space-xs);
}

.home-section__loading {
  display: flex;
  justify-content: center;
}

/* =============================================
   Template grid
   ============================================= */
.home-template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--gc-space-lg);
}

.home-template-card {
  display: flex;
  flex-direction: column;
}

.home-template-desc {
  opacity: 0.6;
  margin-bottom: var(--gc-space-sm);
  line-height: 1.5;
}

.home-template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--gc-space-xxs);
}

.home-template-tag {
  font-family: var(--gc-font-mono);
  font-size: 10px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  padding: 2px 8px;
  border-radius: var(--gc-radius-sm);
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
  opacity: 0.7;
}

/* =============================================
   Color blocks
   ============================================= */
.home-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

/* =============================================
   Responsive
   ============================================= */
@media (max-width: 768px) {
  .home-hero {
    padding: var(--gc-space-xxl) 0;
  }

  .hide-mobile {
    display: none;
  }

  .home-template-grid {
    grid-template-columns: 1fr;
  }

  .home-hero__title {
    font-size: var(--gc-display-lg-size);
    line-height: var(--gc-display-lg-line);
    letter-spacing: var(--gc-display-lg-tracking);
  }
}
</style>
