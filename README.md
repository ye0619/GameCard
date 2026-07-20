# GameCard — 游戏卡片生成平台

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.7-brightgreen)
![JDK](https://img.shields.io/badge/JDK-21-blue)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D)
![Vite](https://img.shields.io/badge/Vite-6.0-646CFF)
![License](https://img.shields.io/badge/License-MIT-yellow)

> **一款配置驱动的游戏卡片生成平台**  
> 选择模板、填写属性、上传图片，即可快速生成风格统一的游戏角色卡片，支持 PNG / JPG / WEBP 导出。

---

## 📋 目录

- [✨ 功能特性](#-功能特性)
- [🖼️ 效果展示](#️-效果展示)
- [🗺️ 项目架构](#️-项目架构)
- [🧱 技术栈](#-技术栈)
- [📂 项目结构](#-项目结构)
- [🚀 快速开始](#-快速开始)
- [🎨 模板系统](#-模板系统)
- [🔌 API 文档](#-api-文档)
- [🛣️ 开发路线图](#️-开发路线图)
- [📄 许可证](#-许可证)

---

## ✨ 功能特性

### 🎴 卡片生成
- **多模板支持** — 宝可梦图鉴、博德之门风格等多种模板自由切换
- **主题自动匹配** — 选择属性类型（如火/水/草），卡片自动切换对应的配色、背景和图标
- **实时预览** — 编辑即渲染，所见即所得
- **高清导出** — 支持 PNG / JPG / WEBP 格式，默认 2x 像素倍率

### 🤖 AI 辅助
- **自动抠图** — 集成 Python 微服务（rembg），上传图片一键去除背景
- **AI 预留接口** — 架构预留 AI 模块（文本生成、视觉识别），支持后续扩展

### ⚙️ 配置驱动
- **零代码扩展** — 新增模板只需创建 JSON 配置文件 + SVG 素材，无需修改后端或前端代码
- **灵活字段系统** — TEXT / NUMBER / ENUM / SELECT / ARRAY / COLOR / IMAGE 等丰富字段类型
- **预设系统** — 预设技能和角色简介，一键填充，提升创作效率

### 🌐 前后端分离
- 后端 Spring Boot REST API，前端 Vue 3 SPA
- CORS 跨域支持，便于本地开发和联调

---

## 🖼️ 效果展示

| 宝可梦图鉴模板 | 博德之门模板 | 正在完善中... |
|:---:|:---:|:---:|
| ![pokemon](https://via.placeholder.com/300x169?text=Pokemon) | ![baldurs](https://via.placeholder.com/300x169?text=Baldur%27s+Gate) | ![more](https://via.placeholder.com/300x169?text=Coming+Soon) |

> 卡片尺寸固定为 **1280×720** 横版 HUD 布局。

---

## 🗺️ 项目架构

```
┌───────────────────────────────────────────────────┐
│                   前端 (Vue 3 SPA)                  │
│  ┌─────────┐ ┌──────────┐ ┌───────────────────┐   │
│  │ 模板选择  │ │ 卡片编辑器 │ │ 卡片渲染器+预览    │   │
│  └────┬────┘ └────┬─────┘ └────────┬──────────┘   │
│       │           │                │               │
│  ┌────▼───────────▼────────────────▼──────────┐    │
│  │         Pinia 状态管理 + Axios              │    │
│  └────────────────┬───────────────────────────┘    │
└───────────────────┼───────────────────────────────┘
                    │ REST API
┌───────────────────▼───────────────────────────────┐
│                后端 (Spring Boot 4.x)               │
│  ┌──────────────┐ ┌──────────────┐                │
│  │ Controller   │ │ Service      │                │
│  └──────┬───────┘ └──────┬───────┘                │
│         │                │                         │
│  ┌──────▼────────────────▼───────┐                │
│  │  TemplateManager 缓存+生命周期  │                │
│  └──────┬────────────────────────┘                │
│         │                                         │
│  ┌──────▼───────┐                                 │
│  │ LocalTemplateLoader (JSON 扫描)                 │
│  └──────────────┘                                 │
└───────────────────┬───────────────────────────────┘
                    │ HTTP
┌───────────────────▼───────────────────────────────┐
│          Python 微服务 (FastAPI + rembg)            │
│         POST /api/remove-background — 自动抠图      │
└───────────────────────────────────────────────────┘
```

### 核心数据流

```
用户操作
  │
  ▼
Vue Router → EditorPage.vue
  │
  ├─ TemplateSelector → 加载模板列表 (GET /api/templates)
  │
  ├─ ImageUploader → 上传图片 → 可选 AI 抠图 (POST /api/image/remove-background)
  │
  ├─ CardEditor → 动态渲染表单 ← 双向绑定 Pinia store
  │
  └─ CardPreview → 主题解析 → CardRenderer (7 个子组件)
```

---

## 🧱 技术栈

| 层级 | 技术 | 版本 |
|:---|:---|---:|
| **后端框架** | Spring Boot | 4.0.7 |
| **Java** | JDK | 21 |
| **ORM** | MyBatis-Plus | 3.5.10.1 |
| **数据库** | MySQL | 8.x |
| **前端框架** | Vue 3 (Composition API + `<script setup>`) | 3.5 |
| **构建工具** | Vite | 6 |
| **状态管理** | Pinia | 2.1 |
| **路由** | Vue Router | 4.4 |
| **CSS** | Tailwind CSS 4 + Design Tokens | 4.0 |
| **类型检查** | TypeScript + vue-tsc | 5.6 / 2.1 |
| **HTTP** | Axios | 1.7 |
| **图片导出** | html-to-image | 1.11 |
| **AI 抠图** | FastAPI + rembg（Python 微服务） | — |

---

## 📂 项目结构

```
GameCard/
├── pom.xml                              # Maven 构建
├── CLAUDE.md                            # 项目 AI 指南
├── TEMPLATE-GUIDE.md                    # 模板创建指南
│
├── src/main/java/com/gamecard/
│   ├── GameCardApplication.java         # 启动类
│   ├── controller/                      # REST API 控制器
│   │   ├── HealthController.java        # GET /api/health
│   │   ├── TemplateController.java      # GET /api/templates
│   │   └── ImageController.java         # POST /api/image/remove-background
│   ├── template/                        # 模板核心模块
│   │   ├── model/                       # 数据模型（Template, TemplateField 等）
│   │   ├── loader/                      # JSON 解析与加载
│   │   ├── manager/                     # 缓存 + 生命周期管理
│   │   └── service/                     # 业务服务
│   ├── config/                          # 全局配置
│   ├── common/                          # 公共模块
│   └── background/                      # 抠图客户端
│
├── src/main/resources/
│   ├── application.yml                  # 主配置
│   ├── templates/                       # 模板 JSON 定义
│   │   ├── pokemon-template/
│   │   ├── baldurs-gate2/
│   │   ├── genshin/
│   │   └── minecraft/
│   └── static/templates/               # 模板静态资源（SVG 素材）
│
├── frontend/
│   ├── package.json
│   └── src/
│       ├── templates/                   # 模板前端布局组件
│       │   ├── pokemon-template/layout.vue
│       │   └── baldurs-gate2/layout.vue
│       ├── renderer/                    # 动态加载器
│       │   └── TemplateRenderer.vue
│       ├── components/                  # 通用组件
│       │   ├── card/                    # 卡片渲染组件
│       │   ├── editor/                  # 编辑器组件
│       │   ├── preview/                 # 预览组件
│       │   └── export/                  # 导出组件
│       ├── stores/                      # Pinia 状态
│       ├── types/                       # TypeScript 类型定义
│       ├── adapter/                     # 数据适配层
│       └── export/                      # 导出服务
│
└── python-image-service/               # Python 抠图微服务
    ├── app.py                           # FastAPI 应用
    └── requirements.txt
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本 |
|:---|---:|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 20+ |
| Python | 3.10+ |
| MySQL | 8.x |

### 1️⃣ 启动后端

```bash
# 克隆项目
git clone https://github.com/your-username/GameCard.git
cd GameCard

# 启动 Spring Boot（默认端口 8080）
mvn spring-boot:run
```

> 开发环境默认连接 `localhost:3306/gamecard`（用户 `root`，密码 `root`），可在 `application-dev.yml` 中修改。

### 2️⃣ 启动前端

```bash
cd frontend
npm install
npm run dev
```

> 前端开发服务器默认运行在 `http://localhost:5173`，已配置代理到后端 `8080` 端口。

### 3️⃣ 启动抠图服务（可选）

```bash
cd python-image-service
pip install -r requirements.txt
python app.py
```

> 服务运行在 `http://localhost:8001`，首次启动需下载模型，稍候片刻。

### 4️⃣ 打开浏览器

访问 `http://localhost:5173` 开始创作你的游戏卡片！

---

## 🎨 模板系统

本项目的核心特色——**配置驱动**的模板架构。

### 什么是模板？

一个模板 = `template.json`（定义字段、主题、尺寸）+ SVG 素材（背景、图标、边框）+ Vue 布局组件。

### 现有模板

| 模板 | ID | 类型 | 状态 |
|:---|---:|:---|:---:|
| 🐉 宝可梦图鉴 | `pokemon-template` | 经典 RPG 图鉴 | ✅ 已完成 |
| ⚔️ 博德之门 | `baldurs-gate2` | 奇幻冒险风格 | ✅ 已完成 |
| 🌟 原神 | `genshin` | 二次元风格 | ⏳ 素材待完善 |
| ⛏️ Minecraft | `minecraft` | 像素风格 | ⏳ 素材待完善 |

### 如何新增模板？

只需三步：

1. **创建 JSON** — 在 `src/main/resources/templates/{your-id}/template.json` 定义字段、主题映射和预设
2. **准备素材** — 在 `src/main/resources/static/templates/{your-id}/` 下放置 SVG 背景、图标、边框
3. **编写布局（可选）** — 在 `frontend/src/templates/{your-id}/layout.vue` 设计专属卡片布局

> 详细指南请参阅 [TEMPLATE-GUIDE.md](TEMPLATE-GUIDE.md)。

### 主题映射（核心机制）

模板通过 `themeMapping` 配置实现视觉主题切换，无需硬编码：

```json
{
  "themeField": "nature",
  "themeMapping": {
    "暴躁": {
      "color": "#EF4444",
      "background": "bg-aggressive",
      "icon": "aggressive",
      "styles": {
        "gradient-start": "#F97316",
        "gradient-end": "#DC2626"
      }
    },
    "温柔": {
      "color": "#3B82F6",
      "background": "bg-gentle",
      "icon": "gentle",
      "styles": {
        "gradient-start": "#60A5FA",
        "gradient-end": "#3B82F6"
      }
    }
  }
}
```

---

## 🔌 API 文档

### 统一返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

### 接口列表

| 方法 | 路径 | 说明 |
|:---|---:|:---|
| `GET` | `/api/health` | 健康检查 |
| `GET` | `/api/templates` | 获取所有模板 |
| `GET` | `/api/templates/{id}` | 获取模板详情 |
| `POST` | `/api/image/remove-background` | AI 抠图（multipart/form-data） |

---

## 🛣️ 开发路线图

### ✅ 已完成
- [x] 模板系统框架（JSON 驱动 + 动态加载）
- [x] 宝可梦图鉴模板（含性格主题切换）
- [x] 博德之门风格模板
- [x] AI 自动抠图（Python rembg 微服务）
- [x] 卡片前端导出（PNG / JPG / WEBP）
- [x] 预设技能和角色简介系统
- [x] 主题自动匹配与 CSS 变量注入
- [x] 数据适配层（CardDataAdapter）

### 🔄 进行中
- [ ] 模板 UI 素材精细化（高质量背景、边框、图标）
- [ ] 原神 / Minecraft 模板完善

### 📅 计划中
- [ ] 卡片数据持久化（MySQL 存储）
- [ ] 用户系统和作品管理
- [ ] AI 文本生成（角色简介、技能描述）
- [ ] 社区模板市场
- [ ] 批量导出 / 卡组功能

---

## ⚠️ 关于模板品质

由于个人能力与资源有限，**现有的模板素材（背景、边框、图标等）难以完全还原高质量商业游戏 UI 的视觉效果**。模板的视觉呈现目前仍处于"功能可用、视觉待完善"的阶段，部分预设素材仅为占位使用，色彩搭配和图形细节有待专业设计师的打磨。

**项目将在后续持续完善：**
- 不断优化和新增模板素材
- 探索 AI 辅助生成 UI 资源的可能性
- 欢迎社区贡献高质量的 SVG 模板素材

如果你对模板设计有好的想法或资源，欢迎提交 Issue 或 PR！

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

<p align="center">
  Made with ❤️ by GameCard Team
</p>
