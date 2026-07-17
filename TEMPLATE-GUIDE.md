# 模板系统使用指南（AI 版）

> 本文档面向 AI 或开发者，用于指导创建新的游戏卡片模板。
> 模板系统采用**配置驱动**架构，新增模板只需创建 JSON 文件 + SVG 素材，**无需修改后端 Java 代码**。

---

## 一、架构总览

### 数据流

```
template.json + SVG 素材
       │
       ▼
LocalTemplateLoader（启动时扫描 classpath:templates/*/template.json）
       │
       ▼
JsonTemplateReader（Jackson 反序列化 → Template Java 对象）
       │
       ▼
TemplateManagerImpl（缓存 + 生命周期管理）
       │
       ▼
TemplateServiceImpl（业务封装）
       │
       ▼
TemplateController（REST API：GET /api/templates）
       │
       ▼
前端 fetchTemplates() → Pinia store → CardRenderer 按配置渲染
```

### 后端模型类（全部位于 `com.gamecard.template.model`）

```
Template.java          ← 核心模型，包含所有模板配置
├── TemplateSize.java         尺寸
├── TemplateField.java        字段定义
├── ThemeMappingItem.java     主题映射条目
├── TemplateAssets.java       资源定义
├── TemplateMetadata.java     元数据
├── PresetSkill.java          ✅ 预设技能（新增）
└── PresetIntroduction.java   ✅ 预设角色简介（新增）
```

---

## 二、模板文件结构

### 2.1 目录布局

```
src/main/resources/
├── templates/
│   └── {your-template-id}/          ← id 必须与目录名一致
│       └── template.json             ← 模板定义文件（唯一必需）
│
└── static/templates/
    └── {your-template-id}/
        ├── backgrounds/              ← 背景 SVG 素材
        ├── icons/                    ← 图标 SVG 素材
        └── frames/                   ← 边框 SVG 素材
```

### 2.2 前端对应目录

```
frontend/src/
├── types/index.ts                     ← TypeScript 类型定义
├── stores/card.ts                     ← Pinia 状态管理（自动适配模板）
├── api/template.ts                    ← API 客户端
├── adapter/
│   ├── types.ts                       ← 数据适配层接口
│   └── pokemon-adapter.ts             ← 参考实现
├── components/
│   ├── card/                          ← 卡片渲染组件
│   │   ├── CardRenderer.vue           ← 通用渲染器
│   │   ├── CardContainer.vue          ← 卡片容器
│   │   ├── CardBackground.vue         ← 背景
│   │   ├── CardHeader.vue             ← 头部（名称 + 属性标签 + 徽章）
│   │   ├── CardImage.vue              ← 图片
│   │   ├── CardStats.vue              ← 属性统计（由 statFields 驱动）
│   │   ├── CardSkills.vue             ← 技能列表
│   │   └── CardDescription.vue        ← 描述
│   └── editor/
│       ├── CardEditor.vue             ← 表单编辑器（自动渲染 fields）
│       ├── PresetSkillSelector.vue    ✅ 预设技能选择器
│       └── PresetIntroSelector.vue    ✅ 预设简介选择器
```

---

## 三、template.json 完整结构

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

  "metadata": {
    "createTime": "2026-07-01T00:00:00",
    "updateTime": "2026-07-17T00:00:00",
    "templateVersion": "1.0.0",
    "minAppVersion": "0.1.0"
  }
}
```

---

## 四、Java 模型类详解

### 4.1 Template.java — 核心模板模型

```java
package com.gamecard.template.model;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Template {
    private String id;                          // 模板唯一标识
    private String name;                        // 显示名称
    private String description;                 // 描述
    private String author;                      // 作者
    private String version;                     // 版本号
    private String previewImage;                // 预览图路径
    private List<String> tags;                  // 标签
    private TemplateSize size;                  // 卡片尺寸
    private List<TemplateField> fields;         // 字段定义列表
    private Map<String, ThemeMappingItem> themeMapping;  // 主题映射表
    private String themeField;                  // 主题驱动字段 key
    private List<String> statFields;            // 统计字段 key 列表
    private TemplateAssets assets;              // 资源定义
    private TemplateMetadata metadata;          // 元数据
    private List<PresetSkill> presetSkills;     // ✅ 预设技能（可选）
    private List<PresetIntroduction> presetIntroductions;  // ✅ 预设角色简介（可选）
}
```

> **注意**：`JsonTemplateReader` 使用 Jackson 3.x 反序列化，已关闭 `FAIL_ON_UNKNOWN_PROPERTIES`，所以 JSON 中的未知字段会被安全忽略。这也意味着即使 Java 模型没有某个字段，多余的 JSON 属性也不会报错。

### 4.2 TemplateField.java — 字段定义

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
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

| type | 说明 | 渲染组件 | 额外字段 |
|------|------|---------|---------|
| `TEXT` | 单行文本 | `<input>` | placeholder |
| `TEXTAREA` | 多行文本 | `<textarea>` | placeholder |
| `NUMBER` | 数字 | `<input type="number">` | placeholder, defaultValue |
| `SELECT` | 下拉选择 | `<select>` | options（JSON 字符串数组） |
| `ENUM` | 标签组 | 按钮组 | options（JSON 字符串数组） |
| `ARRAY` | 动态列表 | 输入列表 + 添加/删除 | fields（子字段定义） |
| `COLOR` | 颜色选择 | `<input type="color">` | — |
| `IMAGE` | 图片上传 | 独立上传组件 | — |

### 4.3 ThemeMappingItem.java — 主题映射条目

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ThemeMappingItem {
    private String background;                  // 背景标识
    private String color;                       // 主色调
    private String icon;                        // 图标标识
    private String secondaryColor;              // 辅助色
    private Map<String, String> styles;         // 扩展样式（gradient-start, gradient-end, glow-color）
}
```

### 4.4 TemplateAssets.java — 资源定义

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateAssets {
    private Map<String, String> backgrounds;    // 背景映射
    private Map<String, String> icons;          // 图标映射
    private Map<String, String> frames;         // 边框映射
    private Map<String, String> fonts;          // 字体映射
}
```

### 4.5 TemplateSize.java

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateSize {
    private int width;
    private int height;
}
```

### 4.6 TemplateMetadata.java

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateMetadata {
    private String createTime;          // ISO 日期字符串
    private String updateTime;          // ISO 日期字符串
    private String templateVersion;     // 模板版本
    private String minAppVersion;       // 最低应用版本
}
```

### 4.7 PresetSkill.java — 预设技能 ✅

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PresetSkill {
    private String name;                // 技能名称（必需）
    private String category;            // 分类（如"攻击""防御""辅助""特殊"）
    private String description;         // 技能描述
    private Integer power;              // 威力等级（1-100）
}
```

### 4.8 PresetIntroduction.java — 预设角色简介 ✅

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PresetIntroduction {
    private String title;               // 预设标题（必需）
    private String content;             // 简介/背景故事内容（必需）
    private String natureHint;          // 推荐性格搭配
}
```

---

## 五、前端 TypeScript 接口

### 5.1 Template（完整定义，位于 `types/index.ts`）

```typescript
export interface Template {
  id: string
  name: string
  description: string
  author: string
  version: string
  previewImage: string
  tags: string[]
  size: TemplateSize | null
  fields: TemplateField[]
  themeMapping: Record<string, ThemeMappingItem> | null
  themeField: string | null
  statFields: string[] | null
  assets: TemplateAssets | null
  metadata: TemplateMetadata
  presetSkills?: PresetSkill[] | null            // ✅
  presetIntroductions?: PresetIntroduction[] | null  // ✅
}
```

### 5.2 PresetSkill（位于 `types/index.ts`）

```typescript
export interface PresetSkill {
  name: string              // 技能名称
  category?: string         // 分类（"攻击"/"防御"/"辅助"/"特殊"）
  description?: string      // 技能描述
  power?: number            // 威力（1-100）
}
```

### 5.3 PresetIntroduction（位于 `types/index.ts`）

```typescript
export interface PresetIntroduction {
  title: string             // 预设标题
  content: string           // 简介内容
  natureHint?: string       // 推荐性格
}
```

### 5.4 其他重要接口

```typescript
export interface ThemeMappingItem {
  background: string
  color: string
  icon: string
  secondaryColor?: string
  styles?: Record<string, string>
}

export interface TemplateField {
  key: string
  label: string
  type: FieldType    // 'TEXT' | 'TEXTAREA' | 'IMAGE' | 'NUMBER' | 'SELECT' | 'ENUM' | 'ARRAY' | 'COLOR'
  required: boolean
  defaultValue: string | null
  placeholder: string | null
  options: string | null     // JSON 数组字符串
  fields: TemplateField[] | null  // ARRAY 子字段
}

export interface TemplateAssets {
  backgrounds: Record<string, string> | null
  icons: Record<string, string> | null
  frames: Record<string, string> | null
  fonts: Record<string, string> | null
}

export interface CardData {
  [key: string]: string     // 用户填写的卡片数据
}
```

---

## 六、创建新模板的步骤

### 步骤 1：确定模板 ID 和目录

```bash
# 创建模板定义文件
src/main/resources/templates/{your-id}/template.json

# 创建素材目录
src/main/resources/static/templates/{your-id}/backgrounds/
src/main/resources/static/templates/{your-id}/icons/
src/main/resources/static/templates/{your-id}/frames/
```

> ⚠️ `template.json` 中的 `id` 字段必须与目录名完全一致。

### 步骤 2：编写 template.json

使用以下最小模板作为起点：

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
    { "key": "description", "label": "描述", "type": "TEXTAREA", "required": false, "placeholder": "输入描述..." },
    { "key": "image", "label": "角色图片", "type": "IMAGE", "required": true }
  ],
  "presetIntroductions": [
    {
      "title": "示例角色",
      "content": "一个来自奇幻世界的冒险者，正在寻找属于自己的传奇……",
      "natureHint": "默认"
    }
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

### 步骤 3：添加 SVG 素材

- `static/templates/my-character/backgrounds/default.svg`
- `static/templates/my-character/icons/default.svg`
- 可选：`static/templates/my-character/frames/default.svg`

### 步骤 4：重启后端

模板由 `LocalTemplateLoader` 在应用启动时扫描加载。新增模板后需重启 Spring Boot 后端。

---

## 七、核心机制详解

### 7.1 主题映射（themeMapping）机制

```
用户选择 themeField 的值（如 "nature": "暴躁"）
  → resolveTheme() 查找 template.themeMapping["暴躁"]
    → 返回 ThemeMappingItem { color, icon, background, styles }
      → Card* 组件使用 theme.color / theme.cssVars 渲染
```

**约束**：
- `themeMapping` 的 key 必须与 `themeField` 对应字段的 `options` 完全一致（区分大小写）
- 若用户选择的值在 `themeMapping` 中不存在，会使用默认主题（靛蓝色 `#6366F1`）

### 7.2 字段与渲染组件的对应关系

| 字段 key | 渲染组件 | 说明 |
|---------|---------|------|
| `name` | CardHeader | 显示为卡片标题 |
| `themeField` 对应的字段（如 `nature`） | CardHeader | 显示为属性标签，驱动主题色 |
| `statFields` 中列出的字段 | CardStats | 显示为数值条，6 项时额外显示雷达图 |
| `skills`（ARRAY 类型） | CardSkills | 显示为技能标签列表 |
| `description`（TEXTAREA 类型） | CardDescription | 显示为描述文本 |
| 非 statFields 的 NUMBER 字段（如 `level`, `cp`） | CardHeader | 显示为右侧徽章 |
| IMAGE 类型字段 | CardImage | 使用独立上传组件 |

### 7.3 预设技能系统（presetSkills）✅

**用途**：为技能输入提供快捷选择列表，用户点击预设即可添加技能到卡片。

**示例数据**：
```json
"presetSkills": [
  { "name": "火焰吐息", "category": "攻击", "description": "释放灼热的火焰", "power": 85 },
  { "name": "铁壁防御", "category": "防御", "description": "大幅提升防御力", "power": 55 },
  { "name": "治愈之光", "category": "辅助", "description": "恢复全队生命值", "power": 50 },
  { "name": "星辰陨落", "category": "特殊", "description": "召唤星辰之力", "power": 100 }
]
```

**表现**：
- 在编辑器 ARRAY 类型字段上方显示为分类标签组
- 点击标签自动将技能添加到输入列表，并同步到卡片数据
- 已添加的技能显示删除线且不可重复添加
- 高威力技能（power ≥ 80）悬停时显示红色高亮

**技巧**：
- 使用 `category` 对技能进行分组，推荐分类：`攻击`、`防御`、`辅助`、`特殊`
- 不填写 `category` 的技能会被归入 `通用` 分类
- `power` 仅为视觉提示，不影响实际游戏逻辑

### 7.4 预设角色简介系统（presetIntroductions）✅

**用途**：提供预编写的角色背景故事，用户一键填充到描述字段。

**示例数据**：
```json
"presetIntroductions": [
  {
    "title": "独行侠客",
    "content": "一位游历大陆的独行侠客，身披残破的斗篷，腰间的长剑已陪伴他走过无数个春秋……",
    "natureHint": "高冷"
  },
  {
    "title": "火焰之子",
    "content": "出生时便被火焰祝福的孩子，体内流淌着灼热的炎之血脉……",
    "natureHint": "暴躁"
  }
]
```

**表现**：
- 在编辑器的描述字段下方显示为折叠式选择器
- 展开后显示预设标题、简介预览和推荐性格标签
- 根据当前已选的性格匹配推荐预设（高亮显示）
- 点击预设自动填充描述内容，支持一键清除

**技巧**：
- `natureHint` 用于与当前选择的性格进行匹配，使推荐更智能
- `title` 应简短有力（4-8 字为宜）
- `content` 建议 50-200 字，过长会在预览中截断

### 7.5 数据适配层

对于需要从外部导入数据的场景，可实现 `CardDataAdapter` 接口：

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

## 八、statFields 与数据展示

### 雷达图 vs 数值条

```
statFields 数量    →    展示效果
    0             →    不显示属性区域
    1-5           →    仅显示数值条
    6             →    雷达图 + 数值条
    7+            →    仅显示数值条（超 6 项）
```

`statFields` 数组中的 key 必须在 `fields` 中有对应的 `NUMBER` 类型字段。

**示例（6 项 → 雷达图）**：
```json
"statFields": ["wisdom", "constitution", "perception", "luck", "charm", "strength"]
```

---

## 九、常见问题排查

### Q: 新建模板后前端看不到？
1. 确认目录名与 `template.json` 中 `id` 一致
2. 确认文件路径为 `src/main/resources/templates/{id}/template.json`
3. 重启后端应用

### Q: 主题颜色不生效？
1. 确认 `themeField` 指定的 key 在 `fields` 中存在
2. 确认 `themeMapping` 的 key 与对应 `ENUM` 的 `options` 完全一致
3. 确认 `color` 值格式为 `"#RRGGBB"`

### Q: 雷达图不显示？
1. 确认 `statFields` 数组**恰好 6 项**
2. 确认每项在 `fields` 中都有对应的 `NUMBER` 字段

### Q: 预设技能不显示？
1. 确认 `presetSkills` 数组格式正确，每个元素至少包含 `name`
2. 确认模板中有 ARRAY 类型的字段用于技能输入
3. 重启后端使 JSON 变更生效

### Q: 预设简介不显示？
1. 确认 `presetIntroductions` 数组格式正确，每个元素包含 `title` 和 `content`
2. 仅在 `key: "description"` 的 TEXTAREA 字段下显示
3. 重启后端使 JSON 变更生效

### Q: 图标/背景不显示？
1. SVG 文件是否存在于 `static/templates/{id}/backgrounds/` 或 `icons/`
2. `assets` 中是否有对应映射
3. `themeMapping` 中引用的 key 是否在 `assets` 中存在

---

## 十、模板设计原则

1. **语义中立**：字段 key 使用通用名称（`nature` 而非 `pokemon_type`），便于跨模板复用
2. **配置驱动**：所有视觉变化通过 `themeMapping` 控制，不修改前端渲染组件
3. **数据解耦**：模板不关心数据来源，通过适配层处理格式转换
4. **渐进增强**：`presetSkills` 和 `presetIntroductions` 为可选字段，不影响旧模板
5. **素材完整**：每个 `themeMapping` 条目引用的 `background` 和 `icon`，必须在 `assets` 中有对应条目
