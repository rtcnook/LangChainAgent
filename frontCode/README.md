# 前端项目总结

`frontCode/` 是 AI 私厨的 Next.js 前端源码目录。当前前端负责聊天页面、图片上传、历史消息读取和流式对话展示。

## 当前对接关系

前端 API 封装在：

```text
frontCode/lib/api.ts
```

当前 API 地址写死为：

```text
http://localhost:8001
```

因此，前端开发服务或静态页面运行时，必须同时启动 FastAPI 后端：

```powershell
uv run python -m app.main
```

后端默认地址：

```text
http://127.0.0.1:8001
```

如果页面报 `Failed to fetch`，通常是后端 `8001` 没有启动，或者只启动了 LangGraph Dev 的 `2024` 服务。

## 本地开发

安装依赖：

```powershell
npm install
```

启动开发服务：

```powershell
npm run dev
```

默认访问地址：

```text
http://localhost:3000
```

开发服务只负责前端页面热更新；聊天、历史消息和 OSS 签名接口仍然请求 `http://localhost:8001`。

## 代码检查

运行 ESLint：

```powershell
npm run lint
```

当前项目可能存在图片组件或未使用导入相关 warning，需按实际输出处理。

## 编译静态产物

本项目在 `next.config.ts` 中配置了静态导出：

```ts
output: "export"
```

编译命令：

```powershell
npm run build
```

编译完成后，静态产物输出到：

```text
frontCode/out/
```

## 复制到后端静态目录

FastAPI 会托管后端目录中的：

```text
app/static/
```

从项目根目录执行复制命令：

```powershell
Copy-Item -Path frontCode\out\* -Destination app\static -Recurse -Force
```

复制完成后，启动后端：

```powershell
uv run python -m app.main
```

然后通过后端地址访问前端页面：

```text
http://127.0.0.1:8001
```

## 常见问题

### Failed to fetch

原因通常是前端请求 `http://localhost:8001`，但 FastAPI 后端没有启动。

检查命令：

```powershell
Invoke-WebRequest -Uri "http://127.0.0.1:8001/api/v1/chat/messages?thread_id=test" -UseBasicParsing
```

正常应返回：

```json
{"messages":[]}
```

### 只启动 LangGraph Dev 不够

`uv run langgraph dev` 默认使用 `2024` 端口，主要用于 Agent 调试和 LangSmith Studio。当前前端代码默认不请求 `2024`，而是请求 FastAPI 网关 `8001`。

### 静态页面不是最新

如果修改了 `frontCode/` 源码，需要重新执行：

```powershell
npm run build
Copy-Item -Path frontCode\out\* -Destination app\static -Recurse -Force
```

否则 `app/static/` 里仍然是旧版本页面。
