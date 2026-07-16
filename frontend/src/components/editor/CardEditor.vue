<script setup lang="ts">
import { computed } from 'vue'
import { useCardStore } from '@/stores/card'
import type { TemplateField } from '@/types'

const store = useCardStore()

const fields = computed(() => store.selectedTemplate?.fields ?? [])

/** 解析 SELECT 选项 */
function parseOptions(raw: string | null): string[] {
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch {
    return []
  }
}
</script>

<template>
  <div class="space-y-4">
    <label class="text-sm font-medium text-gray-300">卡片信息</label>

    <!-- 无模板 -->
    <div v-if="fields.length === 0" class="text-sm text-gray-500 py-4">
      请先选择一个模板
    </div>

    <!-- 动态表单 -->
    <div v-else class="space-y-3">
      <div v-for="field in fields" :key="field.key" class="space-y-1">
        <!-- 标签 -->
        <label class="block text-xs text-gray-400">
          {{ field.label }}
          <span v-if="field.required" class="text-red-400 ml-0.5">*</span>
        </label>

        <!-- TEXT -->
        <input
          v-if="field.type === 'TEXT'"
          v-model="store.cardData[field.key]"
          type="text"
          :placeholder="field.placeholder ?? ''"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition"
        />

        <!-- TEXTAREA -->
        <textarea
          v-else-if="field.type === 'TEXTAREA'"
          v-model="store.cardData[field.key]"
          :placeholder="field.placeholder ?? ''"
          rows="3"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition resize-none"
        ></textarea>

        <!-- NUMBER -->
        <input
          v-else-if="field.type === 'NUMBER'"
          v-model.number="store.cardData[field.key]"
          type="number"
          :placeholder="field.placeholder ?? ''"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition"
        />

        <!-- SELECT -->
        <select
          v-else-if="field.type === 'SELECT'"
          v-model="store.cardData[field.key]"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition appearance-none"
        >
          <option value="" disabled>{{ field.placeholder ?? '请选择' }}</option>
          <option
            v-for="opt in parseOptions(field.options)"
            :key="opt"
            :value="opt"
          >
            {{ opt }}
          </option>
        </select>

        <!-- COLOR -->
        <div v-else-if="field.type === 'COLOR'" class="flex items-center gap-2">
          <input
            v-model="store.cardData[field.key]"
            type="color"
            class="w-10 h-10 rounded cursor-pointer border border-gray-700 bg-gray-800"
          />
          <span class="text-xs text-gray-500">{{ store.cardData[field.key] }}</span>
        </div>

        <!-- IMAGE（由 ImageUploader 处理，此处只读显示） -->
        <div v-else-if="field.type === 'IMAGE'" class="text-xs text-gray-500">
          请使用上方的图片上传区域
        </div>

        <!-- 字段说明 -->
        <p v-if="field.required && !store.cardData[field.key]" class="text-xs text-amber-400">
          此字段为必填项
        </p>
      </div>
    </div>
  </div>
</template>
