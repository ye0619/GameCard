/**
 * Works Store — 保存的作品管理
 *
 * 提供作品的保存、删除、列表管理，所有数据持久化到 localStorage。
 * 设计原则：
 * - 每个作品包含足够的渲染信息（模板 ID、卡片数据、图片），可独立渲染
 * - 导出时从模板池中查找完整的模板对象进行渲染
 * - localStorage 上限约 5MB，建议保存不超过 20 个作品
 */
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { SavedWork } from '@/types'

/** localStorage 持久化 key */
const STORAGE_KEY = 'gamecard-saved-works'

/** 最大保存数量 */
const MAX_WORKS = 30

// ── 持久化工具 ──

function loadWorksFromStorage(): SavedWork[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw) as SavedWork[]
  } catch {
    /* ignore corrupt data */
  }
  return []
}

function saveWorksToStorage(works: SavedWork[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(works))
  } catch (e) {
    console.warn('保存作品失败，可能超出 localStorage 容量限制:', e)
  }
}

// ── 唯一 ID 生成 ──

function generateId(): string {
  const ts = Date.now().toString(36)
  const rand = Math.random().toString(36).slice(2, 8)
  return `work_${ts}_${rand}`
}

// ── Store ──

export const useWorksStore = defineStore('works', () => {
  const works = ref<SavedWork[]>([])

  // ── 初始化：从 localStorage 加载 ──
  works.value = loadWorksFromStorage()

  // ── 自动持久化 ──
  watch(works, (val) => {
    saveWorksToStorage(val)
  }, { deep: true })

  // ── 操作方法 ──

  /** 获取所有作品（按更新时间倒序） */
  function getWorks(): SavedWork[] {
    return [...works.value].sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
    )
  }

  /** 根据 ID 获取单个作品 */
  function getWork(id: string): SavedWork | undefined {
    return works.value.find(w => w.id === id)
  }

  /** 保存作品（新增或更新同名卡片数据） */
  function save(work: Omit<SavedWork, 'id' | 'createdAt' | 'updatedAt'>): SavedWork {
    const now = new Date().toISOString()

    // 检查同名作品（同模板 + 同名 → 覆盖更新）
    const existing = works.value.findIndex(
      w => w.name === work.name && w.templateId === work.templateId,
    )

    if (existing !== -1) {
      const updated: SavedWork = {
        ...works.value[existing],
        ...work,
        id: works.value[existing].id,
        createdAt: works.value[existing].createdAt,
        updatedAt: now,
      }
      works.value[existing] = updated
      return updated
    }

    // 新增
    const saved: SavedWork = {
      ...work,
      id: generateId(),
      createdAt: now,
      updatedAt: now,
    }

    works.value.unshift(saved)

    // 裁剪超出上限的旧作品
    if (works.value.length > MAX_WORKS) {
      works.value.length = MAX_WORKS
    }

    return saved
  }

  /** 删除作品 */
  function remove(id: string): void {
    works.value = works.value.filter(w => w.id !== id)
  }

  return {
    works,
    getWorks,
    getWork,
    save,
    remove,
  }
})
