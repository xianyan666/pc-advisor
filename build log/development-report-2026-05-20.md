# PC硬件推荐系统 — 开发报告

**日期**：2026-05-20

---

## 一、Android WebView APP 封装

### 背景
系统已完整开发完成（后端 Spring Boot + 前端 Vue 3），需要封装为 Android APK，在同一局域网内连接后端服务器，实现数据读取和操作上传。

### 总体架构

```
+-------------------+       LAN HTTP        +-------------------+
|   Android 设备     | -------------------> |  主机 Spring Boot  |
|   WebView 加载     |   /api/*             |  Backend (:8080)  |
|   本地前端资源     |   /images/*          |   MySQL           |
+-------------------+                      +-------------------+
```

- 前端 Vue.js 构建产物打包在 APK assets 中
- WebView 加载本地 HTML/JS/CSS，无需启动前端 dev server
- JavaScript 层拦截 API 请求，重写为服务器地址
- 用户可在设置界面配置后端 IP:端口

### 新增文件

| 文件 | 说明 |
|------|------|
| `android/build.gradle.kts` | 项目级 Gradle（AGP 8.5 + Kotlin 2.0） |
| `android/settings.gradle.kts` | 模块配置 |
| `android/gradle.properties` | Gradle JVM参数 |
| `android/gradle/wrapper/` | Gradle 8.7 wrapper |
| `android/app/build.gradle.kts` | 应用模块（minSdk 24, targetSdk 35）+ 自动复制前端资源 |
| `android/app/src/main/AndroidManifest.xml` | 权限声明（INTERNET/NETWORK）+ HTTP明文配置 |
| `android/app/src/main/java/com/pchw/app/MainActivity.kt` | 主界面：Toolbar + WebView 容器 + 设置菜单 |
| `android/app/src/main/java/com/pchw/app/SettingsActivity.kt` | 设置界面：IP/端口配置 + 连接测试 |
| `android/app/src/main/java/com/pchw/app/webview/ApiProxyInterceptor.kt` | **核心**：JS注入 + shouldInterceptRequest 请求拦截 |
| `android/app/src/main/java/com/pchw/app/webview/WebViewSetup.kt` | WebView 配置（JS、DOM Storage） |
| `android/app/src/main/java/com/pchw/app/util/ServerConfig.kt` | SharedPreferences 持久化服务器地址 |
| `android/app/src/main/res/layout/activity_main.xml` | 主界面布局 |
| `android/app/src/main/res/layout/activity_settings.xml` | 设置界面布局 |
| `android/app/src/main/res/values/strings.xml` | 中文字符串资源 |
| `android/app/src/main/res/values/colors.xml` | 主题颜色 |
| `android/app/src/main/res/values/themes.xml` | AppCompat 主题 |
| `android/app/src/main/res/xml/network_security_config.xml` | HTTP明文通信配置 |
| `android/app/src/main/res/menu/main_menu.xml` | 工具栏菜单 |
| `android/app/src/main/res/drawable/` | 启动器图标 + 齿轮设置图标 |
| `android/scripts/copy-assets.bat` | 前端资源复制脚本 |

---

## 二、关键问题与解决方案

### 问题1：绝对路径资源加载
- **现象**：build产物 `index.html` 使用 `/assets/xxx.js` 绝对路径，`file://` 协议下解析为 `file:///assets/...`（不存在）
- **解决**：改用 `loadDataWithBaseURL("http://localhost/", ...)` 加载页面，`shouldInterceptRequest` 拦截所有 `http://localhost/assets/*` 请求，从 APK assets 读取返回

### 问题2：ES Module CORS 限制
- **现象**：`file://` 协议下 `<script type="module">` 被 Chrome/WebView CORS 策略阻止，导致整个 Vue 应用无法启动
- **解决**：以 `http://localhost` 为源加载，ES module 正常工作；同时移除 `crossorigin` 属性

### 问题3：Material3 组件不兼容
- **现象**：设置页面点击闪退
- **原因**：布局使用 `TextInputLayout`、`MaterialButton` 等 Material3 组件，但主题为 AppCompat
- **解决**：替换为标准 `EditText` + `Button` 组件

### 问题4：API 请求路由
- **需求**：API 请求 `/api/*` 需指向局域网后端服务器，图片 `/images/evaluations/*` 需从服务器加载
- **解决**：注入 JS 代理脚本，patch `XMLHttpRequest.open`、`fetch`、`HTMLImageElement.src` setter，自动将 `/api/*` 和 `/images/evaluations/*` 重写为 `http://<服务器IP>:8080/api/*`

---

## 三、URL 请求处理流程

| 请求路径 | 处理方式 |
|----------|----------|
| `/assets/*.js/css` | Native → APK assets 本地提供 |
| `/favicon.ico` | Native → APK assets 本地提供 |
| `/images/products/*` | Native → APK assets 本地提供 |
| `/images/evaluations/*` | Native → 代理到后端服务器 |
| `/api/*`（XHR/fetch） | JS代理 → 重写为后端服务器地址 |
| SPA路由（`/hardware`等） | Native → 返回 index.html |

---

## 四、APK 构建流程

1. Gradle 构建前自动执行 `copyWebAssets` 任务
2. 从 `backend/src/main/resources/static/` 复制 64 个前端文件到 `assets/www/`
3. `javac` + `kotlinc` 编译 5 个 Kotlin 源文件
4. 输出 APK 体积约 6.9MB

---

## 五、文件变更统计

| 类别 | 新建 | 修改 |
|------|------|------|
| Android Kotlin 源文件 | 5 | 3（修复） |
| Android 资源文件 | 15 | 1（修复） |
| Gradle 配置 | 5 | 0 |
| 脚本 | 1 | 0 |
| 根目录配置 | 1（.gitignore） | 0 |
| **合计** | **27** | **4** |

---

## 六、Git 仓库

代码已推送至 GitHub：https://github.com/xianyan666/pc-advisor

- `14aee4b` — Initial commit（324 文件）
- `7e0de40` — 修复 WebView 空白页面（ES module CORS）
- `27e875f` — 修复 serverBaseUrl 编译错误
