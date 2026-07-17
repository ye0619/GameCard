<script setup lang="ts">
/**
 * CardEditor — property panel for card data.
 *
 * Renders form fields from template.fields definitions.
 * Supports TEXT / TEXTAREA / NUMBER / SELECT / ENUM / ARRAY / COLOR / IMAGE.
 * Compact styling for use as a right-side property panel.
 */
import { computed, ref, watch } from 'vue'
import { useCardStore } from '@/stores/card'
import type { TemplateField } from '@/types'
import EmptyState from '@/components/common/EmptyState.vue'
import PresetSkillSelector from './PresetSkillSelector.vue'
import PresetIntroSelector from './PresetIntroSelector.vue'

const store = useCardStore()

const fields = computed(() => store.selectedTemplate?.fields ?? [])
const selectedTemplate = computed(() => store.selectedTemplate)

function parseOptions(raw: string | null): string[] {
  if (!raw) return []
  try {
    return JSON.parse(raw)
  } catch {
    return []
  }
}

function isNumericField(field: TemplateField): boolean {
  return field.type === 'NUMBER'
}

// ==================== ARRAY support ====================

const arrayEntries = ref<Record<string, string[]>>({})

function getArray(field: TemplateField): string[] {
  const key = field.key
  if (!arrayEntries.value[key]) {
    arrayEntries.value[key] = []
  }
  return arrayEntries.value[key]
}

function addArrayItem(field: TemplateField): void {
  const key = field.key
  if (!arrayEntries.value[key]) {
    arrayEntries.value[key] = []
  }
  arrayEntries.value[key].push('')
  syncArrayToCard(field)
}

function removeArrayItem(field: TemplateField, index: number): void {
  const key = field.key
  if (!arrayEntries.value[key]) return
  arrayEntries.value[key].splice(index, 1)
  syncArrayToCard(field)
}

function syncArrayToCard(field: TemplateField): void {
  const key = field.key
  const items = arrayEntries.value[key] ?? []
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

watch(() => store.selectedTemplateId, () => {
  arrayEntries.value = {}
})

// Sync cardData → arrayEntries for ARRAY fields
// Handles preset skill additions that bypass addArrayItem
watch(
  () => {
    const arrFields = fields.value.filter(f => f.type === 'ARRAY')
    return arrFields.map(f => ({ key: f.key, val: store.cardData[f.key] }))
  },
  (entries) => {
    for (const { key, val } of entries) {
      if (!val || typeof val !== 'string') continue
      try {
        const parsed = JSON.parse(val)
        if (Array.isArray(parsed)) {
          const names = parsed.map((item: unknown) =>
            typeof item === 'string' ? item : String((item as Record<string, unknown>).name ?? ''),
          ).filter(Boolean)
          const current = arrayEntries.value[key] ?? []
          const changed = names.length !== current.length ||
            names.some((n, i) => n !== current[i])
          if (changed) {
            arrayEntries.value[key] = names
          }
        }
      } catch { /* ignore parse errors from intermediate states */ }
    }
  },
  { deep: true },
)
</script>

<template>
  <div class="editor-form">
    <!-- No template -->
    <EmptyState
      v-if="fields.length === 0"
      title="请先选择模板"
      description="选择模板后将显示可编辑的属性"
    />

    <!-- Dynamic fields -->
    <div v-else class="editor-form__fields">
      <div
        v-for="field in fields"
        :key="field.key"
        class="editor-form__group"
      >
        <label class="editor-form__label">
          {{ field.label }}
          <span v-if="field.required" class="editor-form__required">*</span>
        </label>

        <!-- TEXT -->
        <input
          v-if="field.type === 'TEXT'"
          v-model="store.cardData[field.key]"
          type="text"
          :placeholder="field.placeholder ?? ''"
          class="editor-form__input"
        />

        <!-- TEXTAREA -->
        <textarea
          v-else-if="field.type === 'TEXTAREA'"
          v-model="store.cardData[field.key]"
          :placeholder="field.placeholder ?? ''"
          rows="2"
          class="editor-form__input editor-form__textarea"
        />
        <!-- Preset introduction selector (for description fields) -->
        <PresetIntroSelector
          v-if="field.type === 'TEXTAREA' && field.key === 'description' && selectedTemplate?.presetIntroductions && selectedTemplate.presetIntroductions.length > 0"
          :field-key="field.key"
          :presets="selectedTemplate.presetIntroductions"
        />

        <!-- NUMBER -->
        <input
          v-else-if="isNumericField(field)"
          v-model.number="store.cardData[field.key]"
          type="number"
          :placeholder="field.placeholder ?? ''"
          class="editor-form__input"
        />

        <!-- SELECT -->
        <select
          v-else-if="field.type === 'SELECT'"
          v-model="store.cardData[field.key]"
          class="editor-form__input editor-form__select"
        >
          <option value="" disabled>{{ field.placeholder ?? '请选择' }}</option>
          <option
            v-for="opt in parseOptions(field.options)"
            :key="opt"
            :value="opt"
          >{{ opt }}</option>
        </select>

        <!-- ENUM -->
        <div
          v-else-if="field.type === 'ENUM'"
          class="editor-form__enum"
        >
          <button
            v-for="opt in parseOptions(field.options)"
            :key="opt"
            class="editor-form__enum-tag"
            :class="{ 'is-selected': store.cardData[field.key] === opt }"
            @click="store.cardData[field.key] = opt"
          >{{ opt }}</button>
        </div>

        <!-- ARRAY -->
        <div v-else-if="field.type === 'ARRAY'" class="editor-form__array">
          <!-- Preset skill selector (only for skills-type arrays) -->
          <PresetSkillSelector
            v-if="selectedTemplate?.presetSkills && selectedTemplate.presetSkills.length > 0"
            :field-key="field.key"
            :presets="selectedTemplate.presetSkills"
          />

          <div
            v-for="(item, idx) in getArray(field)"
            :key="idx"
            class="editor-form__array-row"
          >
            <input
              v-model="arrayEntries[field.key][idx]"
              type="text"
              :placeholder="field.fields?.[0]?.placeholder ?? ''"
              class="editor-form__input editor-form__array-input"
              @input="syncArrayToCard(field)"
            />
            <button
              class="editor-form__array-remove"
              title="删除"
              @click="removeArrayItem(field, idx)"
            >
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <button
            class="editor-form__array-add"
            @click="addArrayItem(field)"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            <span>添加</span>
          </button>
        </div>

        <!-- COLOR -->
        <div v-else-if="field.type === 'COLOR'" class="editor-form__color">
          <input
            v-model="store.cardData[field.key]"
            type="color"
            class="editor-form__color-picker"
          />
          <span class="editor-form__color-value">{{ store.cardData[field.key] }}</span>
        </div>

        <!-- IMAGE hint -->
        <p v-else-if="field.type === 'IMAGE'" class="editor-form__image-hint">
          请使用左侧上传区域
        </p>

        <!-- Required hint -->
        <p
          v-if="field.required && (!store.cardData[field.key] || store.cardData[field.key] === '')"
          class="editor-form__error"
        >此字段为必填项</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Property-panel form — compact, dense */
.editor-form__fields {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.editor-form__group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.editor-form__label {
  font-family: var(--gc-font-sans);
  font-size: 12px;
  font-weight: 500;
  color: var(--gc-ink);
  opacity: 0.6;
}

.editor-form__required {
  color: var(--gc-accent-magenta);
  margin-left: 2px;
}

/* Inputs — compact, surface-soft background, no border until focus */
.editor-form__input {
  width: 100%;
  padding: 6px 10px;
  font-family: var(--gc-font-sans);
  font-size: 13px;
  font-weight: 400;
  color: var(--gc-ink);
  background-color: var(--gc-surface-soft);
  border: 1px solid transparent;
  border-radius: var(--gc-radius-sm);
  outline: none;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.editor-form__input::placeholder {
  color: var(--gc-ink);
  opacity: 0.2;
}

.editor-form__input:focus {
  border-color: var(--gc-hairline);
  background-color: var(--gc-canvas);
}

/* Textarea */
.editor-form__textarea {
  resize: vertical;
  min-height: 48px;
}

/* Select */
.editor-form__select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%23000' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  padding-right: 28px;
}

/* ENUM — compact pills */
.editor-form__enum {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.editor-form__enum-tag {
  padding: 3px 10px;
  font-family: var(--gc-font-sans);
  font-size: 12px;
  font-weight: 500;
  border-radius: var(--gc-radius-pill);
  background-color: var(--gc-surface-soft);
  color: var(--gc-ink);
  transition: background-color 0.15s ease, color 0.15s ease;
  min-height: 26px;
}

.editor-form__enum-tag:hover {
  opacity: 0.75;
}

.editor-form__enum-tag.is-selected {
  background-color: var(--gc-primary);
  color: var(--gc-on-primary);
}

/* ARRAY */
.editor-form__array {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.editor-form__array-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.editor-form__array-input {
  flex: 1;
}

.editor-form__array-remove {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--gc-radius-full);
  color: var(--gc-ink);
  opacity: 0.2;
  transition: opacity 0.15s ease, color 0.15s ease;
  flex-shrink: 0;
}

.editor-form__array-remove:hover {
  opacity: 0.6;
  color: var(--gc-accent-magenta);
}

.editor-form__array-add {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--gc-ink);
  opacity: 0.4;
  padding: 2px 0;
  transition: opacity 0.15s ease;
}

.editor-form__array-add:hover {
  opacity: 0.8;
}

/* COLOR */
.editor-form__color {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-form__color-picker {
  width: 28px;
  height: 28px;
  padding: 2px;
  border: 1px solid var(--gc-hairline);
  border-radius: var(--gc-radius-sm);
  background-color: var(--gc-canvas);
  cursor: pointer;
}

.editor-form__color-value {
  font-family: var(--gc-font-mono);
  font-size: 10px;
  color: var(--gc-ink);
  opacity: 0.35;
}

/* IMAGE hint */
.editor-form__image-hint {
  font-size: 12px;
  color: var(--gc-ink);
  opacity: 0.35;
}

/* Error */
.editor-form__error {
  font-size: 11px;
  color: var(--gc-accent-magenta);
  margin-top: 1px;
}
</style>
