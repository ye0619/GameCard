# 模板系统使用指南（AI 版）

> 本文档面向 AI 或开发者，用于指导创建新的游戏卡片模板。
> 模板系统采用**配置驱动**架构，新增模板只需创建 JSON 文件 + SVG 素材 + 前端布局组件。

---

## 一、架构总览（更新版）

### 1.1 双端数据流

```
template.json + SVG 素材（后端）
       │
       ▼
LocalTemplateLoader（启动时扫描 classpath:templates/*/template.json）
       │
       ▼
TemplateController（REST API：GET /api/templates）
       │
       ▼
前端 fetchTemplates() → Pinia store
       │
       ▼
TemplateRenderer（根据 template.id 动态加载）
       │
       ├── 加载 templates/{id}/layout.vue ← ★ 新增
       │
       └── 传入 TemplateLayoutProps：
             ├── cardData      — 用户填写的卡片数据
             ├── theme         — 解析后的主题（颜色/图标/CSS变量）
             ├── imageUrl      — 角色图片 URL
             ├── imageConfig   — 图片编辑配置
             └── template      — 模板定义对象
```

### 1.2 前端目录结构（新增）

```
frontend/src/
├── templates/                          ← ★ 每个模板独立目录
│   ├── pokemon-template/
│   │   ├── layout.vue                  ← 组件结构 + 布局
│   │   ├── index.ts                    ← 模板模块导出
│   │   └── components/                 ← 模板自有组件（可选）
│   │       └── TypeBadge.vue
│   │
│   └── baldurs-gate2/
│       ├── layout.vue                  ← 完全不同的布局
│       ├── index.ts
│       └── components/
│           ├── EquipmentPanel.vue
│           ├── AttributePanel.vue
│           ├── PortraitPanel.vue
│           ├── InfoPanel.vue
│           ├── StatusBar.vue
│           └── StoneFrame.vue
│
├── renderer/                           ← ★ 动态加载器
│   ├── TemplateRenderer.vue            ← 根据 templateId 加载 layout.vue
│   ├── types.ts                        ← TemplateLayoutProps 接口
│   └── layoutModules.ts               ← Vite glob 模块映射
│
├── components/
│   ├── card/                           ← ★ 通用卡片积木（可选）
│   │   ├── CardBackground.vue          ← 背景（主题色渐变 + SVG + 辉光）
│   │   ├── CardHeader.vue              ← 头部（标题 + 属性标签 + 徽章）
│   │   ├── CardImage.vue               ← 图片（可定制位置/尺寸）
│   │   ├── CardStats.vue               ← 属性统计（雷达图 + 数值条）
│   │   ├── CardSkills.vue              ← 技能列表（可定制宽高）
│   │   └── CardDescription.vue         ← 描述文字
│   └── common/                         ← 基础 UI 组件
```

---

## 二、TemplateRenderer — 核心机制

### 2.1 工作原理

```
TemplateRenderer
  │
  ├── 监听 store.selectedTemplate.id
  │
  ├── 使用 Vite import.meta.glob 预扫描所有 templates/*/layout.vue
  │
  ├── 根据 id 从预构建映射中查找对应布局
  │
  └── 传入 TemplateLayoutProps 渲染布局
```

### 2.2 TemplateLayoutProps（每个 layout.vue 接收的标准 props）

```typescript
interface TemplateLayoutProps {
  /** 用户填写的所有卡片字段数据 */
  cardData: CardData                    // Record<string, string>
  /** 解析后的主题（颜色、图标、CSS 变量） */
  theme: ResolvedTheme
  /** 角色图片 URL（blob URL 或 data URI） */
  imageUrl: string | null
  /** 图片编辑配置（形状、缩放、位置、裁剪） */
  imageConfig: ImageConfig
  /** 当前模板定义 */
  template: Template | null
}
```

### 2.3 模板加载流程

```
1. 用户选择模板 → store.selectTemplate(id)
2. store.selectedTemplate 更新
3. TemplateRenderer watch 触发
4. 构造路径: /src/templates/{id}/layout.vue
5. 从 glob 映射查找 → 执行动态 import
6. 加载成功 → 渲染布局组件并传入 props
7. 加载失败 → 显示错误提示
```

---

## 三、创建新模板的完整步骤

### Step 1：创建后端模板目录

```
src/main/resources/
├── templates/{your-id}/
│   └── template.json          ← 模板定义（必需）
│
└── static/templates/{your-id}/
    ├── backgrounds/           ← 背景 SVG 素材
    ├── icons/                 ← 图标 SVG 素材
    └── frames/                ← 边框 SVG 素材
```

### Step 2：编写 template.json

参见后文"六、template.json 完整结构"。

> ⚠️ `template.json` 中的 `id` 必须与目录名完全一致。

### Step 3：创建前端模板目录

```
frontend/src/templates/{your-id}/
├── layout.vue                 ← ★ 组件结构 + 布局（必需）
├── index.ts                   ← 导出布局组件
└── components/                ← 模板自有组件（可选）
```

### Step 4：编写 layout.vue

每个模板的 `layout.vue` 接收 `TemplateLayoutProps`，完全自主决定渲染结构。

**最小模板示例：**

```vue
<script setup lang="ts">
import type { TemplateLayoutProps } from '@/renderer/types'

defineProps<TemplateLayoutProps>()
</script>

<template>
  <div class="game-card">
    <h1>{{ cardData.name }}</h1>
    <img v-if="imageUrl" :src="imageUrl" :alt="cardData.name" />
    <p>{{ cardData.description }}</p>
  </div>
</template>

<style scoped>
.game-card {
  width: 1280px;
  height: 720px;
  /* 模板自己的样式 */
}
</style>
```

**完整模板示例（使用通用组件）：**

```vue
<script setup lang="ts">
import { computed } from 'vue'
import type { TemplateLayoutProps } from '@/renderer/types'
import CardBackground from '@/components/card/CardBackground.vue'
import CardHeader, { type BadgeItem } from '@/components/card/CardHeader.vue'
import CardImage from '@/components/card/CardImage.vue'

const props = defineProps<TemplateLayoutProps>()

const badges = computed((): BadgeItem[] => {
  const items: BadgeItem[] = []
  if (props.cardData['level']) items.push({ label: 'LV', value: props.cardData['level'] })
  return items
})
</script>

<template>
  <div
    class="game-card"
    :style="{
      '--card-primary': theme.color,
      '--card-gradient-start': theme.cssVars['--card-gradient-start'],
      '--card-glow': theme.cssVars['--card-glow'],
    }"
  >
    <CardBackground :theme="theme" :template="template" />
    <CardHeader :badges="badges" :theme="theme" />
    <CardImage :image="imageUrl" :name="cardData.name" />
  </div>
</template>

<style scoped>
.game-card {
  position: relative;
  width: 1280px;
  height: 720px;
  overflow: hidden;
  font-family: 'Arial', 'Microsoft YaHei', sans-serif;
  color: white;
  background:
    linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.5)),
    var(--card-gradient-start);
  border-radius: 12px;
}
</style>
```

### Step 5：重启后端

后端 `LocalTemplateLoader` 在启动时扫描模板，新增后需重启。

### Step 6：验证

1. 后端 `GET /api/templates` 返回新模板条目
2. 前端模板选择器中出现新模板
3. 选择模板后，TemplateRenderer 加载 layout.vue
4. 卡片正常渲染，功能正常

---

## 四、模板架构设计原则

### 4.1 通用组件 vs 自有组件

**通用组件**（`components/card/`）提供标准卡片积木，模板可按需导入或完全忽略：

| 组件 | 用途 | 可定制 |
|------|------|--------|
| `CardBackground.vue` | 主题色渐变 + SVG 背景 + 辉光 | 接受 theme + template |
| `CardHeader.vue` | 标题 + 属性标签 + 右上角徽章 | 接受 title/tag/badges props |
| `CardImage.vue` | 角色图片展示 | 可覆盖位置/尺寸 |
| `CardStats.vue` | 雷达图 + 数值条 | showRadar/maxValue 开关 |
| `CardSkills.vue` | 技能列表 | 可覆盖面板高度/宽度 |
| `CardDescription.vue` | 描述文字块 | 简单文本 |

**自有组件**：模板目录下的 `components/` 存放模板特有的 UI 组件。

**选择原则**：
- 如果通用组件能满足需求 → 直接使用
- 如果通用组件无法实现想要的布局/功能 → 自己写组件
- 可以部分使用（如用 CardBackground 但不用 CardSkills）

### 4.2 配置驱动

所有模板差异通过 `template.json` 的 `themeMapping` 配置驱动，组件中禁止写 `if (type === "fire")` 条件判断。

### 4.3 模板 = 完整的布局定义

```
一个模板 = 自己的 layout.vue + 自己的样式 + 自己的组件 + 自己的数据映射
```

模板之间可以完全不同，没有共享的固定 UI 结构。

---

## 五、layout.vue 的常用模式

### 5.1 读取 cardData 字段

```typescript
const name = computed(() => props.cardData['name'] ?? '')
const level = computed(() => props.cardData['level'] ?? '')
const description = computed(() => props.cardData['description'] ?? '')
```

> 注意：`cardData` 的值类型为 `string`，数值字段也是字符串形式。需要使用 `parseInt(String(val), 10)` 转换。

### 5.2 解析技能列表

```typescript
const skills = computed(() => {
  const raw = props.cardData['skills'] ?? ''
  if (!raw || !raw.trim()) return []
  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      return parsed.map((item: any) => item.name ?? String(item)).filter(Boolean)
    }
  } catch {
    return raw.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
})
```

### 5.3 应用图片编辑配置

```typescript
const imageShapeStyle = computed(() => {
  const s: Record<string, string> = {}
  const cfg = props.imageConfig
  if (!cfg?.applied) return s
  if (cfg.crop) {
    const t = cfg.crop.y; const r = 1 - cfg.crop.x - cfg.crop.width
    const b = 1 - cfg.crop.y - cfg.crop.height; const l = cfg.crop.x
    s['clipPath'] = `inset(${t * 100}% ${r * 100}% ${b * 100}% ${l * 100}%)`
    return s
  }
  if (cfg.shape === 'circle') s['clipPath'] = 'circle(50%)'
  else if (cfg.shape === 'rounded') s['borderRadius'] = '12px'
  else s['borderRadius'] = '0'
  return s
})

const imageTransform = computed(() => {
  const cfg = props.imageConfig
  if (!cfg?.applied) return undefined
  if (cfg.scale === 1 && cfg.position.x === 0 && cfg.position.y === 0) return undefined
  return { transform: `translate(${cfg.position.x}px, ${cfg.position.y}px) scale(${cfg.scale})` }
})
```

### 5.4 卡片尺寸

- 模板的 `layout.vue` 中通过 CSS 设置卡片实际尺寸（`.game-card` 的 `width`/`height`）
- `template.json` 中的 `size` 字段用于预览缩放计算，应与 CSS 一致
- 固定尺寸建议：横版 RPG 通用 `1280×720`，D&D 角色纸 `900×700`

---

## 六、template.json 完整结构

```json
{
  "id": "your-template-id",
  "name": "模板显示名称",
  "description": "模板描述",
  "author": "作者名",
  "version": "1.0.0",
  "previewImage": "/images/preview/xxx.png",
  "tags": ["标签1", "标签2"],
  "size": {
    "width": 1280,
    "height": 720
  },

  "themeField": "nature",
  "statFields": ["wisdom", "constitution", "perception", "luck", "charm", "strength"],

  "themeMapping": {
    "值1": {
      "background": "bg-xxx",
      "color": "#EF4444",
      "icon": "xxx-icon",
      "secondaryColor": "#DC2626",
      "styles": {
        "gradient-start": "#F97316",
        "gradient-end": "#DC2626",
        "glow-color": "rgba(239, 68, 68, 0.3)"
      }
    }
  },

  "fields": [
    { "key": "name", "label": "名称", "type": "TEXT", "required": true },
    { "key": "nature", "label": "属性", "type": "ENUM", "required": true, "options": "[\"值1\",\"值2\"]" }
  ],

  "presetSkills": [
    { "name": "技能名", "category": "分类", "description": "技能描述", "power": 80 }
  ],

  "presetIntroductions": [
    { "title": "预设标题", "content": "简介内容……", "natureHint": "推荐性格" }
  ],

  "assets": {
    "backgrounds": { "bg-xxx": "backgrounds/xxx.svg" },
    "icons": { "xxx-icon": "icons/xxx.svg" },
    "frames": { "default": "frames/default.svg" },
    "fonts": {}
  },

  "imageConfig": {
    "imagePrompt": "AI 风格提示词……",
    "processing": {
      "removeBackground": true,
      "preferredRatio": "1:1"
    }
  },

  "metadata": {
    "createTime": "2026-07-01T00:00:00",
    "updateTime": "2026-07-17T00:00:00",
    "templateVersion": "1.0.0",
    "minAppVersion": "0.1.0"
  }
}
```

---

## 七、Java 模型类（后端模板定义）

> 后端模型无需修改，`LocalTemplateLoader` 启动时自动扫描 JSON 文件。

### 7.1 Template.java

```java
package com.gamecard.template.model;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Template {
    private String id;
    private String name;
    private String description;
    private String author;
    private String version;
    private String previewImage;
    private List<String> tags;
    private TemplateSize size;
    private List<TemplateField> fields;
    private Map<String, ThemeMappingItem> themeMapping;
    private String themeField;
    private List<String> statFields;
    private TemplateAssets assets;
    private TemplateMetadata metadata;
    private List<PresetSkill> presetSkills;
    private List<PresetIntroduction> presetIntroductions;
}
```

### 7.2 TemplateField.java

```java
public class TemplateField {
    private String key;                 // 字段标识
    private String label;               // 显示标签
    private String type;                // 字段类型
    private boolean required;           // 是否必填
    private String defaultValue;        // 默认值
    private String placeholder;         // 占位提示
    private String options;             // SELECT/ENUM 的选项（JSON 数组字符串）
    private List<TemplateField> fields; // ARRAY 类型的子字段
}
```

**type 可选值**：

| type | 说明 | 编辑器渲染 |
|------|------|-----------|
| `TEXT` | 单行文本 | `<input>` |
| `TEXTAREA` | 多行文本 | `<textarea>` |
| `NUMBER` | 数字 | `<input type="number">` |
| `SELECT` | 下拉选择 | `<select>` |
| `ENUM` | 标签组按钮 | 按钮组 |
| `ARRAY` | 动态列表 | 输入列表 + 添加/删除 |
| `COLOR` | 颜色选择 | `<input type="color">` |
| `IMAGE` | 图片上传 | 独立上传组件 |

---

## 八、预设系统

### 8.1 presetSkills（预设技能）

```json
"presetSkills": [
  { "name": "火焰吐息", "category": "攻击", "description": "释放灼热的火焰", "power": 85 }
]
```

- `category` 可选值：`攻击`、`防御`、`辅助`、`特殊`、`通用`
- 编辑器在 ARRAY 类型字段上方显示分类标签组
- 点击标签自动添加到技能列表
- `power ≥ 80` 的高威力技能悬停时红色高亮

### 8.2 presetIntroductions（预设角色简介）

```json
"presetIntroductions": [
  {
    "title": "独行侠客",
    "content": "一位游历大陆的独行侠客……",
    "natureHint": "高冷"
  }
]
```

- 在 `key: "description"` 的 TEXTAREA 字段下方显示
- `natureHint` 用于匹配当前选择的性格，高亮推荐

---

## 九、数据适配层

对于需要从外部数据源创建卡片的场景，实现 `CardDataAdapter` 接口：

```typescript
// frontend/src/adapter/types.ts
export interface CardDataAdapter<T = Record<string, unknown>> {
  readonly id: string
  readonly name: string
  transform(raw: T): CardViewModel
  canHandle(raw: unknown): raw is T
}
```

参考实现：`frontend/src/adapter/pokemon-adapter.ts`

---

## 十、常见问题排查

### Q: 新建模板后前端看不到？
1. 确认后端目录名与 `template.json` 中 `id` 一致
2. 确认文件路径为 `src/main/resources/templates/{id}/template.json`
3. 重启后端应用
4. 确认前端 `frontend/src/templates/{id}/layout.vue` 存在

### Q: 模板布局不加载？
1. 打开 F12 Console 查看报错信息
2. 确认 `layout.vue` 在 `frontend/src/templates/{id}/` 目录下
3. 确认 TemplateRenderer 的 `layoutModules` 能匹配到路径
4. 运行 `npm run build` 检查是否有编译错误

### Q: 主题颜色不生效？
1. 确认 `themeField` 指定的 key 在 `fields` 中存在
2. 确认 `themeMapping` 的 key 与对应 `ENUM` 的 `options` 完全一致
3. 确认 `color` 值格式为 `"#RRGGBB"`
4. 确认 layout.vue 的根元素绑定了 CSS 变量

### Q: 雷达图不显示？
1. 确认 `statFields` 数组**恰好 6 项**
2. 确认每项在 `fields` 中都有对应的 `NUMBER` 字段
3. 确认 `cardData` 中有默认值且 `parseInt()` 能正常解析
4. 确认 layout.vue 中正确传递 `statFields` 给 CardStats

### Q: 预设技能不显示？
1. 确认 `presetSkills` 数组格式正确，每个元素至少包含 `name`
2. 确认模板中有 ARRAY 类型的字段
3. 重启后端使 JSON 变更生效

### Q: 预设简介不显示？
1. 确认 `presetIntroductions` 数组格式正确
2. 仅在 `key: "description"` 的 TEXTAREA 字段下显示
3. 重启后端使 JSON 变更生效

### Q: 图标/背景不显示？
1. SVG 文件是否存在于 `static/templates/{id}/backgrounds/` 或 `icons/`
2. `assets` 中是否有对应映射
3. `themeMapping` 中引用的 key 是否在 `assets` 中存在
4. 确认前端 `/templates/{id}/` 路径被后端代理

### Q: 图片编辑（形状/缩放/拖拽）不生效？
1. 确认 layout.vue 中从 `imageConfig` 计算了 `imageShapeStyle` 和 `imageTransform`
2. 确认这些样式传入了 `CardImage` 的 `imgStyle` prop

---

## 十一、模板设计原则

1. **配置驱动**：所有视觉变化通过 `themeMapping` 控制，组件中禁用 if/else 硬编码主题判断
2. **模板自主**：每个模板的 `layout.vue` 决定自己的结构，不依赖固定的 CardRenderer
3. **按需积木**：通用卡片组件是可选积木，不是强制约束
4. **语义中立**：字段 key 使用通用名称（`nature` 而非 `pokemon_type`）
5. **数据解耦**：模板不关心数据来源，通过适配层处理格式转换
6. **渐进增强**：`presetSkills` 和 `presetIntroductions` 为可选字段
7. **素材完整**：每个 `themeMapping` 条目引用的 `background` 和 `icon`，必须在 `assets` 中有对应条目
8. **CSS 变量注入**：layout.vue 根元素需绑定 `--card-primary`、`--card-gradient-start` 等 CSS 变量，供背景和辉光组件使用
