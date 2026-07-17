<script setup lang="ts">
/**
 * PresetIntroSelector — 预设角色简介快捷选择器
 *
 * 展示预设的角色简介列表，用户点击即可将内容应用到描述字段。
 * 显示每个预设的标题和性格提示，方便用户选择合适的背景故事。
 */
import { ref, computed } from 'vue'
import type { PresetIntroduction } from '@/types'
import { useCardStore } from '@/stores/card'

const props = defineProps<{
  fieldKey: string
  presets: PresetIntroduction[]
}>()

const store = useCardStore()

/** 当前是否已展开预设列表 */
const expanded = ref(false)

/** 当前选中的简介索引（高亮显示） */
const activeIndex = ref<number | null>(null)

/** 当前选中的性格，用于高亮推荐的预设 */
const currentNature = computed(() => store.cardData[store.selectedTemplate?.themeField ?? ''] ?? '')

/** 点击预设：填充描述字段 */
function selectIntro(index: number): void {
  const intro = props.presets[index]
  if (!intro) return
  store.cardData[props.fieldKey] = intro.content
  activeIndex.value = index
  expanded.value = false
}

/** 是否推荐此预设（性格匹配） */
function isRecommended(natureHint?: string): boolean {
  if (!natureHint || !currentNature.value) return false
  return natureHint === currentNature.value
}

/** 清除当前描述 */
function clearIntro(): void {
  store.cardData[props.fieldKey] = ''
  activeIndex.value = null
}
</script>

<template>
  <div class="preset-intro">
    <button
      class="preset-intro__toggle"
      :class="{ 'is-expanded': expanded }"
      @click="expanded = !expanded"
    >
      <svg
        width="12" height="12" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" stroke-width="2"
        class="preset-intro__icon"
      >
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
      </svg>
      <span>选择预设简介</span>
      <svg
        width="10" height="10" viewBox="0 0 24 24"
        fill="none" stroke="currentColor" stroke-width="2"
        class="preset-intro__arrow"
        :class="{ 'is-open': expanded }"
      >
        <polyline points="6 9 12 15 18 9" />
      </svg>
    </button>

    <Transition name="slide-fade">
      <div v-if="expanded" class="preset-intro__dropdown">
        <button
          v-if="store.cardData[fieldKey]"
          class="preset-intro__clear"
          @click="clearIntro"
        >
          <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
          </svg>
          清除
        </button>

        <button
          v-for="(intro, idx) in presets"
          :key="idx"
          class="preset-intro__item"
          :class="{
            'is-active': activeIndex === idx,
            'is-recommended': isRecommended(intro.natureHint),
          }"
          :title="intro.content"
          @click="selectIntro(idx)"
        >
          <div class="preset-intro__item-header">
            <span class="preset-intro__item-title">{{ intro.title }}</span>
            <span
              v-if="intro.natureHint"
              class="preset-intro__item-nature"
              :class="{ 'is-match': isRecommended(intro.natureHint) }"
            >
              {{ intro.natureHint }}
            </span>
          </div>
          <p class="preset-intro__item-preview">{{ intro.content.slice(0, 60) }}…</p>
        </button>

        <div v-if="presets.length === 0" class="preset-intro__empty">
          暂无可用预设
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.preset-intro {
  position: relative;
  margin-top: 4px;
}

.preset-intro__toggle {
  display: flex;
  align-items: center;
  gap: 5px;
  width: 100%;
  padding: 5px 8px;
  font-size: 11px;
  font-weight: 500;
  color: var(--gc-ink);
  opacity: 0.5;
  background-color: var(--gc-surface-soft);
  border-radius: var(--gc-radius-sm);
  transition: opacity 0.15s ease, background-color 0.15s ease;
  cursor: pointer;
}

.preset-intro__toggle:hover,
.preset-intro__toggle.is-expanded {
  opacity: 0.8;
  background-color: var(--gc-surface-soft);
}

.preset-intro__icon {
  flex-shrink: 0;
  opacity: 0.5;
}

.preset-intro__arrow {
  margin-left: auto;
  transition: transform 0.2s ease;
  flex-shrink: 0;
}

.preset-intro__arrow.is-open {
  transform: rotate(180deg);
}

/* Dropdown list */
.preset-intro__dropdown {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 4px;
  max-height: 220px;
  overflow-y: auto;
  background-color: var(--gc-canvas);
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-sm);
  padding: 4px;
}

.preset-intro__clear {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  font-size: 10px;
  color: var(--gc-ink);
  opacity: 0.4;
  border-radius: var(--gc-radius-sm);
  cursor: pointer;
  transition: opacity 0.15s ease, background-color 0.15s ease;
  flex-shrink: 0;
}

.preset-intro__clear:hover {
  opacity: 0.7;
  background-color: var(--gc-surface-soft);
}

.preset-intro__item {
  width: 100%;
  text-align: left;
  padding: 6px 8px;
  border-radius: var(--gc-radius-sm);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.preset-intro__item:hover {
  background-color: var(--gc-surface-soft);
}

.preset-intro__item.is-active {
  background-color: var(--gc-surface-soft);
  outline: 1px solid var(--gc-primary);
}

.preset-intro__item.is-recommended {
  border-left: 2px solid var(--gc-primary);
  padding-left: 6px;
}

.preset-intro__item-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
}

.preset-intro__item-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--gc-ink);
}

.preset-intro__item-nature {
  font-size: 9px;
  padding: 1px 5px;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  opacity: 0.5;
}

.preset-intro__item-nature.is-match {
  opacity: 1;
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

.preset-intro__item-preview {
  font-size: 10px;
  color: var(--gc-ink);
  opacity: 0.4;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preset-intro__empty {
  padding: 12px;
  text-align: center;
  font-size: 11px;
  color: var(--gc-ink);
  opacity: 0.3;
}

/* Transition */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.2s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
