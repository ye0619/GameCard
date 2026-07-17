<script setup lang="ts">
/**
 * CardRenderer - 通用卡片渲染器
 *
 * 根据模板配置动态渲染卡片布局。
 * 不包含任何业务逻辑，仅做数据展示编排。
 *
 * 职责：
 * 1. 从 cardData 提取展示字段
 * 2. 从 template.statFields 提取属性维度
 * 3. 从模板字段配置推导 header 徽章列表
 * 4. 按固定布局编排 Card* 组件
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
  /** 图片裁剪/形状样式（由编辑器控制） */
  imageStyle?: Record<string, string>
}>()

/** 从 template.fields 中查找字段 label */
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
 * 右侧徽章：NUMBER 类型且不在 statFields 中的字段
 * 例如：age（年龄）、cp（战斗力）等
 */
const headerBadges = computed(() => {
  if (!props.template) return []
  const statSet = new Set(props.template.statFields ?? [])
  return props.template.fields
    .filter((f): f is TemplateField & { type: 'NUMBER' } =>
      f.type === 'NUMBER' && !statSet.has(f.key),
    )
    .map(f => ({
      label: f.label,
      value: props.cardData[f.key] ?? '',
    }))
    .filter(b => b.value !== '')
})

// ==================== 技能与描述 ====================

const skills = computed(() => props.cardData['skills'] ?? '')
const description = computed(() => props.cardData['description'] ?? '')
const templateName = computed(() => props.template?.name ?? '')
</script>

<template>
  <CardContainer :theme="theme">
    <CardBackground :theme="theme" :template="template" />
    <div class="preview-content">
      <CardHeader
        :name="name"
        :type="nature"
        :theme="theme"
        :template="template"
        :badges="headerBadges"
      />
      <div :style="imageStyle">
        <CardImage :image="imageUrl" :name="name" />
      </div>
      <CardStats :theme="theme" :stats="statValues" :stat-fields="statFields" />
      <CardSkills :theme="theme" :skills="skills" />
      <CardDescription :description="description" :theme="theme" />
      <div class="preview-footer">
        <span>GameCard</span>
        <span>{{ templateName }}</span>
      </div>
    </div>
  </CardContainer>
</template>

<style scoped>
.preview-content {
  position: relative;
  z-index: 10;
}

.preview-footer {
  margin-top: var(--gc-space-md);
  padding-top: var(--gc-space-sm);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
}
</style>
