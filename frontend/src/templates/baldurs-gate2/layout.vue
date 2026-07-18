<script setup lang="ts">
/**
 * Baldur's Gate 2 — 角色卡模板布局
 *
 * 基于经典的 AD&D 2e 角色纸设计：
 *
 * ┌──────────────────────────────────────────────────────────────┐
 * │  石质边框                                                   │
 * │  ┌───────────────────────────────────────────────┐          │
 * │  │              角色名 (Title)                    │          │
 * │  ├──────┬──────────────────┬────────────────────┤          │
 * │  │ 装备 │   角色肖像        │  角色信息           │          │
 * │  │ 栏   │   (Portrait)      │  (种族/职业/背景)   │          │
 * │  │      │                   │                     │          │
 * │  │      │  种族: xxx        │  能力列表           │          │
 * │  │      │                   │  ◆ 技能1           │          │
 * │  │      │                   │  ◆ 技能2           │          │
 * │  │      └───────────────────┘                     │          │
 * │  │       属性面板                                  │          │
 * │  │       STR 18  ■■■■■■■■■■                      │          │
 * │  │       DEX 15  ■■■■■■■■                        │          │
 * │  │       CON 16  ■■■■■■■■■                       │          │
 * │  │       INT 10  ■■■■■                            │          │
 * │  │       WIS 12  ■■■■■■                           │          │
 * │  │       CHA 14  ■■■■■■■                          │          │
 * │  ├──────┴──────────────────┴────────────────────┤          │
 * │  │  状态:  HP 80/80  |  AC 3  |  Level 12       │          │
 * │  └───────────────────────────────────────────────┘          │
 * └──────────────────────────────────────────────────────────────┘
 */
import { computed } from 'vue'
import type { TemplateLayoutProps } from '@/renderer/types'
import CardBackground from '@/components/card/CardBackground.vue'
import CardHeader, { type BadgeItem } from '@/components/card/CardHeader.vue'
import CardImage from '@/components/card/CardImage.vue'
import EquipmentPanel from './components/EquipmentPanel.vue'
import AttributePanel from './components/AttributePanel.vue'
import PortraitPanel from './components/PortraitPanel.vue'
import InfoPanel from './components/InfoPanel.vue'
import StatusBar from './components/StatusBar.vue'
import StoneFrame from './components/StoneFrame.vue'

const props = defineProps<TemplateLayoutProps>()

const name = computed(() => props.cardData['name'] ?? '')
const nature = computed(() => props.cardData['nature'] ?? '')
const race = computed(() => props.cardData['race'] ?? '')
const description = computed(() => props.cardData['description'] ?? '')

// 属性数据
const statFields = computed(() => {
  const keys = props.template?.statFields ?? []
  return keys.map(key => ({
    key,
    label: props.template?.fields?.find(f => f.key === key)?.label ?? key,
    value: parseInt(props.cardData[key] ?? '0', 10) || 0,
  }))
})

// 技能列表
const skills = computed(() => {
  const raw = props.cardData['skills'] ?? ''
  if (!raw || !raw.trim()) return []
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return parsed.map((item: any) => item.name ?? String(item)).filter(Boolean)
    }
  } catch {
    return raw.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
})

// 右上角徽章
const badges = computed((): BadgeItem[] => {
  const items: BadgeItem[] = []
  if (props.cardData['level']?.trim()) {
    items.push({ label: 'LEVEL', value: props.cardData['level'], variant: 'pill' })
  }
  if (props.cardData['hp']?.trim()) {
    items.push({ label: 'HP', value: props.cardData['hp'], variant: 'pill' })
  }
  if (props.cardData['ac']?.trim()) {
    items.push({ label: 'AC', value: props.cardData['ac'], variant: 'pill' })
  }
  return items
})
</script>

<template>
  <div class="game-card character-sheet">
    <!-- 背景层 -->
    <CardBackground :theme="theme" :template="template" />

    <!-- 石质装饰边框 -->
    <StoneFrame />

    <!-- 右上角徽章 -->
    <CardHeader
      :badges="badges"
      :theme="theme"
    />

    <!-- 左上方标题 -->
    <div class="sheet-title" :style="{ color: theme.color, borderBottomColor: theme.color + '66' }">
      {{ name || '无名英雄' }}
      <span v-if="nature" class="sheet-title__class" :style="{ backgroundColor: theme.color }">
        {{ nature }}
      </span>
    </div>

    <!-- 主内容三列网格 -->
    <div class="sheet-content">
      <!-- 左：装备栏 -->
      <EquipmentPanel :theme="theme" />

      <!-- 中：肖像 + 属性 -->
      <div class="sheet-center">
        <PortraitPanel
          :image="imageUrl"
          :name="name"
          :race="race"
          :theme="theme"
        />

        <AttributePanel
          :stats="statFields"
          :theme="theme"
          :max-value="25"
        />
      </div>

      <!-- 右：信息面板 -->
      <InfoPanel
        :description="description"
        :skills="skills"
        :race="race"
        :nature="nature"
        :theme="theme"
      />
    </div>

    <!-- 底部状态栏 -->
    <StatusBar
      :hp="props.cardData['hp'] ?? ''"
      :ac="props.cardData['ac'] ?? ''"
      :level="props.cardData['age'] ?? props.cardData['level'] ?? ''"
      :theme="theme"
    />
  </div>
</template>

<style scoped>
.character-sheet {
  position: relative;
  width: 900px;
  height: 700px;
  overflow: hidden;
  font-family: 'Times New Roman', 'Microsoft YaHei', serif;
  color: #d8c49a;
  background:
    linear-gradient(rgba(20, 15, 10, 0.75), rgba(20, 15, 10, 0.85)),
    linear-gradient(135deg, var(--card-gradient-start, #3a2a1b), var(--card-gradient-end, #1a1008));
  box-shadow: inset 0 0 40px #000, 0 0 30px #000;
}

/* 标题行 */
.sheet-title {
  position: absolute;
  left: 110px;
  right: 25px;
  top: 12px;
  height: 40px;
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 6px;
  border-bottom: 2px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  z-index: 5;
}

.sheet-title__class {
  font-size: 14px;
  padding: 2px 16px;
  border-radius: 30px;
  color: white;
  letter-spacing: 2px;
}

/* 三列内容区 */
.sheet-content {
  position: absolute;
  left: 110px;
  right: 25px;
  top: 58px;
  bottom: 80px;
  display: grid;
  grid-template-columns: 220px 1fr 1fr;
  gap: 10px;
  z-index: 5;
}

/* 中间列（肖像 + 属性） */
.sheet-center {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
