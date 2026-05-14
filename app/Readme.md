# LangChain 后端项目总结

`app/` 是 AI 私厨的后端目录，负责 LangChain Agent、FastAPI 接口、OSS 上传签名、会话记忆和前端静态产物托管。

## 当前运行关系

当前项目有两种后端启动方式：

- FastAPI 网关：用于前端页面实际调用，端口是 `8001`。
- LangGraph Dev：用于 Agent 调试和 LangSmith Studio，默认端口是 `2024`。

当前 `frontCode/lib/api.ts` 中 API 地址写死为：

```text
http://localhost:8001
```

因此，前端联调和用户页面访问时，必须启动 FastAPI 网关，也就是 `app.main`，不要只启动 `uv run langgraph dev`。

## 目录职责

```text
app/
├── main.py                    # FastAPI 入口，挂载 API 路由和 app/static 前端静态资源
├── agents/
│   └── personal_chief.py      # LangChain Agent 核心逻辑
├── api/
│   └── v1/
│       ├── chat.py            # 聊天、历史消息接口
│       └── oss.py             # OSS 上传签名 URL 接口
├── common/
│   └── logger.py              # 日志配置
├── db/
│   └── personal_chief.db      # SQLite 会话记忆数据库，本地生成
├── models/
│   └── schemas.py             # Pydantic 请求模型
└── static/                    # 前端编译后的静态产物
```

## 环境准备

项目使用 `uv` 管理 Python 依赖。根目录 `pyproject.toml` 要求 Python `>=3.13`。

安装依赖：

```powershell
uv sync
```

如果本机 `uv` 默认缓存目录权限异常，可以临时指定项目内缓存目录：

```powershell
$env:UV_CACHE_DIR = ".uv-cache"
uv sync
```

## 环境变量

后端从项目根目录 `.env` 加载配置。不要提交真实密钥。

模型服务配置：

```env
MODEL_NAME=你的多模态模型名称
BASE_URL=模型服务的 OpenAI-compatible base url
SILICONFLOW_API_KEY=模型服务 API Key
```

Tavily 搜索：

```env
TAVILY_API_KEY=你的 Tavily API Key
```

LangSmith 调试，可选：

```env
LANGSMITH_API_KEY=你的 LangSmith API Key
LANGSMITH_TRACING=true
LANGSMITH_PROJECT=lc-course
```

阿里云 OSS 上传签名接口：

```env
OSS_ACCESS_KEY_ID=你的 AccessKey ID
OSS_ACCESS_KEY_SECRET=你的 AccessKey Secret
OSS_BUCKET=你的 Bucket 名称
OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com
```

## 启动 FastAPI 网关

这是当前前端需要连接的后端服务。

在项目根目录运行：

```powershell
uv run python -m app.main
```

默认端口：

```text
http://127.0.0.1:8001
```

接口文档：

```text
http://127.0.0.1:8001/docs
```

如果 `reload=True` 在当前终端环境下不稳定，可以用无 reload 方式启动：

```powershell
uv run python -m uvicorn app.main:app --host 127.0.0.1 --port 8001
```

## FastAPI 接口

当前挂载的业务接口：

- `POST /api/v1/chat/stream`：流式对话。
- `GET /api/v1/chat/messages?thread_id=...`：获取会话历史。
- `DELETE /api/v1/chat/messages?thread_id=...`：清空会话历史。
- `GET /api/v1/oss/presign?filename=...`：获取 OSS 上传签名 URL。

前端打开后如果出现 `Failed to fetch`，优先检查：

1. `http://127.0.0.1:8001/docs` 是否能打开。
2. `http://127.0.0.1:8001/api/v1/chat/messages?thread_id=test` 是否返回 `{"messages":[]}`。
3. 是否只启动了 `uv run langgraph dev`，但没有启动 FastAPI 网关。

## 前端静态托管

`app/main.py` 会托管 `app/static/`：

```python
app.mount("/", StaticFiles(directory=static_dir, html=True), name="static")
```

非 API 请求会 fallback 到 `app/static/index.html`，因此可以通过 FastAPI 地址直接访问前端页面。

前端源码在 `frontCode/`，编译后的静态产物需要复制到：

```text
app/static/
```

具体编译和复制命令见 `frontCode/README.md`。

## LangGraph Dev 调试

`langgraph.json` 注册了：

```json
{
  "dependencies": ["."],
  "graphs": {
    "chief_agent": "./app/agents/personal_chief.py:agent"
  },
  "env": ".env"
}
```

启动 LangGraph Dev：

```powershell
uv run langgraph dev
```

默认访问：

```text
http://127.0.0.1:2024/docs
```

LangSmith Studio：

```text
https://smith.langchain.com/studio/?baseUrl=http://127.0.0.1:2024
```

注意：LangGraph Dev 是调试 Agent 的服务，不是当前前端代码默认调用的 FastAPI 网关。

## 会话记忆

当前 Agent 使用 SQLite checkpointer 保存短期记忆，路径是：

```text
app/db/personal_chief.db
```

会话隔离依赖请求中的 `thread_id`。

## 推荐开发流程

1. 配置根目录 `.env`。
2. 执行 `uv sync`。
3. 需要调试 Agent 时，运行 `uv run langgraph dev`，访问 `:2024/docs` 或 LangSmith Studio。
4. 需要联调前端时，运行 `uv run python -m app.main`，访问 `:8001`。
5. 前端更新后，在 `frontCode/` 运行 `npm run build`，再把 `frontCode/out/` 复制到 `app/static/`。
