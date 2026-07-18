<script setup lang="ts">
/**
 * CardHeader - 通用卡片头部组件（可选的通用积木）
 *
 * 两个区域：
 * 1. 右上角徽章区（badges）— 如 CP、LV、HP、AC
 * 2. 可选左上标题 + 属性标签（title/tag）
 *
 * 每个模板可根据需要选择是否使用此组件，
 * 或自行实现头部。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

export interface BadgeItem {
  label: string
  /** 徽章显示值 */
  value: string | number
  /** 可选：单独指定颜色（默认使用 theme.color） */
  color?: string
  /** 视觉变体：default | pill | cp */
  variant?: 'default' | 'pill' | 'cp'
}

defineProps<{
  title?: string
  titleIcon?: string
  /** 属性标签文字（如"暴躁""战士"） */
  tag?: string
  /** 标签背景色 */
  tagColor?: string
  /** 标签图标 URL */
  tagIcon?: string
  theme: ResolvedTheme
  /** 右上角徽章列表 */
  badges?: BadgeItem[]
}>()
</script>

<template>
  <!-- 左上角标题区 -->
  <div v-if="title || tag" class="title-area">
    <div v-if="title" class="title-area__name" :style="{ color: theme.color }">
      {{ title }}
    </div>
    <div v-if="tag" class="title-area__tag" :style="{ backgroundColor: tagColor || theme.color }">
      <img
        v-if="tagIcon"
        :src="tagIcon"
        class="title-area__tag-icon"
        alt=""
        @error="(e: Event) => (e.target as HTMLElement).style.display = 'none'"
      />
      {{ tag }}
    </div>
  </div>

  <!-- 右上角徽章面板 -->
  <div v-if="badges && badges.length > 0" class="top-info">
    <div
      v-for="badge in badges"
      :key="badge.label"
      class="info-box"
      :class="{
        'is-cp': badge.variant === 'cp',
        'is-pill': badge.variant === 'pill',
      }"
    >
      <div class="info-box__label">{{ badge.label }}</div>
      <div class="info-box__value" :style="{ color: badge.color || theme.color }">
        {{ badge.value }}
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 左上角标题 */
.title-area {
  position: absolute;
  left: 370px;
  top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 20;
}

.title-area__name {
  font-size: 28px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.title-area__tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 14px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: bold;
  color: white;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

.title-area__tag-icon {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

/* 右上角徽章 */
.top-info {
  position: absolute;
  right: 32px;
  top: 18px;
  display: flex;
  gap: 24px;
  z-index: 20;
}

.info-box {
  text-align: center;
}

.info-box__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.5);
  margin-bottom: 2px;
}

.info-box__value {
  font-size: 30px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.info-box.is-cp .info-box__value {
  background: rgba(255, 255, 255, 0.7);
  color: #555 !important;
  padding: 2px 16px;
  border-radius: 30px;
  font-size: 28px;
}

.info-box.is-pill .info-box__value {
  background: rgba(255, 255, 255, 0.12);
  padding: 2px 14px;
  border-radius: 30px;
  font-size: 24px;
}
</style>

<style scoped>
.top-info {
  position: absolute;
  right: 32px;
  top: 18px;
  display: flex;
  gap: 24px;
  z-index: 20;
}

.info-box {
  text-align: center;
}

.info-box__label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.5);
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.5);
  margin-bottom: 2px;
}

.info-box__value {
  font-size: 30px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.info-box.is-cp .info-box__value {
  background: rgba(255, 255, 255, 0.7);
  color: #555 !important;
  padding: 2px 16px;
  border-radius: 30px;
  font-size: 28px;
}
</style>
