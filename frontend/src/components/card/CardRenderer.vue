<script setup lang="ts">
/**
 * CardRenderer — 通用卡片渲染器（1280×720 横版 HUD 布局）
 *
 * 基于 pokemon.css 的 futuristic HUD 风格布局：
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  ┌──────────────┐               ┌──────┐ ┌──────┐          │
 * │  │  Skill Panel  │               │ CP   │ │ LV   │          │
 * │  │  (left 340px) │               └──────┘ └──────┘          │
 * │  │               │               ┌────────────────────┐     │
 * │  │  skill 1      │   ┌──────┐   │  Radar Chart       │     │
 * │  │  skill 2      │   │ Img  │   │  420×420           │     │
 * │  │  skill 3      │   └──────┘   └────────────────────┘     │
 * │  │  ...          │                                          │
 * │  └──────────────┘               ┌────────────────────────┐ │
 * │                                 │  Identity Panel        │ │
 * │                                 │  Name / LV / Nature    │ │
 * │                                 └────────────────────────┘ │
 * └──────────────────────────────────────────────────────────────┘
 */
import { computed } from 'vue'
import type { Template, CardData, TemplateField } from '@/types'
import type { ResolvedTheme } from '@/utils/themeResolver'

import CardContainer from './CardContainer.vue'
import CardBackground from './CardBackground.vue'
import CardHeader from './CardHeader.vue'
import CardImage from './CardImage.vue'
import CardStats from './CardStats.vue'
import CardSkills from './CardSkills.vue'
import CardDescription from './CardDescription.vue'

const props = defineProps<{
  template: Template | null
  cardData: CardData
  theme: ResolvedTheme
  imageUrl: string | null
  imageStyle?: Record<string, string>
  imageTransform?: Record<string, string>
}>()

function getFieldLabel(key: string): string {
  return props.template?.fields?.find(f => f.key === key)?.label ?? key
}

// ==================== 属性统计 ====================

const statFields = computed(() => {
  const keys = props.template?.statFields ?? []
  return keys.map(key => ({
    key,
    label: getFieldLabel(key),
    type: 'NUMBER' as const,
    required: false,
    defaultValue: null,
    placeholder: null,
    options: null,
    fields: null,
  }))
})

const statValues = computed(() => {
  const stats: Record<string, number> = {}
  for (const field of statFields.value) {
    const val = props.cardData[field.key]
    stats[field.key] = val ? parseInt(String(val), 10) || 0 : 0
  }
  return stats
})

// ==================== 头部字段 ====================

const name = computed(() => props.cardData['name'] ?? '')
const nature = computed(() => props.cardData['nature'] ?? '')

/**
 * 右上角徽章：模板中 age → LV, cp → CP
 */
const headerBadges = computed(() => {
  if (!props.template) return []
  return props.template.fields
    .filter((f): f is TemplateField & { type: 'NUMBER' } => f.type === 'NUMBER')
    .map(f => ({
      key: f.key,
      label: f.key === 'age' ? 'LV' : f.label.toUpperCase(),
      value: props.cardData[f.key] ?? '',
    }))
    .filter(b => b.value !== '')
})

// ==================== 技能与描述 ====================

const skills = computed(() => props.cardData['skills'] ?? '')
const description = computed(() => props.cardData['description'] ?? '')
const templateName = computed(() => props.template?.name ?? '')

/** 计算等级数值（用于 identity panel 的 LV 显示） */
const levelValue = computed(() => {
  const age = props.cardData['age']
  return age ? parseInt(String(age), 10) || 1 : 1
})
</script>

<template>
  <CardContainer :theme="theme">
    <CardBackground :theme="theme" :template="template" />

    <!-- 右上角 CP / LV 信息 -->
    <CardHeader
      :name="name"
      :type="nature"
      :theme="theme"
      :badges="headerBadges"
    />

    <!-- 左侧技能面板 -->
    <CardSkills
      :theme="theme"
      :skills="skills"
      :template="template"
    />

    <!-- 中间角色图片 -->
    <CardImage
      :image="imageUrl"
      :name="name"
      :img-style="imageTransform"
    />

    <!-- 右侧雷达图区域 -->
    <div class="radar-panel">
      <CardStats
        :theme="theme"
        :stats="statValues"
        :stat-fields="statFields"
      />
    </div>

    <!-- 右下角身份面板 -->
    <div
      class="identity-panel"
      :style="{
        borderColor: theme.color + '66',
        background: `rgba(30, 50, 50, 0.55)`,
      }"
    >
      <!-- 角色名称 -->
      <div class="identity-panel__name" :style="{ color: theme.color }">
        {{ name || '未知角色' }}
      </div>

      <!-- 等级和属性标签行 -->
      <div class="identity-panel__meta">
        <span class="identity-panel__level">
          LV.{{ levelValue }}
        </span>

        <!-- 性格/属性标签 -->
        <span
          v-if="nature"
          class="identity-panel__tag"
          :style="{ backgroundColor: theme.color }"
        >
          <img
            v-if="theme.icon && template"
            :src="`/templates/${template.id}/icons/${theme.icon}`"
            class="identity-panel__tag-icon"
            alt=""
            @error="(e: Event) => (e.target as HTMLElement).style.display = 'none'"
          />
          {{ nature }}
        </span>
      </div>

      <!-- 描述文字 -->
      <p v-if="description" class="identity-panel__desc">
        {{ description }}
      </p>
    </div>
  </CardContainer>
</template>

<style scoped>
/* =============================================
   右侧雷达图区域
   ============================================= */
.radar-panel {
  position: absolute;
  right: 75px;
  top: 55px;
  width: 380px;
  height: 380px;
  z-index: 15;
}

/* =============================================
   右下角身份面板（identity-panel）
   对应 pokemon.css 的 .identity-panel
   ============================================= */
.identity-panel {
  position: absolute;
  right: 0;
  bottom: 32px;
  width: 500px;
  padding: 20px 30px;
  border-radius: 45px 0 0 45px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 2px solid;
  border-right: none;
  z-index: 15;
}

.identity-panel__name {
  font-size: 36px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.identity-panel__meta {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 6px;
}

.identity-panel__level {
  font-size: 24px;
  font-weight: bold;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.4);
}

.identity-panel__tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 16px;
  border-radius: 30px;
  font-size: 16px;
  font-weight: bold;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.identity-panel__tag-icon {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.identity-panel__desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.6);
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
