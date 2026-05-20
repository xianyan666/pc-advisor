# 开发报告 — 2026-05-17

## 概述

本日主要完成三方面工作：
1. **硬件产品图片数据库存储** — 图片路径存 MySQL，磁盘文件由 Spring Boot 静态资源服务
2. **硬件浏览界面重构** — 按类型展示参数、引导至评测详情
3. **前后端数据联通修复** — 解决 API 响应格式、字段命名不一致等问题

---

## 1. 硬件图片存储方案

### 方案选型

选择 **方案 A**：图片存磁盘，数据库只存路径。

| 对比 | 方案 A（磁盘+路径） | 方案 B（BLOB 存二进制） |
|------|---------------------|-------------------------|
| 性能 | 静态资源直接返回 | 经过 DB + Java 内存 |
| 维护 | 图片文件独立管理 | DB 备份体积大 |
| 实现 | 上传接口 + 静态映射 | Base64 编解码 |

### 实现

| 文件 | 变更 |
|------|------|
| `entity/MediaInf.java` | 新增 `hardwareId` 字段 + `hardwareInf` 关联对象 |
| `mapper/HardwareInfMapper.java` | **新建** MyBatis-Plus Mapper |
| `mapper/MediaInfMapper.java` | **新建** MyBatis-Plus Mapper |
| `resources/static/images/products/` | 29 张 webp 图片复制到位 |
| `resources/init.sql` | 完整重写（详见第 3 节） |

### 数据流

```
浏览器 /images/products/xxx.webp
  → Vite 代理 → http://localhost:8080/pcadvisor/images/products/xxx.webp
  → Spring Boot 静态资源 → 返回图片
```

---

## 2. 数据库重构

### 2.1 init.sql 完整重写

原 `init.sql` 仅创建 3 张用户表。本次新增：

| 表名 | 说明 |
|------|------|
| `Hardware_Inf` | 硬件信息主表 |
| `CPU_Inf` | CPU 详情（核心/线程/频率/TDP） |
| `GraphicsCard_Inf` | 显卡详情（核心型号/显存/功耗） |
| `Motherboard_Inf` | 主板详情（板型/插槽/M.2） |
| `Evaluation` | 评测表 |
| `Comment` | 评论表 |
| `Collect` | 收藏表 |
| `Media_Inf` | 媒体资源表（通用：硬件/评测/评论） |

### 2.2 种子数据

插入 15 条硬件记录及其详情参数：

| 类型 | 数量 | 产品示例 |
|------|------|---------|
| CPU | 6 条 | Intel 酷睿 i9-14900K, AMD Ryzen 9 9950X3D, ... |
| 显卡 | 5 条 | NVIDIA GeForce RTX 5090, 七彩虹 RTX 5080, ... |
| 主板 | 4 条 | 华硕 ROG Maximus Z790/Z890, 微星 MPG Z790/Z890 |

14 条图片关联记录（`INSERT...SELECT` 自动匹配硬件 ID）。

---

## 3. 后端 API 改进

### 3.1 TestController 增强

| 变更 | 说明 |
|------|------|
| SQL 显式列名 + AS 别名 | 数据库 snake_case → 前端 PascalCase（`hardware_name AS HardwareName`） |
| LEFT JOIN 详情表 | 单次查询返回 CPU/显卡/主板完整参数 |
| 评测计数子查询 | `evaluationCount` 字段 |
| try-catch 容错回退 | 详情表不存在时自动降级为基础查询 |
| Result\<T\> 包装 | 统一 `{ code: 200, data: [...], message: "操作成功" }` 返回格式 |

### 3.2 字段别名对照

```
数据库列名              → API JSON key（前端期望）
─────────────────────────────────────────────────
hardware_name           → HardwareName
hardware_type           → HardwareType
hardware_brand          → HardwareBrand
hardware_model          → HardwareModel
hardware_price          → HardwarePrice
media_url               → imageUrl
core_count / vram_cap … → coreCount / vramCap …
```

### 3.3 HelloController

`/api/hello` 和 `/api/hardware` 改用 `HardwareInfMapper` + `MediaInfMapper` 查询真实数据，同样用 `Result<T>` 包装返回。

---

## 4. 前端开发

### 4.1 图片映射工具

**新建 `src/utils/imageMapping.ts`** — 29 个硬件产品名 → 图片路径的映射表。

```ts
// 长关键词优先匹配，避免 "RX 9070" 抢先匹配 "RX 9070 XT"
'RX 9070 XT' → '/images/products/Radeon RX 9070 XT.webp'
'RX 9070'    → '/images/products/Radeon RX 9070.webp'
```

### 4.2 首页改造 (HomeView.vue)

| 变更 | 说明 |
|------|------|
| 轮播图/热门/排行/新品 | 全部替换 picsum 占位图为真实硬件图片 |
| 硬件分类精简 | 8 类 → 3 类（CPU / 显卡 / 主板） |
| 分类按钮可点击 | 跳转 `/hardware?type=CPU` 等，自动筛选 |
| 产品卡片可点击 | 通过名称 query 参数跳转详情页 |
| 硬件名称修正 | 修复 10 处命名错误（空格缺失、型号不存在等） |

### 4.3 硬件浏览页重构 (HardwareView.vue)

**卡片设计**：从简单图片+价格升级为信息卡片。

```
┌──────────────────────────┐
│   [产品图片]              │
│   [CPU 处理器] [3篇评测]  │
├──────────────────────────┤
│  Intel 酷睿 i9-14900K    │
│  Intel · BX8071514900K   │
│  ┌──────┬──────┬──────┐  │
│  │ 核心 │ 频率 │ TDP  │  │  ← 按类型动态显示
│  │ 24核 │ 3.2  │125W  │  │     CPU: 核心/频率/TDP
│  └──────┴──────┴──────┘  │     显卡: 显存/核心/功耗
│  ¥4299    [评测] [♥]    │     主板: 板型/内存槽/M.2
└──────────────────────────┘
```

- 卡片点击 → 进入硬件详情页
- "评测"按钮 → 跳转详情页评测区
- 支持 URL query 参数 (`?type=CPU`) 初始化筛选
- 加载骨架屏动画

### 4.4 硬件详情页 (HardwareDetailView.vue) — 新建

| 区域 | 内容 |
|------|------|
| 面包屑导航 | DIY硬件 > 硬件名称 |
| 基本信息卡 | 全尺寸图片 + 名称/品牌/型号 + 价格 |
| 规格参数表 | 按类型显示全部参数（CPU 6项 / 显卡 5项 / 主板 5项） |
| 操作按钮 | 收藏 / 加入对比 |
| 相关评测区 | 评测数量 + 评测列表占位（后续对接 API） |

支持两种查找方式：
- `route.params.id` — 数字 ID 精确查找
- `route.query.name` — 双向名称匹配（DB 名包含查询名 OR 查询名包含 DB 名）

### 4.5 API 层修复

**`src/api/common.ts`** — 新增 `unwrap()` 函数，自动从 `{ code:200, data:[...] }` 中提取 `data` 字段，兼容 Result 包装和原始返回。

### 4.6 Vite 配置

```ts
proxy: {
  '/api':    { target: 'http://localhost:8080/pcadvisor' },
  '/images': { target: 'http://localhost:8080/pcadvisor' },  // 新增
}
```

### 4.7 路由

```ts
/hardware          → HardwareView.vue      // 硬件列表
/hardware/:id      → HardwareDetailView.vue // ID 查找
/hardware-detail   → HardwareDetailView.vue // 名称查找（?name=xxx）
```

---

## 5. 问题排查记录

| 问题 | 根因 | 修复 |
|------|------|------|
| 详情页显示"未找到该硬件" | DB 无硬件表，SQL LEFT JOIN 失败 | 创建完整表结构 + 种子数据 |
| API 返回数据但前端显示空 | `JdbcTemplate` 返回 snake_case key，前端用 PascalCase 访问 | SQL 显式 AS 别名转换 |
| 响应被拦截器拒掉 | 后端返回裸数组无 `code` 字段 | Controller 统一用 `Result<T>` 包装 |
| "RX 9070 XT" 匹配错误图片 | 短关键词 "RX 9070" 先匹配 | imageMapping 按长度降序排列 |
| 首页产品名不一致 | 空格缺失 / 型号不存在 | 逐项校对修正 |

---

## 6. 今日修改文件清单

```
后端 (6 文件)
├── entity/MediaInf.java               # 新增 hardwareId/hardwareInf 字段
├── mapper/HardwareInfMapper.java       # 新建
├── mapper/MediaInfMapper.java          # 新建
├── controller/HelloController.java     # Result 包装 + 真实数据查询
├── controller/TestController.java      # 丰富 SQL + 别名 + 容错
└── resources/init.sql                  # 完整重写（7 表 + 15 条种子）

前端 (8 文件)
├── src/utils/imageMapping.ts           # 新建：硬件名→图片路径映射
├── src/views/HomeView.vue              # 真实图片 + 分类精简 + 名称修正 + 点击导航
├── src/views/HardwareView.vue          # 完整重构：参数卡片 + 评测入口 + 筛选
├── src/views/HardwareDetailView.vue    # 新建：硬件详情页
├── src/api/common.ts                   # unwrap() 兼容 Result 包装
├── src/router/index.ts                 # 新增 /hardware/:id /hardware-detail
├── src/types/index.ts                  # 精简 HardwareType
└── vite.config.ts                      # 新增 /images 代理

资源
└── static/images/products/             # 29 张 webp 图片
```

---

## 7. 启动步骤

```bash
# 1. 数据库初始化（首次执行，会重建所有表）
#    在 MySQL 中执行 backend/src/main/resources/init.sql

# 2. 启动后端
cd backend
./mvnw spring-boot:run

# 3. 启动前端
cd frontend/vue-project
npm run dev

# 4. 访问 http://localhost:5173
```

---

## 8. 待完成

- [ ] 评测功能完整 API（Evaluation/Comment 的 CRUD）
- [ ] 硬件详情页对接真实评测数据
- [ ] 收藏功能对接
- [ ] 硬件对比功能
- [ ] 硬件搜索功能与后端搜索 API 对接
- [ ] 用户个人中心页面
- [ ] 硬件图片上传功能（目前为手动管理）
