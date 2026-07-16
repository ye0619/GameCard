<script setup lang="ts">
/**
 * CardEditor - 卡片数据编辑器
 *
 * 根据模板字段定义动态渲染编辑表单。
 * 支持 TEXT / TEXTAREA / NUMBER / SELECT / ENUM / ARRAY / COLOR / IMAGE 类型。
 * 所有字段由 template.fields 驱动，无硬编码字段。
 */
import { computed, ref, watch } from 'vue'
import { useCardStore } from '@/stores/card'
import type { TemplateField } from '@/types'

const store = useCardStore()

const fields = computed(() => store.selectedTemplate?.fields ?? [])

/** 解析 SELECT / ENUM 选项 */
function parseOptions(raw: string | null): string[] {
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch {
    return []
  }
}

/** 判断是否为数值字段 */
function isNumericField(field: TemplateField): boolean {
  return field.type === 'NUMBER'
}

// ==================== ARRAY 类型支持（如技能列表） ====================

/** 编辑中的数组条目 */
const arrayEntries = ref<Record<string, string[]>>({})

/** 获取某个数组字段的当前条目 */
function getArray(field: TemplateField): string[] {
  const key = field.key
  if (!arrayEntries.value[key]) {
    arrayEntries.value[key] = []
  }
  return arrayEntries.value[key]
}

/** 添加数组条目 */
function addArrayItem(field: TemplateField): void {
  const key = field.key
  if (!arrayEntries.value[key]) {
    arrayEntries.value[key] = []
  }
  // 推入空字符串
  arrayEntries.value[key].push('')
  syncArrayToCard(field)
}

/** 删除数组条目 */
function removeArrayItem(field: TemplateField, index: number): void {
  const key = field.key
  if (!arrayEntries.value[key]) return
  arrayEntries.value[key].splice(index, 1)
  syncArrayToCard(field)
}

/** 将数组同步为 cardData 中的 JSON 字符串 */
function syncArrayToCard(field: TemplateField): void {
  const key = field.key
  const items = arrayEntries.value[key] ?? []
  // 生成 ARRAY 子字段的 JSON
  const subField = field.fields?.[0]
  if (subField) {
    const json = items
      .filter(v => v.trim())
      .map(v => ({ [subField.key]: v.trim() }))
    store.cardData[key] = JSON.stringify(json)
  } else {
    store.cardData[key] = items.filter(v => v.trim()).join(',')
  }
}

/** 从 cardData 初始化数组条目 */
function initArrayFromCard(field: TemplateField): void {
  const key = field.key
  const raw = store.cardData[key]
  if (!raw || raw.trim() === '') {
    arrayEntries.value[key] = []
    return
  }

  // 尝试 JSON 数组
  if (raw.trim().startsWith('[')) {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) {
        const subField = field.fields?.[0]
        arrayEntries.value[key] = parsed.map((item: unknown) => {
          if (typeof item === 'string') return item
          if (subField && typeof item === 'object' && item !== null) {
            return String((item as Record<string, unknown>)[subField.key] ?? '')
          }
          return String(item)
        })
        return
      }
    } catch { /* fallthrough */ }
  }

  // 逗号分隔
  if (raw.includes(',')) {
    arrayEntries.value[key] = raw.split(',').map(s => s.trim()).filter(Boolean)
    return
  }

  // 单条
  arrayEntries.value[key] = [raw.trim()]
}

/** 监听模板切换，重置数组条目 */
watch(() => store.selectedTemplateId, () => {
  arrayEntries.value = {}
})
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

        <!-- ====== TEXT ====== -->
        <input
          v-if="field.type === 'TEXT'"
          v-model="store.cardData[field.key]"
          type="text"
          :placeholder="field.placeholder ?? ''"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition"
        />

        <!-- ====== TEXTAREA ====== -->
        <textarea
          v-else-if="field.type === 'TEXTAREA'"
          v-model="store.cardData[field.key]"
          :placeholder="field.placeholder ?? ''"
          rows="3"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition resize-none"
        />

        <!-- ====== NUMBER ====== -->
        <input
          v-else-if="isNumericField(field)"
          v-model.number="store.cardData[field.key]"
          type="number"
          :placeholder="field.placeholder ?? ''"
          class="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-100 placeholder-gray-500 focus:border-blue-500 focus:ring-1 focus:ring-blue-500 outline-none transition"
        />

        <!-- ====== SELECT ====== -->
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

        <!-- ====== ENUM（标签组选择器） ====== -->
        <div
          v-else-if="field.type === 'ENUM'"
          class="flex flex-wrap gap-1.5"
        >
          <button
            v-for="opt in parseOptions(field.options)"
            :key="opt"
            @click="store.cardData[field.key] = opt"
            :class="[
              'px-2.5 py-1 rounded-lg text-xs font-medium border transition-all',
              store.cardData[field.key] === opt
                ? 'text-white border-transparent shadow-sm'
                : 'text-gray-400 border-gray-700 bg-gray-800/50 hover:border-gray-500 hover:text-gray-300',
            ]"
            :style="store.cardData[field.key] === opt ? {
              backgroundColor: '#3B82F6',
              borderColor: '#3B82F6',
            } : {}"
          >
            {{ opt }}
          </button>
        </div>

        <!-- ====== ARRAY（动态增删列表） ====== -->
        <div v-else-if="field.type === 'ARRAY'" class="space-y-1.5">
          <!-- 现有条目 -->
          <div
            v-for="(item, idx) in getArray(field)"
            :key="idx"
            class="flex items-center gap-1.5"
          >
            <input
              v-model="arrayEntries[field.key][idx]"
              type="text"
              :placeholder="field.fields?.[0]?.placeholder ?? '输入' + field.label"
              class="flex-1 px-2.5 py-1.5 bg-gray-800 border border-gray-700 rounded-lg text-xs text-gray-100 placeholder-gray-500 focus:border-blue-500 outline-none transition"
              @input="syncArrayToCard(field)"
            />
            <button
              @click="removeArrayItem(field, idx)"
              class="w-6 h-6 flex items-center justify-center rounded text-gray-500 hover:text-red-400 hover:bg-red-500/10 transition-colors shrink-0"
              title="删除"
            >
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- 添加按钮 -->
          <button
            @click="addArrayItem(field)"
            class="flex items-center gap-1 text-xs text-blue-400 hover:text-blue-300 transition-colors py-1"
          >
            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            <span>添加{{ field.label }}</span>
          </button>
        </div>

        <!-- ====== COLOR ====== -->
        <div v-else-if="field.type === 'COLOR'" class="flex items-center gap-2">
          <input
            v-model="store.cardData[field.key]"
            type="color"
            class="w-10 h-10 rounded cursor-pointer border border-gray-700 bg-gray-800"
          />
          <span class="text-xs text-gray-500">{{ store.cardData[field.key] }}</span>
        </div>

        <!-- ====== IMAGE（由 ImageUploader 处理） ====== -->
        <div v-else-if="field.type === 'IMAGE'" class="text-xs text-gray-500">
          请使用上方的图片上传区域
        </div>

        <!-- 必填提示 -->
        <p v-if="field.required && (!store.cardData[field.key] || store.cardData[field.key] === '')" class="text-xs text-amber-400">
          此字段为必填项
        </p>
      </div>
    </div>
  </div>
</template>
