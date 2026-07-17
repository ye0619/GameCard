<script setup lang="ts">
/**
 * CardSkills - 卡片技能列表组件
 *
 * 基于 pokemon.css 的左侧技能面板样式：
 * - 固定在卡片左侧，半透明毛玻璃背景
 * - 每项技能一行：类型圆点 | 技能名称 | PP 值
 * - 金色底部边框分割
 */
import { computed } from 'vue'
import type { ResolvedTheme } from '@/utils/themeResolver'
import type { Template, PresetSkill } from '@/types'
import { parseSkills, getAssetUrl } from '@/utils/themeResolver'

const props = defineProps<{
  theme: ResolvedTheme
  skills: string | undefined
  template: Template | null
}>()

/** 解析后的技能名称列表 */
const skillList = computed(() => parseSkills(props.skills))

/** 为每个技能匹配预设数据（用于获取图标和威力） */
function findPreset(name: string): PresetSkill | undefined {
  return props.template?.presetSkills?.find(
    p => p.name === name,
  )
}

/** 生成稳定但多样化的颜色（基于技能名 hash） */
function skillColor(name: string): string {
  const colors = [
    '#f5d928', '#36e6a0', '#ff6b6b', '#74b9ff',
    '#a29bfe', '#fd79a8', '#00cec9', '#e17055',
    '#00b894', '#6c5ce7', '#fdcb6e', '#e84393',
  ]
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return colors[Math.abs(hash) % colors.length]
}
</script>

<template>
  <div
    v-if="skillList.length > 0"
    class="skill-panel"
    :style="{ borderRightColor: theme.color + '44' }"
  >
    <!-- 面板标题 -->
    <div class="skill-panel__header">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M13 10V3L4 14h7v7l9-11h-7z" />
      </svg>
      <span>技能</span>
    </div>

    <div class="skill-panel__list">
      <div
        v-for="(skill, idx) in skillList"
        :key="idx"
        class="skill-item"
        :style="{ borderBottomColor: theme.color + '66' }"
      >
        <!-- 技能类型圆点图标 -->
        <div
          class="skill-type"
          :style="{ backgroundColor: skillColor(skill) }"
        >
          {{ skill.charAt(0) }}
        </div>

        <!-- 技能名称 -->
        <span class="skill-name">{{ skill }}</span>

        <!-- PP 值（优先使用 preset 中的 power，否则用序号） -->
        <span class="skill-pp" :style="{ color: theme.color }">
          {{ findPreset(skill)?.power ?? (idx + 1) * 25 + 10 }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skill-panel {
  position: absolute;
  left: 0;
  top: 0;
  width: 340px;
  height: 100%;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-right: 2px solid;
  z-index: 10;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.skill-panel__header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 14px 22px 8px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.3);
  opacity: 0.7;
}

.skill-panel__list {
  flex: 1;
  overflow-y: auto;
}

.skill-item {
  display: flex;
  align-items: center;
  height: 52px;
  padding: 0 22px;
  border-bottom: 2px solid;
  font-size: 20px;
  letter-spacing: 1px;
  text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.3);
  gap: 12px;
}

.skill-type {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.4);
}

.skill-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-pp {
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}
</style>
