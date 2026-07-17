<script setup lang="ts">
/**
 * CardContainer - 卡片容器组件
 *
 * 基于 pokemon.css 的 futuristic HUD 风格：
 * - 1280×720 横版宽屏布局
 * - 背景覆盖 + 渐变遮罩
 * - 相对定位容器，子组件使用绝对定位排版
 */
import type { ResolvedTheme } from '@/utils/themeResolver'

defineProps<{
  theme: ResolvedTheme
}>()
</script>

<template>
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
    <slot />
  </div>
</template>

<style scoped>
.game-card {
  position: relative;
  width: 1280px;
  height: 720px;
  overflow: hidden;
  font-family: 'Arial Rounded MT Bold', 'Microsoft YaHei', sans-serif;
  color: white;
  background:
    linear-gradient(
      rgba(20, 80, 90, 0.25),
      rgba(10, 40, 50, 0.35)
    ),
    var(--card-gradient-start);
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

/* 径向辉光叠加层 */
.game-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(
      circle at 50% 50%,
      rgba(255, 255, 255, 0.35),
      transparent 45%
    );
  pointer-events: none;
  z-index: 1;
}
</style>
