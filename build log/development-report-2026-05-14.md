# 开发报告 — 2026-05-14

## 项目概述

基于现有 Spring Boot 后端项目（`backend`），搭建前端项目并完成前后端分离架构的前端初始化开发。前端采用 Vue 3 + TypeScript + Vite 技术栈，UI 设计对齐后端 `src/main/resources/static` 中的原生 HTML 页面风格。

---

## 1. 项目结构初始化

### 1.1 创建 Vue 项目

在 `frontend/` 目录下使用 `npm create vue@latest` 创建 Vue 3 脚手架项目（`vue-project`）。

**技术栈：**
- Vue 3.5 + Composition API + `<script setup>`
- TypeScript 6.0
- Vite 8.0
- Vue Router 5.0
- Pinia 3.0（状态管理）
- Axios 1.7（HTTP 客户端）

### 1.2 安装额外依赖

```json
{
  "tailwindcss": "^4.1.0",
  "@tailwindcss/vite": "^4.1.0",
  "chart.js": "^4.4.8",
  "sweetalert2": "^11.14.0",
  "axios": "^1.7.0"
}
```

---

## 2. 后端 API 分析

阅读并梳理了后端 `com.pcadvisor` 包下的全部代码：

### 2.1 控制器端点

| 控制器 | 路径前缀 | 端点 |
|--------|---------|------|
| `AuthController` | `/api/auth` | `POST /login`, `POST /logout`, `POST /register`, `GET /check-username`, `GET /check-email` |
| `HelloController` | `/api` | `GET /hello`, `GET /hardware` |
| `TestController` | `/api/test` | `GET /db`, `GET /tables`, `GET /users`, `GET /hardware` |
| `UserController` | `/user` | `GET /test`, `GET /{id}`, `POST /`, `GET /list` |

### 2.2 实体与数据模型

- **用户体系**: `User` (sys_user), `OrdinaryUser`, `Administrator`
- **硬件体系**: `HardwareInf`, `CpuInf`, `MotherboardInf`, `GraphicsCardInf`
- **评测体系**: `Evaluation`, `Comment`, `Collect`, `MediaInf`
- **DTO/VO**: `LoginRequest/Response`, `RegisterRequest`, `UserDTO`, `UserQueryDTO`, `EvaluationDTO`, `HardwareQueryDTO`, `RecommendationRequest`, `UserVO`, `UserDetailVO`, `HardwareVO`, `RecommendationVO`

### 2.3 安全配置

- 无状态 Session + JWT 认证
- `JwtAuthenticationFilter` 从 `Authorization: Bearer <token>` 提取并验证令牌
- CORS 全局开放
- 公开路径：`/api/auth/**`, `/api/test/**`, `/api/hello`, `/api/hardware`, `/user/test` 及静态资源

### 2.4 配置

```yaml
server.port: 8080
server.servlet.context-path: /pcadvisor
spring.datasource: mysql://localhost:3306/pcadvisor
mybatis-plus: 下划线转驼峰 + SQL 日志输出
```

---

## 3. 前端开发内容

### 3.1 类型定义 (`src/types/index.ts`)

完整映射后端所有实体、DTO、VO、枚举，共 30+ 个 TypeScript 接口：
- API 通用响应 `Result<T>`
- 认证相关：`LoginRequest`, `LoginResponse`, `RegisterRequest`
- 用户相关：`User`, `OrdinaryUser`, `Administrator`, `UserDTO`, `UserVO`, `UserDetailVO`
- 硬件相关：`HardwareInf`, `CpuInf`, `MotherboardInf`, `GraphicsCardInf`, `HardwareQueryDTO`, `HardwareVO`
- 评测/评论/收藏/媒体：`Evaluation`, `Comment`, `Collect`, `MediaInf`
- 推荐系统：`RecommendationRequest`, `RecommendationVO`
- 分页：`PageResult<T>`

### 3.2 HTTP 客户端 (`src/api/`)

| 文件 | 说明 |
|------|------|
| `request.ts` | Axios 实例，baseURL=`/api`，请求拦截器自动携带 JWT Token，响应拦截器统一处理 401 跳转 |
| `auth.ts` | `login()`, `register()`, `logout()`, `checkUsername()`, `checkEmail()` |
| `user.ts` | `getUserById()`, `addUser()`, `listUsers()`, `userTest()` |
| `common.ts` | `hello()`, `getHardware()`, `testDb()`, `testTables()`, `testUsers()`, `testHardware()` |

### 3.3 状态管理 (`src/stores/`)

| Store | 说明 |
|-------|------|
| `auth.ts` | Token 持久化（localStorage），用户信息，登录/注册/登出逻辑，`isLoggedIn`/`isAdmin` 计算属性 |
| `user.ts` | 用户列表 CRUD，加载状态 |

### 3.4 路由配置 (`src/router/index.ts`)

```
/            → 重定向到 /home
/login       → LoginView（guest 路由）
/register    → RegisterView（guest 路由）
/home        → MainLayout > HomeView
/hardware    → MainLayout > HardwareView
/admin       → MainLayout > AdminDashboardView（需 ADMIN 角色）
```

**路由守卫逻辑：**
- 未登录访问需认证页面 → 跳转 `/login`
- 已登录访问 guest 页面 → 跳转 `/home`
- 访问 admin 路由需 ADMIN 角色 → 否则跳转 `/home`

### 3.5 Vite 代理配置

```ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080/pcadvisor',
      changeOrigin: true,
    },
  },
}
```

前端开发服务器将 `/api/*` 请求代理到后端 `http://localhost:8080/pcadvisor/api/*`，解决跨域问题。

### 3.6 UI 设计（对齐 static 页面）

#### 设计系统

| 属性 | 值 |
|------|-----|
| 主色 | `#165DFF`（蓝色） |
| 辅色 | `#FF7D00`（橙色） |
| 背景色 | `#F5F7FA`（浅灰） |
| 字体 | PingFang SC, Microsoft YaHei |
| CSS 框架 | Tailwind CSS v4 |
| 图标库 | Font Awesome 4.7 CDN |
| 弹窗库 | SweetAlert2 |
| 图表库 | Chart.js |

#### 页面实现

| 页面 | 文件 | 说明 |
|------|------|------|
| 主布局 | `layouts/MainLayout.vue` | 顶部导航栏（Logo + 搜索 + 登录状态 + 桌面/移动导航）+ 页脚（四列布局） |
| 登录页 | `views/LoginView.vue` | 居中卡片 + 渐变顶部装饰条，密码显隐切换，记住我，SweetAlert2 反馈，管理员快捷登录按钮 |
| 注册页 | `views/RegisterView.vue` | 与登录页风格一致，五个字段校验（用户名/密码/确认密码/邮箱/手机号） |
| 首页 | `views/HomeView.vue` | 轮播图 + 硬件分类导航 + 热门推荐商品卡片（4列）+ 排行榜（带Tab切换）+ 硬件资讯 + Chart.js 价格趋势图 + 新品上市（6列）+ 返回顶部按钮 |
| 硬件浏览 | `views/HardwareView.vue` | 搜索框 + 类型标签筛选 + 下拉筛选 + 商品卡片网格 |
| 管理后台 | `views/admin/DashboardView.vue` | 统计卡片（数据库/数据表/用户数）+ 数据表标签 + 快捷操作 + 用户列表表格 |

---

## 4. 文件清单

```
frontend/vue-project/src/
├── types/index.ts               # 全部 TypeScript 类型定义
├── api/
│   ├── request.ts               # Axios 实例 + 拦截器
│   ├── auth.ts                  # 认证 API
│   ├── user.ts                  # 用户 API
│   └── common.ts                # 通用/测试 API
├── stores/
│   ├── auth.ts                  # 认证状态
│   └── user.ts                  # 用户状态
├── router/index.ts              # 路由 + 守卫
├── layouts/MainLayout.vue       # 主布局（顶栏+页脚）
├── views/
│   ├── LoginView.vue            # 登录页
│   ├── RegisterView.vue         # 注册页
│   ├── HomeView.vue             # 首页
│   ├── HardwareView.vue         # 硬件浏览
│   └── admin/DashboardView.vue  # 管理后台
├── assets/main.css              # Tailwind + 全局样式
├── App.vue                      # 根组件
└── main.ts                      # 入口
```

---

## 5. 启动方式

```bash
# 1. 启动后端（Spring Boot，端口 8080）
cd backend
./mvnw spring-boot:run

# 2. 安装前端依赖并启动
cd frontend/vue-project
npm install
npm run dev

# 3. 访问 http://localhost:5173
```

---

## 6. 待完成事项

- [ ] 后端评测、评论、收藏、推荐等功能的 API 接口尚未实现（实体和 DTO 已定义）
- [ ] 前端对应的评测详情页、评测发布页、个人中心页
- [ ] 硬件详情页
- [ ] 用户头像上传
- [ ] 响应式适配的进一步优化
- [ ] 单元测试和 E2E 测试
