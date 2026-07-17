# GameCard — 游戏卡片生成平台

## 项目概述

GameCard 是一个**配置驱动的游戏卡片生成平台**。用户选择模板、填写卡片属性（名称、属性、技能、数值等）、上传角色图片，即可生成一张风格统一的游戏卡片（PNG/JPG/WEBP 导出）。

核心特色：
- **主题自动匹配**：选择属性类型（如火/水/草），卡片自动切换对应的颜色、背景、图标
- **配置驱动扩展**：新增模板只需创建 JSON + SVG 文件，无需修改代码
- **AI 辅助**：自动抠图（Python rembg 微服务）
- **预设系统**：预设技能和角色简介，一键填充

---

## 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 后端框架 | Spring Boot | 4.0.7 |
| Java | JDK | 21 |
| ORM | MyBatis-Plus | 3.5.10.1 |
| 数据库 | MySQL | 8.x |
| 前端框架 | Vue 3 (Composition API + `<script setup>`) | 3.5+ |
| 前端构建 | Vite | 6.x |
| 状态管理 | Pinia | 2.1+ |
| 路由 | Vue Router | 4.4+ |
| CSS | Tailwind CSS + 自定义 Design Tokens | 4.x |
| 类型检查 | TypeScript + vue-tsc | 5.6 / 2.1 |
| HTTP 客户端 | Axios | 1.7 |
| 导出 | html-to-image | 1.11 |
| Python 微服务 | FastAPI + rembg | — |

---

## 项目结构

```
GameCard/
├── CLAUDE.md                        # ← 本文件 — AI 项目指南
├── TEMPLATE-GUIDE.md                 # 模板创建指南（重要参考）
├── pom.xml                           # Maven 构建（Spring Boot 4.x）
│
├── src/main/java/com/gamecard/
│   ├── GameCardApplication.java      # 启动类
│   │
│   ├── common/                       # 公共模块
│   │   ├── constant/Constants.java   # 常量定义
│   │   ├── properties/               # 配置属性
│   │   │   ├── AiProperties.java     # AI 配置（预留）
│   │   │   └── BackgroundRemovalProperties.java  # 抠图服务配置
│   │   └── result/
│   │       ├── Result.java           # 统一 API 返回值
│   │       └── ResultCode.java       # 状态码枚举
│   │
│   ├── config/
│   │   ├── WebMvcConfig.java         # CORS + Web 配置
│   │   └── MyBatisPlusConfig.java    # MyBatis-Plus 配置
│   │
│   ├── controller/
│   │   ├── HealthController.java     # GET /api/health
│   │   └── ImageController.java      # POST /api/image/remove-background
│   │
│   ├── exception/
│   │   └── BusinessException.java    # 业务异常
│   │
│   ├── handler/
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   │
│   ├── utils/                        # 工具类（部分 TODO）
│   │   ├── FileUtil.java
│   │   ├── IdUtil.java
│   │   ├── ImageUtil.java
│   │   └── JsonUtil.java
│   │
│   ├── ai/                           # AI 模块（预留）
│   │   ├── AIClient.java             # AI 客户端接口
│   │   ├── VisionService.java        # 视觉服务
│   │   └── TextService.java          # 文本服务
│   │
│   ├── export/
│   │   └── ExportService.java        # 导出服务接口（TODO）
│   │
│   ├── upload/
│   │   └── UploadService.java        # 文件上传接口（TODO）
│   │
│   ├── background/
│   │   └── RemoveBackgroundService.java  # 调用 Python 抠图服务
│   │
│   └── template/                     # ★ 模板模块（核心）
│       ├── model/                    # 数据模型
│       │   ├── Template.java         # 核心模板模型
│       │   ├── TemplateField.java    # 字段定义
│       │   ├── TemplateSize.java     # 卡片尺寸
│       │   ├── TemplateAssets.java   # 资源定义
│       │   ├── TemplateMetadata.java # 元数据
│       │   ├── ThemeMappingItem.java # 主题映射条目
│       │   ├── PresetSkill.java      # 预设技能
│       │   └── PresetIntroduction.java  # 预设角色简介
│       ├── loader/
│       │   ├── TemplateLoader.java   # 加载器接口
│       │   ├── LocalTemplateLoader.java   # 本地文件加载（启动扫描）
│       │   └── JsonTemplateReader.java    # Jackson 3.x JSON 解析
│       ├── manager/
│       │   ├── TemplateManager.java       # 管理器接口
│       │   └── TemplateManagerImpl.java   # 缓存 + 生命周期
│       ├── service/
│       │   ├── TemplateService.java       # 服务接口
│       │   └── TemplateServiceImpl.java   # 服务实现
│       └── controller/
│           └── TemplateController.java    # REST API
│
├── src/main/resources/
│   ├── application.yml               # 主配置
│   ├── application-dev.yml           # 开发配置（MySQL + 抠图服务）
│   ├── application-prod.yml          # 生产配置
│   ├── templates/                    # 模板定义（classpath 扫描）
│   │   └── {template-id}/
│   │       └── template.json         # ★ 模板 JSON 定义
│   └── static/templates/             # 模板静态资源
│       └── {template-id}/
│           ├── backgrounds/          # 背景 SVG
│           ├── icons/                # 图标 SVG
│           └── frames/               # 边框 SVG
│
├── frontend/
│   ├── package.json                  # Node 依赖
│   ├── vite.config.ts                # Vite 配置
│   ├── tsconfig.json                 # TypeScript 配置
│   └── src/
│       ├── main.ts                   # 入口（createApp + Pinia + Router）
│       ├── App.vue                   # 根组件（Header + RouterView + Footer）
│       ├── style.css                 # 全局样式
│       ├── styles/tokens.css         # ★ Design Tokens（色板、字体、间距）
│       │
│       ├── router/index.ts           # 路由配置
│       │   ├── / → Home.vue
│       │   ├── /templates → TemplatesPage.vue
│       │   ├── /editor → EditorPage.vue
│       │   └── /works → WorksPage.vue
│       │
│       ├── types/index.ts            # ★ 所有 TypeScript 类型定义
│       ├── stores/card.ts            # ★ Pinia 全局状态（模板选择 + 卡片数据 + 图片配置 + localStorage 持久化）
│       │
│       ├── api/
│       │   ├── template.ts           # 模板 API（fetchTemplates, fetchTemplate）
│       │   └── image.ts              # 图片 API（removeBackground）
│       │
│       ├── adapter/                  # ★ 数据适配层（外部数据 → 模板标准格式）
│       │   ├── types.ts              # CardViewModel + CardDataAdapter 接口
│       │   ├── pokemon-adapter.ts    # 宝可梦数据适配器（参考实现）
│       │   └── index.ts              # 导出
│       │
│       ├── utils/themeResolver.ts    # ★ 主题解析器（resolveTheme + getAssetUrl + parseSkills）
│       │
│       ├── export/                   # 导出模块
│       │   ├── types.ts              # 导出选项类型
│       │   ├── CardExporter.ts       # html-to-image 封装
│       │   └── ExportService.ts      # 导出编排（验证 + 字体等待 + 下载触发）
│       │
│       └── components/
│           ├── common/               # 通用组件
│           │   ├── AppHeader.vue     # 导航栏
│           │   ├── AppFooter.vue     # 页脚
│           │   ├── AppButton.vue     # 按钮
│           │   ├── AppCard.vue       # 卡片容器
│           │   ├── EmptyState.vue    # 空状态
│           │   └── Loading.vue       # 加载状态
│           │
│           ├── card/                 # ★ 卡片渲染组件（1280×720 固定尺寸）
│           │   ├── CardRenderer.vue  # ★ 主渲染器（编排所有子组件）
│           │   ├── CardContainer.vue # 卡片容器（CSS 变量注入）
│           │   ├── CardBackground.vue# 背景（渐变色 + 主题 SVG + 辉光效果）
│           │   ├── CardHeader.vue    # 头部（名称 + 属性标签 + CP/LV 徽章）
│           │   ├── CardImage.vue     # 图片（拖拽定位 + 缩放 + 形状裁剪）
│           │   ├── CardStats.vue     # 属性统计（雷达图 + 数值条）
│           │   ├── CardSkills.vue    # 技能列表（左侧面板）
│           │   └── CardDescription.vue# 描述文字
│           │
│           ├── editor/               # ★ 编辑器组件（自动根据模板字段渲染）
│           │   ├── CardEditor.vue    # 属性面板（支持所有字段类型）
│           │   ├── PresetSkillSelector.vue    # 预设技能选择器
│           │   └── PresetIntroSelector.vue    # 预设简介选择器
│           │
│           ├── preview/
│           │   └── CardPreview.vue   # 画布预览（自适应缩放 + 图片拖拽）
│           │
│           ├── template/
│           │   └── TemplateSelector.vue  # 模板选择器
│           │
│           ├── image/
│           │   ├── ShapeSelector.vue # 图片形状选择
│           │   ├── ImageEditor.vue   # 图片编辑（拖拽 + 缩放 + 裁剪）
│           │   └── BackgroundRemover.vue # AI 抠图触发器
│           │
│           ├── upload/
│           │   └── ImageUploader.vue # 图片上传
│           │
│           └── export/
│               └── ExportButton.vue  # 导出按钮
│
└── python-image-service/             # Python 抠图微服务
    ├── app.py                        # FastAPI 应用（POST /api/remove-background）
    └── requirements.txt              # fastapi, uvicorn, rembg, Pillow
```

---

## 核心架构与数据流

### 请求流程

```
用户操作
  │
  ▼
Vue Router → EditorPage.vue
  │
  ├─ TemplateSelector.vue → onMounted → store.loadTemplates()
  │     └─ Backend: TemplateController → TemplateServiceImpl → TemplateManagerImpl → LocalTemplateLoader → JSON 解析
  │
  ├─ ImageUploader.vue → 上传图片（暂存为 blob URL）
  │     └─ BackgroundRemover.vue → POST /api/image/remove-background
  │           └─ Backend: ImageController → RemoveBackgroundService → Python FastAPI → rembg
  │
  ├─ CardEditor.vue → 按 template.fields 动态渲染表单
  │     └─ 双向绑定 store.cardData[key]
  │
  └─ CardPreview.vue → resolveTheme(template, cardData)
        └─ CardRenderer → 7 个子组件按配置渲染
```

### 模板驱动渲染

```
template.json
  ├── fields[]          → CardEditor 自动渲染表单
  ├── themeField        → 决定用 cardData 中哪个字段做主题键
  ├── themeMapping{}    → 字段值 → 视觉主题（颜色、背景、图标）
  ├── statFields[]      → CardStats 显示哪些属性
  ├── presetSkills[]    → PresetSkillSelector 快速添加技能
  └── presetIntroductions[] → PresetIntroSelector 一键填充描述
```

### 主题解析（核心机制）

`themeResolver.ts` 中的 `resolveTheme()` 函数：
1. 读取 `template.themeField`（如 `"nature"`）
2. 从 `cardData` 获取值（如 `"暴躁"`）
3. 在 `template.themeMapping["暴躁"]` 中查找主题
4. 返回 `ResolvedTheme`（color, icon, background, cssVars）
5. 所有 Card* 组件渲染时使用 theme 中的值

**重要原则**：前端组件中**禁止写** `if (type === "fire")` 这样的条件判断，所有主题差异通过 `themeMapping` 配置驱动。

---

## 关键 API

### 后端 REST API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/health` | 健康检查 |
| GET | `/api/templates` | 获取所有模板列表 |
| GET | `/api/templates/{id}` | 获取模板详情 |
| POST | `/api/image/remove-background` | AI 抠图（multipart/form-data） |

### 统一返回格式

```json
{ "code": 200, "message": "操作成功", "data": { ... } }
```

---

## 重要设计原则

1. **配置驱动**：所有模板差异通过 `template.json` 配置，无需修改 Java/Vue 代码
2. **零硬编码主题**：禁止在组件中写属性相关的 if/else，必须通过 `themeMapping` 驱动
3. **关注点分离**：后端负责数据提供，前端负责渲染，模板系统桥接两者
4. **渐进增强**：presetSkills/presetIntroductions 为可选字段，不影响旧模板
5. **语义中立**：字段 key 使用通用名称（`nature` 而非 `pokemon_type`），便于跨模板复用
6. **数据与展示分离**：通过 CardDataAdapter 接口将外部数据格式转换为模板标准格式

---

## 常用开发命令

```bash
# 后端
mvn spring-boot:run                    # 启动后端（默认 8080）

# 前端
cd frontend
npm install                             # 安装依赖
npm run dev                             # 启动开发服务器（Vite）
npm run build                           # 生产构建
npm run preview                         # 预览构建结果

# Python 抠图服务
cd python-image-service
pip install -r requirements.txt
python app.py                           # 启动（默认 8001）
```

---

## 模板字段类型对照

| 字段类型 | 编辑器渲染 | 卡片渲染位置 |
|---|---|---|
| `TEXT` | `<input>` | 视字段 key 而定 |
| `TEXTAREA` | `<textarea>` | CardDescription（key=description） |
| `NUMBER` | `<input type="number">` | CardHeader 徽章 / CardStats 数值条 |
| `ENUM` | 按钮组 | CardHeader 属性标签 |
| `SELECT` | `<select>` | 视字段 key 而定 |
| `ARRAY` | 动态列表 + 添加/删除 | CardSkills 技能列表（key=skills） |
| `COLOR` | `<input type="color">` | 视字段 key 而定 |
| `IMAGE` | 左侧上传区域 | CardImage |

## 关键约束

- **statFields 雷达图**：恰好 6 项时显示雷达图 + 数值条；1-5 或 7+ 项仅显示数值条
- **themeMapping**：key 必须与 themeField 对应字段的 options 完全一致（区分大小写）
- **template.json id**：必须与目录名完全一致
- **卡片尺寸**：固定为 1280×720（横版 HUD 布局）
- **导出格式**：PNG/JPG/WEBP，默认 2x 像素倍率

## 当前 TODO

- `FileUtil.java` — 文件扩展名获取、删除
- `ImageUtil.java` — 图片尺寸获取、缩放
- `IdUtil.java` — 雪花算法 ID 生成（已预留 MyBatis-Plus 配置）
- `ExportService.java` — 后端导出服务（前端已实现前端导出）
- `UploadService.java` — 文件上传服务（目前使用 blob URL 在前端处理）
- AI 模块（AIClient, TextService, VisionService）— 已预留接口和配置
- 数据库相关功能（目前模板从 JSON 文件加载，尚未持久化卡片数据）

## 环境配置

### 开发环境（application-dev.yml）

- MySQL: `localhost:3306/gamecard` (root/root)
- 抠图服务: `http://localhost:8001`
- MyBatis-Plus: 控制台 SQL 日志 + 雪花算法主键 + 逻辑删除

### 配置文件

```yaml
# application.yml — 主配置
ai:
  enabled: false          # AI 功能开关（预留）

# application-dev.yml — 开发配置
bg-remover:
  enabled: true           # 抠图功能开关
  service-url: http://localhost:8001
  timeout-ms: 120000
```

---

## 如何新增一个模板

详见 `TEMPLATE-GUIDE.md`，简要步骤：

1. 创建 `src/main/resources/templates/{your-id}/template.json`
2. 创建 `src/main/resources/static/templates/{your-id}/backgrounds/` 等素材目录
3. 编写 template.json（参考现有 `pokemon-template`）
4. 重启后端（LocalTemplateLoader 启动时扫描）

无需修改任何 Java 或 Vue 代码。

---

## 数据适配层

对于需要从外部数据源（API、DB 导入）创建卡片的场景，可以实现 `CardDataAdapter` 接口：

```typescript
interface CardDataAdapter<T = Record<string, unknown>> {
  readonly id: string
  readonly name: string
  transform(raw: T): CardViewModel  // 转换为模板标准格式
  canHandle(raw: unknown): raw is T  // 类型守卫
}
```

参考实现：`frontend/src/adapter/pokemon-adapter.ts`（宝可梦数据 → 模板标准格式）

---

> 首次阅读建议：先浏览 `frontend/src/types/index.ts` 了解所有类型定义，然后阅读 `TEMPLATE-GUIDE.md` 理解模板系统，最后对照 `CardRenderer.vue` 理解渲染流程。
