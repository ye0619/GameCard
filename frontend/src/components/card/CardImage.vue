<script setup lang="ts">
/**
 * CardImage - 卡片图片组件
 *
 * 基于 pokemon.css 的角色展示区：
 * - 居中底部展示
 * - drop-shadow 投影效果
 * - 320px 宽
 */
defineProps<{
  image: string | null
  name: string
  imgStyle?: Record<string, string>
}>()
</script>

<template>
  <!-- 有图片时 -->
  <div v-if="image" class="character-area">
    <img
      :src="image"
      :alt="name || '角色图片'"
      class="character-image"
      :style="imgStyle"
    />
  </div>

  <!-- 无图片时：空状态占位 -->
  <div v-else class="character-area character-area--empty">
    <div class="character-placeholder">
      <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
          d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z" />
      </svg>
      <span>暂无图片</span>
    </div>
  </div>
</template>

<style scoped>
.character-area {
  position: absolute;
  left: 50%;
  bottom: 90px;
  transform: translateX(-50%);
  z-index: 5;
}

.character-image {
  width: 280px;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 12px 20px rgba(0, 0, 0, 0.5));
}

.character-area--empty {
  bottom: 120px;
}

.character-placeholder {
  width: 240px;
  aspect-ratio: 3 / 4;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.25);
  font-size: 13px;
  backdrop-filter: blur(4px);
}
</style>
