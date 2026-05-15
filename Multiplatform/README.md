# Kotlin Multiplatform 跨端应用说明

`Multiplatform/` 是 AI 私厨的 Kotlin Multiplatform / Compose Multiplatform 项目，目标平台包括 Android、iOS、Desktop JVM、Web JS、Web Wasm 和 Ktor Server。

当前 `composeApp` 已开始复刻 `frontCode/` 的前端界面：暖色背景、顶部标题栏、聊天面板、空状态、用户/助手消息气泡和底部输入栏。同时已经接入 FastAPI 后端的历史消息、清空会话和聊天请求接口。

## 目录结构

```text
Multiplatform/
├── composeApp/                 # Compose Multiplatform 应用
│   └── src/
│       ├── commonMain/          # 跨平台共享 UI 和逻辑
│       ├── androidMain/         # Android 专属入口
│       ├── iosMain/             # iOS 专属入口
│       ├── jvmMain/             # Desktop JVM 专属入口
│       └── webMain/             # Web 专属入口
├── iosApp/                      # iOS App 壳工程，需要 macOS + Xcode
├── server/                      # Ktor Server 模块
├── shared/                      # 多平台共享基础代码
├── gradlew                      # macOS/Linux Gradle Wrapper
└── gradlew.bat                  # Windows Gradle Wrapper
```

## 当前 UI 入口

跨端共享界面入口：

```text
composeApp/src/commonMain/kotlin/com/example/multiplatform/App.kt
```

当前测试文件：

```text
composeApp/src/commonTest/kotlin/com/example/multiplatform/ComposeAppCommonTest.kt
composeApp/src/commonTest/kotlin/com/example/multiplatform/ChatControllerTest.kt
```

## 后端访问

跨端后端访问入口：

```text
composeApp/src/commonMain/kotlin/com/example/multiplatform/ChatBackend.kt
```

当前默认后端地址：

```text
http://localhost:8001
```

已接入接口：

```text
GET    /api/v1/chat/messages?thread_id=default-thread
DELETE /api/v1/chat/messages?thread_id=default-thread
GET    /api/v1/oss/presign?filename=<image-name>
PUT    <OSS presigned uploadUrl>
POST   /api/v1/chat/stream
```

先在仓库根目录启动 Python 后端：

```powershell
uv run python -m app.main
```

## 桌面端图片上传

Desktop JVM 端已经支持点击底部“图片”按钮打开系统文件选择器。支持选择 `jpg`、`jpeg`、`png`、`gif`、`webp` 图片。

发送时流程如下：

```text
选择本地图片 -> 生成时间戳 OSS 文件名 -> 请求 /api/v1/oss/presign -> PUT 上传到 OSS -> 将 accessUrl 作为 image_url 发送给 /api/v1/chat/stream
```

Android、iOS、Web 目前保留空实现，后续可以接入各平台原生图片选择器。

## Windows 常用命令

以下命令均在 `Multiplatform/` 目录下执行。

查看所有 Gradle 任务：

```powershell
.\gradlew.bat tasks --all
```

编译 Android debug APK：

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

运行 Desktop JVM 应用：

```powershell
.\gradlew.bat :composeApp:run
```

构建 Desktop JVM jar：

```powershell
.\gradlew.bat :composeApp:jvmJar
```

运行 JVM 测试：

```powershell
.\gradlew.bat :composeApp:jvmTest
```

构建 Web Wasm 生产产物：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
```

运行 Web Wasm 开发服务：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

构建 Web JS 生产产物：

```powershell
.\gradlew.bat :composeApp:jsBrowserDistribution
```

运行 Web JS 开发服务：

```powershell
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

运行 Ktor Server：

```powershell
.\gradlew.bat :server:run
```

## iOS 运行说明

iOS App 位于：

```text
iosApp/
```

完整 iOS 编译、签名和模拟器运行需要 macOS + Xcode。在 macOS 上打开 `iosApp` 目录中的 Xcode 工程后运行。

## 已验证命令

当前 UI 复刻和后端访问接入后已验证：

```powershell
.\gradlew.bat :composeApp:jvmTestClasses
.\gradlew.bat :composeApp:jvmTest
.\gradlew.bat :composeApp:jvmJar
.\gradlew.bat :composeApp:assembleDebug
.\gradlew.bat :composeApp:jsBrowserDistribution
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
```

以上命令均已通过。若 Windows 上偶发提示 `R.jar` 被占用，关闭 IDE 或释放旧 `build/` 目录文件锁后重试即可。

## 后续计划

后续可以继续补充：

- 将 `POST /api/v1/chat/stream` 改成真正逐块消费响应，而不是等待请求结束后一次性渲染。
- 为 Android、iOS、Web 分别接入原生图片选择器。
- 将 Markdown 回复渲染到 Compose UI。
- 补充 Android、Desktop、Web 端的实际运行截图验证。
