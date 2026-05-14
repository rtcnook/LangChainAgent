# LangChainAgent 前端与跨端应用构建说明

本仓库包含两个主要应用：

- `frontCode/`：Next.js 前端源码。
- `Multiplatform/`：Kotlin Multiplatform / Compose Multiplatform 跨端应用，包含 Android、Desktop JVM、Web JS、Web Wasm、iOS framework 和 server 模块。

## 前端 frontCode

进入前端目录：

```powershell
cd frontCode
```

安装依赖：

```powershell
npm install
```

如果本机 npm 默认缓存目录权限异常，可以把缓存放到项目目录：

```powershell
npm install --cache .\.npm-cache
```

运行开发服务：

```powershell
npm run dev -- --hostname 127.0.0.1 --port 3000
```

当前 Windows 环境中 Turbopack 可能因为临时目录权限报 `os error 5`。遇到该问题时使用 webpack dev server：

```powershell
$env:TEMP = Join-Path (Resolve-Path .).Path '.tmp'
$env:TMP = $env:TEMP
npm run dev -- --hostname 127.0.0.1 --port 3000 --webpack
```

访问地址：

```text
http://127.0.0.1:3000
```

代码检查与生产构建：

```powershell
npm run lint
npm run build
```

## 跨端应用 Multiplatform

进入跨端项目目录：

```powershell
cd Multiplatform
```

查看可用 Gradle 任务：

```powershell
.\gradlew.bat tasks --all
```

构建 Android debug APK：

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

构建 Desktop JVM jar：

```powershell
.\gradlew.bat :composeApp:jvmJar
```

构建 Web JS 生产分发包：

```powershell
.\gradlew.bat :composeApp:jsBrowserDistribution
```

构建 Web Wasm 生产分发包：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
```

构建 server 模块：

```powershell
.\gradlew.bat :server:build
```

一次性构建主要跨端产物：

```powershell
.\gradlew.bat :composeApp:assembleDebug :composeApp:jvmJar :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution :server:build
```

运行 Desktop JVM 应用：

```powershell
.\gradlew.bat :composeApp:run
```

运行 server：

```powershell
.\gradlew.bat :server:run
```

运行 Compose Web 开发服务：

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
```

或使用 JS target：

```powershell
.\gradlew.bat :composeApp:jsBrowserDevelopmentRun
```

## iOS 说明

`Multiplatform/iosApp` 需要在 macOS + Xcode 环境中打开并运行。Windows 环境可以通过 Gradle 生成部分 Kotlin/Native 任务列表，但完整 iOS App 编译、签名和模拟器运行应在 macOS 上完成。

## 本次已验证命令

以下命令已在当前 Windows 环境运行：

- `npm install --cache .\.npm-cache`：通过。
- `npm run lint`：通过，存在 4 个 warning。
- `npm run build`：通过。
- `npm run dev -- --hostname 127.0.0.1 --port 3000 --webpack`：已启动，`http://127.0.0.1:3000` 返回 `200 OK`。
- `.\gradlew.bat tasks --all`：通过。
- `.\gradlew.bat :composeApp:assembleDebug :composeApp:jvmJar :composeApp:jsBrowserDistribution :composeApp:wasmJsBrowserDistribution :server:build`：通过。

