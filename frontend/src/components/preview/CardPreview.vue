<script setup lang="ts">
import { computed } from 'vue'
import { useCardStore } from '@/stores/card'

const store = useCardStore()

/** 属性 → 颜色映射 */
const typeColors: Record<string, string> = {
  '火': '#EF4444',
  '水': '#3B82F6',
  '草': '#22C55E',
  '电': '#EAB308',
  '超能力': '#A855F7',
  '格斗': '#F97316',
  '岩石': '#A16207',
  '地面': '#D97706',
  '飞行': '#60A5FA',
  '虫': '#84CC16',
  '毒': '#D946EF',
  '一般': '#9CA3AF',
  '幽灵': '#8B5CF6',
  '冰': '#06B6D4',
  '龙': '#6366F1',
  '恶': '#78350F',
  '钢': '#6B7280',
  '妖精': '#F472B6',
}

/** 稀有度 → 颜色映射 */
const rarityColors: Record<string, string> = {
  '普通': '#9CA3AF',
  '稀有': '#3B82F6',
  '史诗': '#A855F7',
  '传说': '#F59E0B',
}

const element = computed(() => store.cardData['element'] ?? '')
const elementColor = computed(() => typeColors[element.value] ?? '#6366F1')
const rarity = computed(() => store.cardData['rarity'] ?? '')
const rarityColor = computed(() => rarityColors[rarity.value] ?? '#9CA3AF')

const name = computed(() => store.cardData['name'] ?? '')
const skill = computed(() => store.cardData['skill'] ?? '')
const description = computed(() => store.cardData['description'] ?? '')
const hasImage = computed(() => !!store.uploadedImage)
const hasData = computed(() => name.value || skill.value || description.value || hasImage.value)
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
      <p class="text-sm mt-1">上传图片并填写信息，右侧将实时预览</p>
    </div>

    <!-- 卡片预览 -->
    <div v-else class="relative" style="width: 380px;">
      <!-- 卡片容器 -->
      <div
        class="relative rounded-2xl overflow-hidden shadow-2xl border-2 transition-all duration-300"
        :style="{ borderColor: elementColor }"
      >
        <!-- 顶部装饰条 -->
        <div class="h-2" :style="{ background: `linear-gradient(90deg, ${elementColor}, ${elementColor}88)` }"></div>

        <!-- 内卡片 -->
        <div class="bg-gradient-to-b from-gray-900 to-gray-950 p-5">
          <!-- 图片区域 -->
          <div
            class="relative rounded-xl overflow-hidden mb-4 bg-gray-800"
            style="aspect-ratio: 16 / 10;"
          >
            <img
              v-if="store.uploadedImage"
              :src="store.uploadedImage"
              alt="卡片图片"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="flex items-center justify-center h-full text-gray-600 text-sm"
            >
              暂无图片
            </div>

            <!-- 类型标签 -->
            <div
              v-if="element"
              class="absolute top-2 left-2 px-2.5 py-1 rounded-full text-xs font-bold text-white shadow-lg"
              :style="{ backgroundColor: elementColor }"
            >
              {{ element }}
            </div>

            <!-- 稀有度标签 -->
            <div
              v-if="rarity"
              class="absolute top-2 right-2 px-2.5 py-1 rounded-full text-xs font-bold text-white shadow-lg"
              :style="{ backgroundColor: rarityColor, opacity: 0.9 }"
            >
              ★ {{ rarity }}
            </div>
          </div>

          <!-- 名称 -->
          <h2
            v-if="name"
            class="text-xl font-bold text-white mb-1"
            :style="{ color: elementColor }"
          >
            {{ name }}
          </h2>

          <!-- 属性线 -->
          <div v-if="element || rarity" class="flex items-center gap-2 text-xs text-gray-400 mb-3">
            <span v-if="element" :style="{ color: elementColor }">● {{ element }}</span>
            <span v-if="element && rarity" class="text-gray-600">|</span>
            <span v-if="rarity" :style="{ color: rarityColor }">{{ rarity }}</span>
          </div>

          <!-- 分割线 -->
          <div class="h-px bg-gradient-to-r from-transparent via-gray-600 to-transparent my-3"></div>

          <!-- 技能 -->
          <div v-if="skill" class="mb-3">
            <div class="flex items-start gap-2">
              <div
                class="mt-0.5 w-5 h-5 rounded flex items-center justify-center text-xs font-bold text-white shrink-0"
                :style="{ backgroundColor: elementColor }"
              >
                ⚡
              </div>
              <div>
                <span class="text-xs text-gray-400">技能</span>
                <p class="text-sm text-gray-200">{{ skill }}</p>
              </div>
            </div>
          </div>

          <!-- 描述 -->
          <div v-if="description" class="mt-2">
            <p class="text-xs leading-relaxed text-gray-400 italic border-l-2 border-gray-700 pl-3">
              {{ description }}
            </p>
          </div>

          <!-- 底部署名 -->
          <div class="mt-4 pt-3 border-t border-gray-800 flex justify-between text-xs text-gray-600">
            <span>GameCard</span>
            <span>{{ store.selectedTemplate?.name ?? '' }}</span>
          </div>
        </div>
      </div>

      <!-- 尺寸提示 -->
      <p class="text-center text-xs text-gray-600 mt-3">380 × 520 · 实时预览</p>
    </div>
  </div>
</template>
