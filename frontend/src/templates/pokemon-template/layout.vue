<script setup lang="ts">
/**
 * Pokemon Template Layout
 *
 * 迁移自原 CardRenderer.vue 的逻辑，整理为模板专用布局。
 * 使用通用卡片积木来实现经典宝可梦风格 HUD 布局。
 *
 * 布局：
 * ┌──────────────────────────────────────────────────────────────┐
 * │  ┌──────────────┐              ┌──────┐ ┌──────┐           │
 * │  │  Skill Panel  │              │  CP  │ │  LV  │           │
 * │  │  (left 340px) │              └──────┘ └──────┘           │
 * │  │               │              ┌────────────────────┐      │
 * │  │  skill 1      │  ┌──────┐   │  Radar Chart       │      │
 * │  │  skill 2      │  │ Img  │   │  420×420           │      │
 * │  │  skill 3      │  └──────┘   └────────────────────┘      │
 * │  │  ...          │                                          │
 * │  └──────────────┘              ┌────────────────────────┐  │
 * │                                │  Identity Panel        │  │
 * │                                │  Name / LV / Nature    │  │
 * │                                └────────────────────────┘  │
 * └──────────────────────────────────────────────────────────────┘
 */
import { computed } from 'vue'
import type { TemplateLayoutProps } from '@/renderer/types'
import type { TemplateField } from '@/types'
import CardBackground from '@/components/card/CardBackground.vue'
import CardHeader, { type BadgeItem } from '@/components/card/CardHeader.vue'
import CardImage from '@/components/card/CardImage.vue'
import CardStats from '@/components/card/CardStats.vue'
import CardSkills from '@/components/card/CardSkills.vue'
import CardDescription from '@/components/card/CardDescription.vue'
import { getAssetUrl } from '@/utils/themeResolver'

const props = defineProps<TemplateLayoutProps>()

// ==================== 名称与性格 ====================

const name = computed(() => props.cardData['name'] ?? '')
const nature = computed(() => props.cardData['nature'] ?? '')

// ==================== 右上角徽章 ====================

/** Pokemon 模板只将 cp 和 age 字段显示为徽章 */
const headerBadges = computed((): BadgeItem[] => {
  const badges: BadgeItem[] = []
  const cp = String(props.cardData['cp'] ?? '')
  if (cp.trim()) {
    badges.push({ label: 'CP', value: cp, variant: 'cp' })
  }
  const age = String(props.cardData['age'] ?? '')
  if (age.trim()) {
    badges.push({ label: 'LV', value: age })
  }
  return badges
})

// ==================== 标签图标 ====================

const tagIconUrl = computed(() => {
  if (props.theme.icon && props.template) {
    return getAssetUrl(props.template, props.theme.icon, 'icons')
  }
  return ''
})

// ==================== 属性统计 ====================

const statFields = computed(() => {
  const keys = props.template?.statFields ?? []
  return keys.map(key => ({
    key,
    label: props.template?.fields?.find(f => f.key === key)?.label ?? key,
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

// ==================== 等级（identity panel 显示） ====================

const levelValue = computed(() => {
  const age = props.cardData['age']
  return age ? parseInt(String(age), 10) || 1 : 1
})

// ==================== 图片编辑配置 ====================

/** 从 imageConfig 计算图片形状/裁剪样式 */
const imageShapeStyle = computed(() => {
  const s: Record<string, string> = {}
  const cfg = props.imageConfig
  if (!cfg?.applied) return s
  s['overflow'] = 'hidden'
  if (cfg.crop) {
    const t = cfg.crop.y
    const r = 1 - cfg.crop.x - cfg.crop.width
    const b = 1 - cfg.crop.y - cfg.crop.height
    const l = cfg.crop.x
    s['clipPath'] = `inset(${t * 100}% ${r * 100}% ${b * 100}% ${l * 100}%)`
    return s
  }
  if (cfg.shape === 'circle') s['clipPath'] = 'circle(50%)'
  else if (cfg.shape === 'rounded') s['borderRadius'] = '12px'
  else s['borderRadius'] = '0'
  return s
})

/** 从 imageConfig 计算图片缩放/位移变换 */
const imageTransform = computed((): Record<string, string> | undefined => {
  const cfg = props.imageConfig
  if (!cfg?.applied) return undefined
  if (cfg.scale === 1 && cfg.position.x === 0 && cfg.position.y === 0) return undefined
  return { transform: `translate(${cfg.position.x}px, ${cfg.position.y}px) scale(${cfg.scale})` }
})

// ==================== 技能与描述 ====================

const skills = computed(() => props.cardData['skills'] ?? '')
const description = computed(() => props.cardData['description'] ?? '')
const templateName = computed(() => props.template?.name ?? '')
</script>

<template>
  <!--
    注入 CSS 变量供背景渐变和辉光使用（来自 resolveTheme 的结果）
  -->
  <div
    class="game-card"
    :style="{
      '--card-primary': theme.color,
      '--card-secondary': theme.secondaryColor,
      '--card-gradient-start': theme.cssVars['--card-gradient-start'],
      '--card-gradient-end': theme.cssVars['--card-gradient-end'],
      '--card-glow': theme.cssVars['--card-glow'],
      borderColor: theme.color + '44',
    }"
  >
    <!-- 背景层 -->
    <CardBackground :theme="theme" :template="template" />

    <!-- 右上角 CP / LV 徽章 -->
    <CardHeader
      :badges="headerBadges"
      :theme="theme"
    />

    <!-- 左侧技能面板 -->
    <CardSkills
      :theme="theme"
      :skills="skills"
      :template="template"
    />

    <!-- 中间角色图片（应用裁剪/缩放/位移） -->
    <CardImage
      :image="imageUrl"
      :name="name"
      :img-style="imageTransform ? { ...imageShapeStyle, ...imageTransform } : imageShapeStyle"
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

        <span
          v-if="nature"
          class="identity-panel__tag"
          :style="{ backgroundColor: theme.color }"
        >
          <img
            v-if="tagIconUrl"
            :src="tagIconUrl"
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
  </div>
</template>

<style scoped>
/* =============================================
   Pokemon 模板专属样式
   ============================================= */

.game-card {
  position: relative;
  width: 1280px;
  height: 720px;
  overflow: hidden;
  font-family: 'Arial Rounded MT Bold', 'Microsoft YaHei', sans-serif;
  color: white;
  background:
    linear-gradient(rgba(20, 80, 90, 0.25), rgba(10, 40, 50, 0.35)),
    var(--card-gradient-start);
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.game-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.35), transparent 45%);
  pointer-events: none;
  z-index: 1;
}

/* 雷达图区域 */
.radar-panel {
  position: absolute;
  right: 75px;
  top: 55px;
  width: 380px;
  height: 380px;
  z-index: 15;
}

/* 身份面板 */
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
