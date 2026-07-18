<script setup lang="ts">
/**
 * PortraitPanel — 角色肖像面板
 *
 * 基于 Baldur'sGate2.css 的 .portrait-panel 和 .portrait 样式：
 * - 带边框的角色肖像
 * - 下方显示种族
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  image: string | null
  name: string
  race: string
  theme: ResolvedTheme
}>()
</script>

<template>
  <div class="portrait-panel">
    <div class="portrait-frame">
      <img
        v-if="image"
        :src="image"
        :alt="name"
        class="portrait"
      />
      <div v-else class="portrait portrait--empty">
        <span>无肖像</span>
      </div>
    </div>
    <div
      v-if="race"
      class="race-tag"
      :style="{ color: theme.color }"
    >
      {{ race }}
    </div>
  </div>
</template>

<style scoped>
.portrait-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: rgba(30, 20, 10, 0.8);
  border: 2px solid #83613a;
  padding: 8px;
}

.portrait-frame {
  width: 180px;
  height: 200px;
  overflow: hidden;
  border: 4px solid #7c623e;
  display: flex;
  align-items: center;
  justify-content: center;
}

.portrait {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.portrait--empty {
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.3);
}

.race-tag {
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.8);
}
</style>
