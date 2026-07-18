<script setup lang="ts">
/**
 * InfoPanel — 角色信息/能力面板
 *
 * 基于 Baldur'sGate2.css 的 .info-panel 和 .ability 样式：
 * - 显示背景故事
 * - 显示能力/法术列表
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  description: string
  skills: string[]
  race: string
  nature: string
  theme: ResolvedTheme
}>()
</script>

<template>
  <div class="info-panel" :style="{ borderColor: theme.color + '66' }">
    <!-- 角色信息 -->
    <div class="info-section">
      <div class="info-title" :style="{ color: theme.color, borderBottomColor: theme.color + '66' }">
        角色信息
      </div>
      <div class="info-line">
        <span class="info-label">职业</span>
        <span class="info-value" :style="{ color: theme.color }">{{ nature || '—' }}</span>
      </div>
      <div class="info-line">
        <span class="info-label">种族</span>
        <span class="info-value">{{ race || '—' }}</span>
      </div>
    </div>

    <!-- 背景故事 -->
    <div v-if="description" class="info-section">
      <div class="info-title" :style="{ color: theme.color, borderBottomColor: theme.color + '66' }">
        背景
      </div>
      <p class="info-desc">{{ description }}</p>
    </div>

    <!-- 能力/法术列表 -->
    <div v-if="skills.length > 0" class="info-section">
      <div class="info-title" :style="{ color: theme.color, borderBottomColor: theme.color + '66' }">
        能力 / 法术
      </div>
      <div class="ability-list">
        <div
          v-for="(skill, idx) in skills"
          :key="idx"
          class="ability-item"
          :style="{ color: theme.color }"
        >
          <span class="ability-bullet" :style="{ color: theme.color }">◆</span>
          {{ skill }}
        </div>
        <div v-if="skills.length === 0" class="ability-empty">
          无
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.info-panel {
  background: rgba(20, 15, 10, 0.8);
  border: 2px solid;
  padding: 10px 14px;
  font-size: 15px;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.info-title {
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid;
  padding-bottom: 2px;
  letter-spacing: 2px;
}

.info-line {
  display: flex;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  opacity: 0.6;
  width: 36px;
  flex-shrink: 0;
}

.info-value {
  font-weight: bold;
}

.info-desc {
  font-size: 12px;
  line-height: 1.5;
  opacity: 0.75;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.ability-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ability-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.85;
}

.ability-bullet {
  font-size: 10px;
  flex-shrink: 0;
}

.ability-empty {
  font-size: 13px;
  opacity: 0.4;
  font-style: italic;
}
</style>
