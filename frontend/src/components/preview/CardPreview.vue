<script setup lang="ts">
/**
 * CardPreview - 卡片预览组件
 *
 * 核心职责：根据模板 + 卡片数据动态渲染游戏卡片。
 *
 * 设计原则：
 * - 零硬编码：不写 if(type==="fire") 条件，所有主题来自 template.themeMapping
 * - 组件化：拆分为 CardContainer / CardBackground / CardHeader / CardImage / CardStats / CardSkills / CardDescription
 * - 配置驱动：template.json 决定渲染什么字段、什么主题
 */
import { computed } from 'vue'
import { useCardStore } from '@/stores/card'
import { resolveTheme, parseSkills } from '@/utils/themeResolver'

import CardContainer from '@/components/card/CardContainer.vue'
import CardBackground from '@/components/card/CardBackground.vue'
import CardHeader from '@/components/card/CardHeader.vue'
import CardImage from '@/components/card/CardImage.vue'
import CardStats from '@/components/card/CardStats.vue'
import CardSkills from '@/components/card/CardSkills.vue'
import CardDescription from '@/components/card/CardDescription.vue'

const store = useCardStore()

/** 解析当前主题 */
const theme = computed(() => resolveTheme(store.selectedTemplate, store.cardData))

/** 是否有数据 */
const hasData = computed(() => {
  const data = store.cardData
  return Object.keys(data).length > 0 &&
    Object.values(data).some(v => v && v.trim() !== '')
})

/** 提取数值属性给 CardStats */
const statValues = computed(() => {
  const data = store.cardData
  const statKeys = ['hp', 'attack', 'defense', 'spatk', 'spdef', 'speed']
  const stats: Record<string, number> = {}
  for (const key of statKeys) {
    const val = data[key]
    stats[key] = val ? parseInt(String(val), 10) || 0 : 0
  }
  return stats
})

/** 角色名称 */
const name = computed(() => store.cardData['name'] ?? '')

/** 属性类型 */
const type = computed(() => store.cardData['type'] ?? '')

/** 等级 */
const level = computed(() => store.cardData['level'] ?? '')

/** CP */
const cp = computed(() => store.cardData['cp'] ?? '')

/** 技能 */
const skills = computed(() => {
  const raw = store.cardData['skills']
  return raw || ''
})

/** 描述 */
const description = computed(() => store.cardData['description'] ?? '')

/** 上传图片 */
const image = computed(() => store.uploadedImage)

/** 模板名称 */
const templateName = computed(() => store.selectedTemplate?.name ?? '')
</script>

<template>
  <div class="flex items-center justify-center h-full">
    <!-- 无数据提示 -->
    <div v-if="!hasData" class="text-center text-gray-500">
      <svg class="w-16 h-16 mx-auto mb-3 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1"
          d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
      </svg>
      <p class="text-lg font-medium">等待卡片编辑</p>
      <p class="text-sm mt-1">填写左侧信息，右侧将实时预览</p>
    </div>

    <!-- 卡片预览 -->
    <div v-else class="relative" style="width: 380px;">
      <CardContainer :theme="theme">
        <!-- 背景 -->
        <CardBackground
          :theme="theme"
          :template="store.selectedTemplate"
        />

        <!-- 内容区（在背景之上） -->
        <div class="relative z-10">
          <!-- 头部 -->
          <CardHeader
            :name="name"
            :type="type"
            :theme="theme"
            :template="store.selectedTemplate"
            :level="level"
            :cp="cp"
          />

          <!-- 图片 -->
          <CardImage
            :image="image"
            :name="name"
          />

          <!-- 属性 -->
          <CardStats
            :theme="theme"
            :stats="statValues"
          />

          <!-- 技能 -->
          <CardSkills
            :theme="theme"
            :skills="skills"
          />

          <!-- 描述 -->
          <CardDescription
            :description="description"
            :theme="theme"
          />

          <!-- 底部署名 -->
          <div class="mt-4 pt-3 border-t border-gray-800 flex justify-between text-[10px] text-gray-500">
            <span>GameCard</span>
            <span>{{ templateName }}</span>
          </div>
        </div>
      </CardContainer>

      <!-- 尺寸提示 -->
      <p class="text-center text-xs text-gray-600 mt-3">380 × 自动 · 实时预览</p>
    </div>
  </div>
</template>
