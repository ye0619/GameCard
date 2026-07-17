# 模板系统使用指南

## 概述

模板系统采用**配置驱动**架构，模板仅负责：
1. 数据展示结构（定义有哪些字段）
2. 元素排列组合（字段的类型和顺序）
3. 样式设计（颜色、图标、背景）
4. 布局控制（通过 themeMapping 控制主题）

模板**不包含**任何业务逻辑判断，所有数据通过统一接口传递。

---

## 一、模板文件结构

### 存放位置

```
src/main/resources/
├── templates/
│   └── {your-template-id}/
│       └── template.json          ← 模板定义文件
│
└── static/templates/
    └── {your-template-id}/
        ├── backgrounds/           ← 背景 SVG 素材
        ├── icons/                 ← 图标 SVG 素材
        └── frames/                ← 边框 SVG 素材
```

模板扫描路径默认为 `templates/*/template.json`，由 `application.yml` 中 `gamecard.template.scan-path` 控制。

### 前端对应

```
frontend/src/
├── types/index.ts                 ← TypeScript 类型定义
├── stores/card.ts                 ← 状态管理（自动适配模板配置）
├── components/card/
│   ├── CardRenderer.vue           ← 通用渲染器（按模板配置渲染）
│   ├── CardContainer.vue          ← 卡片容器
│   ├── CardBackground.vue         ← 背景
│   ├── CardHeader.vue             ← 头部（名称 + 属性标签 + LV/CP）
│   ├── CardImage.vue              ← 图片
│   ├── CardStats.vue              ← 属性统计（由 statFields 驱动）
│   ├── CardSkills.vue             ← 技能列表
│   └── CardDescription.vue        ← 描述
└── adapter/
    ├── types.ts                   ← 数据适配层接口
    ├── pokemon-adapter.ts         ← Pokemon 数据适配器
    └── index.ts
```

---

## 二、template.json 完整结构

```json
{
  "id": "your-template-id",
  "name": "模板显示名称",
  "description": "模板描述",
  "author": "作者",
  "version": "1.0.0",
  "previewImage": "/images/preview/xxx.png",
  "tags": ["标签1", "标签2"],
  "size": {
    "width": 600,
    "height": 900
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
    { "key": "nature", "label": "性格", "type": "ENUM", "required": true, "options": "[\"值1\",\"值2\"]" }
  ],

  "assets": {
    "backgrounds": { "bg-xxx": "backgrounds/xxx.svg" },
    "icons": { "xxx-icon": "icons/xxx.svg" },
    "frames": { "default": "frames/default.svg" },
    "fonts": {}
  },

  "metadata": {
    "createTime": "2026-07-01T00:00:00",
    "updateTime": "2026-07-16T00:00:00",
    "templateVersion": "1.0.0",
    "minAppVersion": "0.1.0"
  }
}
```

---

## 三、核心字段详解

### 3.1 顶层字段

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `id` | string | ✅ | 模板唯一标识，用于 URL 和缓存 key。必须与目录名一致 |
| `name` | string | ✅ | 模板显示名称 |
| `themeField` | string | ✅ | 驱动主题映射的字段 key。用户选择该字段的值 → 匹配 themeMapping → 确定颜色/图标/背景 |
| `statFields` | string[] | ❌ | 属性统计字段 key 列表。用于雷达图/数值条展示的维度定义 |
| `themeMapping` | object | ✅ | 主题映射表（见 3.2） |
| `fields` | array | ✅ | 字段定义列表（见 3.3） |
| `assets` | object | ✅ | 资源定义（见 3.4） |

### 3.2 themeMapping — 主题映射

`themeMapping` 将 `themeField` 字段的每个可选值映射为一组视觉样式：

```json
"themeField": "nature",
"themeMapping": {
  "暴躁": {
    "background": "bg-aggressive",     // 对应 assets.backgrounds 的 key
    "color": "#EF4444",                // 主色
    "icon": "aggressive",              // 对应 assets.icons 的 key
    "secondaryColor": "#DC2626",       // 辅助色
    "styles": {                        // CSS 自定义属性扩展
      "gradient-start": "#F97316",
      "gradient-end": "#DC2626",
      "glow-color": "rgba(239, 68, 68, 0.3)"
    }
  }
}
```

**主题映射的值必须与 `themeField` 对应字段的可选值完全一致**。例如 `themeField: "nature"`，则 themeMapping 的 key 必须与 fields 中 `key: "nature"` 的 options 一致。

### 3.3 fields — 字段定义

支持以下字段类型：

| type | 说明 | 额外属性 |
|------|------|---------|
| `TEXT` | 单行文本 | `placeholder` |
| `TEXTAREA` | 多行文本 | `placeholder`, `rows` |
| `NUMBER` | 数字输入 | `placeholder`, `defaultValue` |
| `SELECT` | 下拉选择 | `options` (JSON 数组字符串) |
| `ENUM` | 标签组选择 | `options` (JSON 数组字符串) |
| `ARRAY` | 动态列表 | `fields` (子字段定义) |
| `COLOR` | 颜色选择器 | |
| `IMAGE` | 图片上传 | 实际使用独立上传组件 |

字段结构：

```json
{
  "key": "field_key",            // 字段标识，在 cardData 中以此 key 存取
  "label": "显示标签",
  "type": "ENUM",
  "required": true,
  "defaultValue": "默认值",
  "placeholder": "占位提示",
  "options": "[\"选项A\",\"选项B\",\"选项C\"]",   // SELECT/ENUM 必填
  "fields": [                                      // ARRAY 类型必填
    {
      "key": "name",
      "label": "子字段名",
      "type": "TEXT",
      "required": true,
      "placeholder": "请输入..."
    }
  ]
}
```

### 3.4 assets — 资源定义

```json
"assets": {
  "backgrounds": {
    "bg-xxx": "backgrounds/xxx.svg"        // themeMapping 通过此 key 引用
  },
  "icons": {
    "xxx-icon": "icons/xxx.svg"            // themeMapping 通过此 key 引用
  },
  "frames": {
    "default": "frames/default.svg"
  },
  "fonts": {}
}
```

资源路径相对于 `static/templates/{template-id}/` 目录。

---

## 四、修改现有模板

以 pokemon-template 为例：

### 4.1 修改字段

编辑 `src/main/resources/templates/pokemon-template/template.json` 中的 `fields` 数组。

**示例：增加一个新属性字段**

```json
{
  "key": "agility",
  "label": "敏捷",
  "type": "NUMBER",
  "required": false,
  "defaultValue": "60",
  "placeholder": "敏捷"
}
```

同时更新 `statFields` 数组：

```json
"statFields": ["wisdom", "constitution", "perception", "luck", "charm", "strength", "agility"]
```

> **注意**：雷达图仅在 statFields 恰好为 6 项时显示。超过 6 项只显示数值条。

### 4.2 修改主题映射

**不改颜色，只改名称：**

修改 `themeMapping` 的 key 和 `fields` 中对应 ENUM 的 `options` 即可。确保两者一致。

**示例：将"暴躁"改为"热血"**

```json
// themeMapping 中
"热血": {
  "background": "bg-aggressive",   // 颜色资产不变
  "color": "#EF4444",
  "icon": "aggressive"
}

// fields 中
{
  "key": "nature",
  "label": "性格",
  "type": "ENUM",
  "options": "[\"热血\",\"温柔\",\"活泼\",...]"
}
```

### 4.3 修改视觉风格

每个 themeMapping 条目控制：
- `color` / `secondaryColor` — 主色/辅色
- `styles.gradient-start` / `gradient-end` — 渐变起止色
- `styles.glow-color` — 辉光色
- `background` — 背景图（需在 assets 中有对应 entry）
- `icon` — 图标（需在 assets 中有对应 entry）

修改颜色值即可改变对应主题的视觉风格。

### 4.4 替换/新增 SVG 素材

1. 将 SVG 文件放入 `static/templates/{template-id}/backgrounds/` 或 `icons/`
2. 在 `assets.backgrounds` 或 `assets.icons` 中添加映射条目
3. 在 `themeMapping` 中引用新 key

---

## 五、创建新模板

### 步骤 1：创建目录和 JSON

```
src/main/resources/
├── templates/my-character/       ← id 为 "my-character"
│   └── template.json
└── static/templates/my-character/
    ├── backgrounds/
    ├── icons/
    └── frames/
```

### 步骤 2：编写 template.json

参考以下最小模板：

```json
{
  "id": "my-character",
  "name": "角色卡片",
  "description": "通用角色卡片模板",
  "author": "Your Name",
  "version": "1.0.0",
  "previewImage": "",
  "tags": ["通用"],
  "size": { "width": 600, "height": 900 },
  "themeField": "type",
  "statFields": [],
  "themeMapping": {
    "默认": {
      "background": "default-bg",
      "color": "#6366F1",
      "icon": "default-icon",
      "secondaryColor": "#4F46E5",
      "styles": {
        "gradient-start": "#818CF8",
        "gradient-end": "#4F46E5",
        "glow-color": "rgba(99, 102, 241, 0.3)"
      }
    }
  },
  "fields": [
    { "key": "name", "label": "名称", "type": "TEXT", "required": true, "placeholder": "角色名" },
    { "key": "type", "label": "类型", "type": "ENUM", "required": true, "options": "[\"默认\"]" },
    { "key": "description", "label": "描述", "type": "TEXTAREA", "required": false, "placeholder": "输入描述..." }
  ],
  "assets": {
    "backgrounds": { "default-bg": "backgrounds/default.svg" },
    "icons": { "default-icon": "icons/default.svg" },
    "frames": {},
    "fonts": {}
  },
  "metadata": {
    "createTime": "2026-07-17T00:00:00",
    "updateTime": "2026-07-17T00:00:00",
    "templateVersion": "1.0.0",
    "minAppVersion": "0.1.0"
  }
}
```

### 步骤 3：添加素材

创建必要的 SVG 文件：
- `static/templates/my-character/backgrounds/default.svg`
- `static/templates/my-character/icons/default.svg`

### 步骤 4：重启应用

模板由 `LocalTemplateLoader` 在应用启动时扫描加载。新增模板后需重启后端。

---

## 六、数据流与渲染机制

### 6.1 字段 → 展示 映射

模板字段与渲染组件的对应关系：

| 字段场景 | 渲染组件 | 说明 |
|---------|---------|------|
| `name` | CardHeader | 显示为标题 |
| `themeField` 对应的字段 | CardHeader | 显示为属性标签 + 主题色 |
| `statFields` 中列出的字段 | CardStats | 显示为数值条 + 雷达图 |
| `skills` | CardSkills | 显示为技能标签列表 |
| `description` | CardDescription | 显示为描述文本 |
| `level`, `cp` | CardHeader | 显示为右侧徽章 |
| IMAGE 类型字段 | CardImage | 使用独立上传组件 |

### 6.2 主题解析流程

```
用户选择 themeField 的值（如 "nature": "暴躁"）
  → resolveTheme() 查找 template.themeMapping["暴躁"]
    → 返回 { color, icon, background, cssVars }
      → Card* 组件使用 theme.color / theme.cssVars 渲染
```

### 6.3 数据适配层

对于需要从外部导入数据的场景，可使用适配器：

```typescript
import { pokemonAdapter } from '@/adapter'

const rawData = { name: '喷火龙', type: 'fire' }
const viewModel = pokemonAdapter.transform(rawData)
// viewModel.nature → "暴躁"（自动映射）
```

新增适配器需实现 `CardDataAdapter` 接口：

```typescript
import type { CardDataAdapter, CardViewModel } from '@/adapter/types'

export class MyAdapter implements CardDataAdapter {
  readonly id = 'my-adapter'
  readonly name = '我的适配器'

  canHandle(raw: unknown): raw is Record<string, unknown> {
    // 判断是否能处理该数据
    return true
  }

  transform(raw: Record<string, unknown>): CardViewModel {
    return {
      name: String(raw.name ?? ''),
      nature: String(raw.type ?? ''),
      stats: {},
      skills: [],
      description: String(raw.desc ?? ''),
      image: null,
      extras: {},
    }
  }
}
```

---

## 七、常见问题

### Q: 新建模板后前端看不到？
确保：
1. 目录名与 `template.json` 中的 `id` 一致
2. 文件位于 `src/main/resources/templates/{id}/template.json`
3. 重启后端应用

### Q: 主题颜色不生效？
检查：
1. `themeField` 指定的字段 key 在 `fields` 中存在
2. `themeMapping` 的 key 与对应字段的 `options` 值完全一致（区分大小写）
3. `themeMapping` 中 `color` 值格式正确（如 `"#EF4444"`）

### Q: 图标/背景不显示？
检查：
1. SVG 文件存在于 `static/templates/{id}/` 下
2. `assets.backgrounds` / `assets.icons` 中有对应 key
3. `themeMapping` 中引用的 key 在 `assets` 中存在
4. SVG 文件路径正确

### Q: 雷达图不显示？
检查：
1. `statFields` 数组是否**恰好 6 项**
2. `statFields` 中指定的 key 在 `fields` 中都有对应的 NUMBER 字段
3. 后端 `Template.java` 是否有 `statFields` 字段（Spring Boot 4.x 需确保编译）

### Q: 属性数值条全为 0？
检查 `fields` 中对应 NUMBER 字段是否设置了 `defaultValue`。如果没有默认值，前端会用 0 填充。

---

## 八、模板设计原则

1. **语义中立**：字段 key 使用通用名称（`nature` 而非 `pokemon_type`），便于跨模板复用
2. **配置驱动**：所有视觉变化通过 `themeMapping` 控制，不修改前端组件代码
3. **数据解耦**：模板不关心数据来源，通过适配层处理数据格式转换
4. **渐进增强**：可在不影响现有功能的前提下逐步扩展模板能力
