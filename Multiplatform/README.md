# Kotlin Multiplatform 跨端应用说明

`Multiplatform/` 是 AI 私人厨师的 Kotlin Multiplatform / Compose Multiplatform 项目，目标平台包括 Android、iOS、Desktop JVM、Web JS、Web Wasm 和 Ktor Server。

当前 `composeApp` 负责跨平台 UI，`shared` 负责公共业务逻辑。前端界面已经按 `frontCode/` 的聊天体验复刻到 Compose：暖色背景、顶部标题栏、聊天面板、空状态、用户/助手消息气泡、图片选择、复制和底部输入栏。

## 目录结构

```text
Multiplatform/
├── composeApp/                 # Compose Multiplatform 应用层
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/com/example/multiplatform/
│       │       ├── App.kt      # 跨端 UI 入口
│       │       ├── platform/   # 图片选择、剪贴板等平台能力 expect
│       │       └── ui/         # 页面、组件、主题、文案
│       ├── androidMain/        # Android actual 实现
│       ├── iosMain/            # iOS actual 实现
│       ├── jvmMain/            # Desktop JVM actual 实现
│       └── webMain/            # Web actual 实现
├── shared/                     # 跨平台公共逻辑层
│   └── src/commonMain/kotlin/com/example/multiplatform/
│       ├── config/             # 后端地址、超时等配置
│       ├── data/               # ChatBackend、HttpChatBackend
│       ├── domain/             # ChatController 等业务状态控制
│       └── model/              # ChatMessage、SelectedImage、ImageUploadTarget
├── iosApp/                     # iOS App 壳工程，需要 macOS + Xcode
├── server/                     # Ktor Server 模块
├── gradlew                     # macOS/Linux Gradle Wrapper
└── gradlew.bat                 # Windows Gradle Wrapper
```

## 架构约定

- `composeApp` 只写 UI、页面编排、主题、平台能力接入，不放网络请求和核心业务流程。
- `shared` 写公共逻辑：模型、后端配置、网络接口、OSS 上传流程、聊天状态控制和可复用测试。
- `shared` 不依赖 Compose，避免公共逻辑和界面层互相耦合。
- UI 组件按文件拆分，避免把页面、组件、样式和文案都堆在一个大文件里。
- 优先使用跨平台库：Ktor Client、kotlinx.serialization、kotlinx.coroutines、StateFlow。

## 当前 UI 入口

```text
composeApp/src/commonMain/kotlin/com/example/multiplatform/App.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/screen/AiChefScreen.kt
```

主要组件：

```text
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/HeaderBar.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/ChatPanel.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/MessageBubble.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/InputBar.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/EmptyState.kt
composeApp/src/commonMain/kotlin/com/example/multiplatform/ui/components/StatusText.kt
```

## 后端访问

跨端公共后端访问入口：

```text
shared/src/commonMain/kotlin/com/example/multiplatform/data/HttpChatBackend.kt
shared/src/commonMain/kotlin/com/example/multiplatform/domain/ChatController.kt
shared/src/commonMain/kotlin/com/example/multiplatform/config/BackendConfig.kt
```

当前默认后端地址：

```text
http://192.168.2.2:8001
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

后端监听 `0.0.0.0:8001`，同一局域网内的安卓真机、桌面端和前端页面都通过电脑 IP `192.168.2.2:8001` 访问。若手机访问失败，检查电脑防火墙是否放行 `8001`。

跨平台客户端连接超时为 15 秒，聊天生成请求超时为 10 分钟，避免 LangChain/图片识别耗时较长时桌面端提前报 `Request timeout has expired`。

## 图片上传

当前 Desktop JVM 端支持点击底部“图片”按钮打开系统文件选择器。支持选择 `jpg`、`jpeg`、`png`、`gif`、`webp` 图片。

发送时流程如下：

```text
选择本地图片
-> 使用时间戳生成 OSS 文件名
-> 请求 /api/v1/oss/presign
-> PUT 上传到 OSS
-> 将 accessUrl 作为 image_url 发送给 /api/v1/chat/stream
```

Android、iOS、Web 目前保留空实现，后续可以分别接入原生图片选择器。

## 桌面端复制

Desktop JVM 端聊天气泡底部提供“复制”操作，会把当前消息内容写入系统剪贴板。

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

运行 shared 和 composeApp 的 JVM 测试：

```powershell
.\gradlew.bat :shared:jvmTest :composeApp:jvmTest --no-parallel
```

构建 Web JS 生产产物：

```powershell
.\gradlew.bat :composeApp:jsBrowserDistribution
```

构建 Web Wasm 生产产物：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
```

运行 Web JS 开发服务：

```powershell
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

运行 Web Wasm 开发服务：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
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

当前架构拆分和后端访问接入后已验证：

```powershell
.\gradlew.bat :shared:jvmTest :composeApp:jvmTest --no-parallel
.\gradlew.bat -I build-verify.gradle.kts :composeApp:assembleDebug --no-parallel
.\gradlew.bat -I build-verify.gradle.kts :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution --no-parallel
```

说明：本机 `shared/build` 曾被外部 Java 进程锁住，因此 Android/JS/Wasm 验证时临时使用 `build-verify.gradle.kts` 将构建输出重定向到 `build-verify/`。临时脚本和产物已清理，源码构建命令仍使用上方“Windows 常用命令”中的标准命令。

JS/Wasm 构建时根据 Gradle 提示执行过：

```powershell
.\gradlew.bat -I build-verify.gradle.kts kotlinUpgradeYarnLock --no-parallel
```

因此 `kotlin-js-store/yarn.lock` 已更新。

## 后续计划

- 将 `POST /api/v1/chat/stream` 改成真正逐块消费响应，而不是等请求结束后一次性渲染。
- 为 Android、iOS、Web 分别接入原生图片选择器。
- 将 Markdown 回复渲染到 Compose UI。
- 补充 Android、Desktop、Web 端的实际运行截图验证。
