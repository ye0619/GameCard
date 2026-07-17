<script setup lang="ts">
/**
 * TemplatesPage — Browse all templates.
 *
 * Uses Template API (unchanged). Only UI is updated.
 */
import { onMounted, computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCardStore } from '@/stores/card'
import AppButton from '@/components/common/AppButton.vue'
import AppCard from '@/components/common/AppCard.vue'
import Loading from '@/components/common/Loading.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const router = useRouter()
const store = useCardStore()

const searchQuery = ref('')

onMounted(() => {
  store.loadTemplates()
})

const templates = computed(() => store.templates)
const loading = computed(() => store.loading)

/** All unique tags across templates */
const allTags = computed(() => {
  const tags = new Set<string>()
  templates.value.forEach(t => t.tags?.forEach(tag => tags.add(tag)))
  return Array.from(tags)
})

const selectedTag = ref<string | null>(null)

/** Filtered templates */
const filteredTemplates = computed(() => {
  let list = templates.value
  if (selectedTag.value) {
    list = list.filter(t => t.tags?.includes(selectedTag.value!))
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    list = list.filter(t =>
      t.name.toLowerCase().includes(q) ||
      t.description.toLowerCase().includes(q)
    )
  }
  return list
})

function startWithTemplate(templateId: string) {
  store.selectTemplate(templateId)
  router.push('/editor')
}
</script>

<template>
  <div class="templates-page">
    <div class="gc-container">
      <!-- Header -->
      <div class="templates-page__header">
        <p class="gc-eyebrow templates-page__eyebrow">Templates</p>
        <h1 class="gc-headline">模板市场</h1>
        <p class="gc-body-sm templates-page__sub">
          选择游戏风格，开始创作专属卡片
        </p>
      </div>

      <!-- Search + filter -->
      <div class="templates-page__toolbar">
        <!-- Search -->
        <div class="templates-page__search">
          <svg class="templates-page__search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索模板..."
            class="templates-page__search-input"
          />
        </div>

        <!-- Tags -->
        <div class="templates-page__tags">
          <button
            class="templates-page__tag"
            :class="{ 'is-active': selectedTag === null }"
            @click="selectedTag = null"
          >
            全部
          </button>
          <button
            v-for="tag in allTags"
            :key="tag"
            class="templates-page__tag"
            :class="{ 'is-active': selectedTag === tag }"
            @click="selectedTag = tag"
          >
            {{ tag }}
          </button>
        </div>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="templates-page__loading">
        <Loading text="加载模板中..." />
      </div>

      <!-- Grid -->
      <div
        v-else-if="filteredTemplates.length > 0"
        class="templates-page__grid"
      >
        <AppCard
          v-for="tpl in filteredTemplates"
          :key="tpl.id"
          variant="soft"
          hoverable
          class="templates-page__card"
          @click="startWithTemplate(tpl.id)"
        >
          <!-- Preview thumbnail -->
          <template #image>
            <div class="templates-page__card-thumb">
              <span class="templates-page__card-letter">
                {{ tpl.name.charAt(0) }}
              </span>
            </div>
          </template>
          <template #eyebrow>
            <span class="gc-caption">v{{ tpl.version }}</span>
          </template>
          <template #title>
            {{ tpl.name }}
          </template>
          <template #body>
            <p class="templates-page__card-desc">
              {{ tpl.description }}
            </p>
            <div class="templates-page__card-tags">
              <span
                v-for="tag in tpl.tags"
                :key="tag"
                class="templates-page__card-tag"
              >{{ tag }}</span>
            </div>
          </template>
        </AppCard>
      </div>

      <!-- Empty -->
      <EmptyState
        v-else
        title="没有匹配的模板"
        description="尝试其他搜索条件或分类"
      >
        <template #action>
          <AppButton
            variant="tertiary"
            @click="searchQuery = ''; selectedTag = null"
          >
            清除筛选
          </AppButton>
        </template>
      </EmptyState>
    </div>
  </div>
</template>

<style scoped>
.templates-page {
  padding: var(--gc-space-section) 0;
}

.templates-page__header {
  margin-bottom: var(--gc-space-xl);
}

.templates-page__eyebrow {
  opacity: 0.4;
  margin-bottom: var(--gc-space-xs);
}

.templates-page__sub {
  opacity: 0.5;
  margin-top: var(--gc-space-xs);
}

/* Toolbar */
.templates-page__toolbar {
  display: flex;
  flex-direction: column;
  gap: var(--gc-space-md);
  margin-bottom: var(--gc-space-xxl);
}

.templates-page__search {
  position: relative;
  max-width: 400px;
}

.templates-page__search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.4;
}

.templates-page__search-input {
  width: 100%;
  padding: 12px 14px 12px 42px;
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-size);
  font-weight: var(--gc-body-weight);
  line-height: var(--gc-body-line);
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-md);
  color: var(--gc-ink);
  outline: none;
  transition: border-color 0.15s ease;
}

.templates-page__search-input:focus {
  border-color: var(--gc-primary);
}

.templates-page__search-input::placeholder {
  color: var(--gc-ink);
  opacity: 0.3;
}

/* Tags */
.templates-page__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--gc-space-xs);
}

.templates-page__tag {
  font-family: var(--gc-font-sans);
  font-size: var(--gc-body-sm-size);
  font-weight: var(--gc-body-sm-weight);
  padding: 6px 16px;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  transition: background-color 0.15s ease, opacity 0.15s ease;
  min-height: 36px;
}

.templates-page__tag:hover {
  opacity: 0.8;
}

.templates-page__tag.is-active {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

/* Grid */
.templates-page__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--gc-space-lg);
}

.templates-page__loading {
  display: flex;
  justify-content: center;
  padding: var(--gc-space-xxl);
}

/* Card */
.templates-page__card {
  display: flex;
  flex-direction: column;
}

.templates-page__card-thumb {
  aspect-ratio: 16 / 10;
  background: linear-gradient(135deg, var(--gc-surface-soft), var(--gc-hairline));
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--gc-radius-sm);
  margin-bottom: var(--gc-space-sm);
}

.templates-page__card-letter {
  font-family: var(--gc-font-sans);
  font-size: 48px;
  font-weight: 700;
  opacity: 0.15;
}

.templates-page__card-desc {
  opacity: 0.6;
  margin-bottom: var(--gc-space-sm);
  line-height: 1.5;
}

.templates-page__card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--gc-space-xxs);
}

.templates-page__card-tag {
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
</style>
