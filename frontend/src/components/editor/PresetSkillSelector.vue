<script setup lang="ts">
/**
 * PresetSkillSelector — 预设技能快捷选择器
 *
 * 按分类展示预设技能标签，点击即可将技能添加到卡片技能列表中。
 * 已在列表中的技能显示为已禁用状态，避免重复添加。
 */
import { ref, computed } from 'vue'
import type { PresetSkill } from '@/types'
import { useCardStore } from '@/stores/card'

const props = defineProps<{
  fieldKey: string
  presets: PresetSkill[]
}>()

const store = useCardStore()

/** 当前已有的技能名称集合，用于去重检测 */
const existingSkills = computed(() => {
  const raw = store.cardData[props.fieldKey] ?? ''
  if (!raw || raw.trim() === '') return new Set<string>()
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return new Set(parsed.map((item: unknown) =>
        typeof item === 'string' ? item : String((item as Record<string, unknown>).name ?? ''),
      ).filter(Boolean))
    }
  } catch { /* fall through */ }
  return new Set<string>()
})

/** 按分类分组的预设技能 */
const grouped = computed(() => {
  const groups: Record<string, PresetSkill[]> = {}
  for (const skill of props.presets) {
    const cat = skill.category ?? '通用'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(skill)
  }
  return groups
})

/** 分类排序（攻击 > 防御 > 辅助 > 特殊 > 其他） */
const categoryOrder: Record<string, number> = {
  '攻击': 0, '防御': 1, '辅助': 2, '特殊': 3,
}

const sortedGroups = computed(() => {
  return Object.entries(grouped.value).sort(([a], [b]) => {
    return (categoryOrder[a] ?? 99) - (categoryOrder[b] ?? 99)
  })
})

/** 点击预设技能：添加到卡片 */
function addSkill(skillName: string): void {
  if (existingSkills.value.has(skillName)) return

  const raw = store.cardData[props.fieldKey] ?? ''
  let items: Array<{ name: string }> = []

  if (raw && raw.trim()) {
    try {
      items = JSON.parse(raw)
      if (!Array.isArray(items)) items = []
    } catch { items = [] }
  }

  items.push({ name: skillName })
  store.cardData[props.fieldKey] = JSON.stringify(items)
}

/** 判断技能是否已存在 */
function isAdded(skillName: string): boolean {
  return existingSkills.value.has(skillName)
}
</script>

<template>
  <div class="preset-skills">
    <div class="preset-skills__hint">
      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" />
        <line x1="12" y1="16" x2="12" y2="12" />
        <line x1="12" y1="8" x2="12.01" y2="8" />
      </svg>
      <span>点击预设技能快速添加</span>
    </div>

    <div
      v-for="[category, skills] in sortedGroups"
      :key="category"
      class="preset-skills__group"
    >
      <span class="preset-skills__cat-label">{{ category }}</span>
      <div class="preset-skills__tags">
        <button
          v-for="skill in skills"
          :key="skill.name"
          class="preset-skills__tag"
          :class="{
            'is-added': isAdded(skill.name),
            'is-high': (skill.power ?? 0) >= 80,
            'is-mid': (skill.power ?? 0) >= 50 && (skill.power ?? 0) < 80,
          }"
          :title="skill.description ?? skill.name"
          :disabled="isAdded(skill.name)"
          @click="addSkill(skill.name)"
        >
          {{ skill.name }}
          <span v-if="skill.power" class="preset-skills__power">{{ skill.power }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.preset-skills {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 6px 8px;
  background-color: var(--gc-surface-soft);
  border-radius: var(--gc-radius-sm);
  margin-top: 4px;
}

.preset-skills__hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: var(--gc-ink);
  opacity: 0.35;
}

.preset-skills__group {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.preset-skills__cat-label {
  font-size: 9px;
  font-weight: 600;
  color: var(--gc-ink);
  opacity: 0.4;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.preset-skills__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 3px;
}

.preset-skills__tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 7px;
  font-size: 11px;
  font-weight: 500;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-canvas);
  color: var(--gc-ink);
  opacity: 0.65;
  transition: opacity 0.15s ease, background-color 0.15s ease;
  cursor: pointer;
  min-height: 22px;
}

.preset-skills__tag:hover:not(:disabled) {
  opacity: 1;
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

.preset-skills__tag.is-high:hover:not(:disabled) {
  background-color: #EF4444;
}

.preset-skills__tag.is-mid:hover:not(:disabled) {
  background-color: #F59E0B;
}

.preset-skills__tag.is-added {
  opacity: 0.5;
  cursor: default;
}

.preset-skills__power {
  font-size: 9px;
  font-weight: 700;
  opacity: 0.5;
}

.preset-skills__tag:hover .preset-skills__power {
  opacity: 0.8;
}
</style>
