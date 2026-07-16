<script setup lang="ts">
/**
 * CardSkills - 卡片技能列表组件
 *
 * 展示角色技能列表，支持空状态。
 * 样式由主题驱动，颜色来自 theme.color。
 */
import type { ResolvedTheme } from '@/utils/themeResolver'
import { parseSkills } from '@/utils/themeResolver'

const props = defineProps<{
  theme: ResolvedTheme
  skills: string | undefined
}>()

/** 解析后的技能列表 */
const skillList = () => parseSkills(props.skills)
</script>

<template>
  <div v-if="skills" class="mb-3">
    <div class="flex items-center gap-1.5 mb-2">
      <span class="text-xs font-medium text-gray-400 tracking-wider">技能</span>
      <div class="h-px flex-1" :style="{ backgroundColor: theme.color + '33' }" />
    </div>

    <div class="flex flex-wrap gap-1.5">
      <div
        v-for="(skill, idx) in skillList()"
        :key="idx"
        class="flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium transition-colors"
        :style="{
          backgroundColor: theme.color + '15',
          color: theme.color,
          borderColor: theme.color + '30',
        }"
      >
        <!-- 技能图标圆点 -->
        <span
          class="w-2 h-2 rounded-full shrink-0"
          :style="{ backgroundColor: theme.color }"
        />
        {{ skill }}
      </div>
    </div>

    <!-- 空技能列表 -->
    <div
      v-if="skillList().length === 0"
      class="text-xs text-gray-500 italic"
    >
      暂无技能
    </div>
  </div>
</template>
