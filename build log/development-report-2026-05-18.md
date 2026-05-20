# PC硬件推荐系统 — 开发报告

**日期**：2026-05-18

---

## 一、用户个人中心

### 新增文件
| 文件 | 说明 |
|------|------|
| `views/UserView.vue` | 用户个人页面，蓝白简约风格 |
| `api/user.ts` — `CollectedItem` / `EvaluationItem` 类型 | 收藏和评测数据接口 |
| `controller/UserProfileController.java` | 用户档案API（收藏列表、评测列表、收藏/取消收藏、发布评测） |
| `mapper/CollectMapper.java` | Collect 表映射器 |
| `mapper/EvaluationMapper.java` | Evaluation 表映射器 |
| `resources/seed_profile.sql` | 收藏+评测种子数据脚本 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `layouts/MainLayout.vue` | 导航栏用户名改为可点击，链接到个人中心；移动端菜单加入"个人中心"和"退出登录" |
| `router/index.ts` | 新增 `/user` 路由，`requiresAuth` 守卫；路由守卫重构 |
| `views/HardwareDetailView.vue` | 收藏按钮对接API，新增评测发布表单（标题/性能数据/使用体验/优缺点） |

---

## 二、图片上传系统

### 问题排查历程
1. **手动设置 `Content-Type: multipart/form-data`** → 缺少 boundary → 服务器无法解析
2. **移除手动 header** → axios 实例默认 `Content-Type: application/json` 覆盖 FormData → `Not a multipart request`
3. **移除 axios 实例默认 Content-Type** → 成功，axios 自动为 FormData 设置正确的 multipart header
4. **`file.transferTo(File)` 在 Windows 下失败** → 改用 `Files.copy(InputStream, Path)`
5. **运行时上传的图片无法访问** → 新增 `WebMvcConfig` 文件系统资源处理器

### 新增/修改文件
| 文件 | 说明 |
|------|------|
| `controller/FileUploadController.java` | `POST /api/upload` 和 `/api/upload/batch` |
| `config/WebMvcConfig.java` | 文件系统资源映射 `/images/evaluations/**` |
| `api/request.ts` | 移除默认 `Content-Type: application/json` |
| `api/user.ts` — `uploadImage()` | 图片上传函数 |
| `application.yml` | 增加 `spring.servlet.multipart` 配置（单文件10MB，总请求50MB） |

---

## 三、JWT Token 过期处理

### 问题
用户浏览器 localStorage 中存有已过期 3 天的 JWT token，后端 `validateToken()` 吞掉了 `ExpiredJwtException`，返回 403，前端只拦截 401。

### 修复
| 文件 | 改动 |
|------|------|
| `JwtUtil.java` | 新增 `parseToken()` 方法，向上抛出 `ExpiredJwtException` |
| `JwtAuthenticationFilter.java` | 直接调用 `parseToken()`，捕获 `ExpiredJwtException` 返回 401 JSON |
| `api/request.ts` | 错误拦截器同时处理 401 和 403 |

---

## 四、首页优化

### 改动
| 改动 | 说明 |
|------|------|
| 页脚精简 | 删除"关于我们/快速链接/帮助中心/联系我们"，仅保留版权行 |
| 硬件评测板块 | "硬件资讯"改为"硬件评测"，调用数据库评测数据 |
| 推荐榜 Tab 删除 | 移除"热门/新品/推荐"三按钮 |
| 链接修正 | "查看更多"→`/hardware`，"更多新品"→`/hardware`，"查看完整榜单"→`/ranking` |

---

## 五、评测详情页

### 新增
| 文件 | 说明 |
|------|------|
| `views/EvaluationDetailView.vue` | 评测完整展示页：标题/发布者/时间/关联硬件卡片/配图/使用体验/性能测试数据/优缺点分析 |
| `api/user.ts` — `getEvaluationById()` | 单篇评测 API |
| `router/index.ts` — `/evaluation/:id` 路由 | |
| `UserProfileController.java` — `GET /evaluation/{id}` | 后端公开接口 |

---

## 六、图片灯箱（点击放大 + 滚轮缩放）

### 功能
- 点击任意图片 → 全屏黑色遮罩灯箱（80% 不透明度）
- 滚轮上下 → 缩放 10%/步（范围 50% ~ 500%）
- 底部显示当前缩放百分比
- 点击遮罩或关闭按钮退出

### 涉及文件
`EvaluationDetailView.vue`、`HardwareDetailView.vue`、`UserView.vue`、`EvaluationsView.vue`

---

## 七、评测列表页

### 新增
| 文件 | 说明 |
|------|------|
| `views/EvaluationsView.vue` | 双列网格卡片布局，展示所有评测（标题/摘要/关联硬件/配图/发布者/时间） |
| `HelloController.java` — `GET /api/evaluations` | 公开接口，返回所有已通过评测 |
| 导航栏 | "硬件评测"按钮（桌面+移动端） |

---

## 八、核心推荐算法

### 算法设计
根据 `算法.txt` 实现硬性/软性条件双层筛选：

**硬性条件（全部必须满足）**：
- 游戏门槛：`is_gaming=true` 需有独显
- 预算：`CPU价格 + GPU价格 + 主板价格 ≤ budget`
- CPU核心数：`≥ min_cpu_cores`

**软性条件（打分排序）**：
- GPU系列匹配（RTX30/40/50系、RX7000系）
- 主板尺寸匹配（ATX/M-ATX/ITX）
- 品牌偏好匹配（CPU/GPU/主板中任意命中）
- 软性分 = 命中数 / 软性条件总数
- 排序：软性分降序 → 总价升序 → Top 3

### 技术演进
1. **初版**：新建 `pc_configuration` 表存储预置配置 → 用户数据库无此表，报错
2. **终版**：废弃预置表方案，改用 `Hardware_Inf` + `CPU_Inf` + `GraphicsCard_Inf` + `Motherboard_Inf` 动态组合，按 CPU 接口 ↔ 主板 CPU 接口匹配生成兼容方案

### 新增/修改文件
| 文件 | 说明 |
|------|------|
| `service/RecommendationService.java` | 核心算法服务 |
| `controller/RecommendationController.java` | `POST /api/recommend` |
| `mapper/CpuInfMapper.java` | 新建 |
| `mapper/GraphicsCardInfMapper.java` | 新建 |
| `mapper/MotherboardInfMapper.java` | 新建 |
| `views/RecommendView.vue` | 推荐页面（左侧表单+右侧结果卡片） |
| `init.sql` — 补充硬件 | 11款CPU + 5款GPU + 7款主板（覆盖 ¥3229~¥17897） |
| 导航栏 | "配置推荐"按钮 |

---

## 九、推荐榜页面

### 新增
| 文件 | 说明 |
|------|------|
| `controller/RankingController.java` | `GET /api/ranking`，CPU/显卡/主板分榜，性价比评分排序 |
| `views/RankingView.vue` | 三Tab（CPU/显卡/主板），排名勋章+规格+评分 |
| 导航栏 | "推荐榜"按钮 |

### 评分公式
- **CPU榜**：`核心数 × 频率 / 价格 × 1000`
- **显卡榜**：`显存(GB) / 价格 × 10000`
- **主板榜**：`(内存槽 + M.2槽) / 价格 × 10000`

---

## 十、管理员后台系统

### 主题
GitHub 暗色风格：`#0d1117` 背景 / `#161b22` 卡片 / `#30363d` 边框 / `#c9d1d9` 文字

### 新增文件
| 文件 | 说明 |
|------|------|
| `controller/AdminController.java` | 管理员API（硬件CRUD、评测审核、统计概览） |
| `views/admin/AdminLayout.vue` | 暗色侧边栏布局，管理员用户名+退出按钮 |
| `views/admin/AdminDashboard.vue` | 概览统计卡片（CPU/GPU/主板/评测/用户数） |
| `views/admin/AdminHardware.vue` | 硬件管理：分类过滤（全部/CPU/显卡/主板）+ 增删改 |
| `views/admin/AdminAudit.vue` | 评测审核：状态筛选 + 完整详情展开 + 通过/拒绝 |

### 权限设计
- 管理员登录后路由守卫强制跳转 `/admin`，无法访问前台页面
- 管理员与普通用户共用同一登录表单，登录后根据角色自动分流
- `/admin` 为独立顶级路由，不嵌套 MainLayout，无前台导航栏和页脚
- 所有 `/api/admin/**` 需要 `@PreAuthorize("hasRole('ADMIN')")`

### 编辑表单升级
硬件编辑表单按类型分段展示完整规格字段：
- **CPU**：核心数/线程数/基础频率/TDP/接口类型/支持内存
- **显卡**：核心型号/显存容量/显存类型/功耗/长度
- **主板**：CPU接口/板型/内存插槽数/M.2插槽数/支持内存
- 后端 `updateHardware()` 同步更新 `Hardware_Inf` 和对应的 `CPU_Inf`/`GraphicsCard_Inf`/`Motherboard_Inf` 表

---

## 文件变更统计

| 类别 | 新建 | 修改 |
|------|------|------|
| 后端 Java | 15 | 6 |
| 前端 Vue/TS | 10 | 8 |
| SQL/配置 | 2 | 3 |
| **合计** | **27** | **17** |
