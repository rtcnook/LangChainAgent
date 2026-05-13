# 仓库指南

## 项目结构与模块组织

本仓库包含 Python LangChain/LangGraph 后端和 Next.js 前端。

- `app/` 是 FastAPI 后端：接口位于 `app/api/v1/`，智能体逻辑位于 `app/agents/`，公共工具位于 `app/common/`，数据模型位于 `app/models/`，导出的前端静态资源位于 `app/static/`。
- `frontCode/` 是可编辑的 Next.js 前端源码，主要目录包括 `frontCode/app/`、`frontCode/components/`、`frontCode/lib/` 和 `frontCode/types/`。
- `notebook/` 存放课程 Notebook 和示例资源。
- `db/` 与 `notebook/**/resources/` 存放本地 SQLite 状态文件。
- `langgraph.json` 注册了 `chief_agent`，入口为 `app/agents/personal_chief.py:agent`。

## 构建、测试与本地开发命令

- `uv sync`：根据 `pyproject.toml` 和 `uv.lock` 安装 Python 依赖。
- `uv run python -m app.main`：在 `127.0.0.1:8001` 启动 FastAPI 后端。
- `uv run langgraph dev`：按 `langgraph.json` 启动 LangGraph 开发服务。
- `cd frontCode; npm install`：安装前端依赖。
- `cd frontCode; npm run dev`：启动 Next.js 开发服务器。
- `cd frontCode; npm run build`：生成前端生产构建。
- `cd frontCode; npm run lint`：运行前端 ESLint 检查。

## 编码风格与命名约定

后端使用 Python 3.13。模块应保持小而清晰，并放在 `app/` 对应包内。Python 文件、函数和变量使用 snake_case；Pydantic 模型使用 PascalCase。FastAPI 路由按业务域组织在 `app/api/v1/` 下。

前端使用 TypeScript、React、Next.js App Router 和 ESLint。React 组件使用 PascalCase，例如 `RecipeCard.tsx`；工具函数使用 camelCase；可复用 UI 组件放在 `frontCode/components/`。

## 测试指南

当前仓库没有独立测试套件。新增后端测试时，放入 `tests/`，文件命名为 `test_*.py`，并优先使用 pytest 兼容写法。前端新增行为时应补充相应测试，并至少运行 `npm run lint`。修改智能体逻辑后，手动验证相关 LangGraph 流程或 `/api/v1/chat/*` 接口。

## 提交与 Pull Request 规范

近期提交信息多为简短祈使句，例如 `fix compile error`、`add front code`、`fix bug`。继续使用这种风格，但应尽量说明具体范围，例如 `fix chat stream error handling`。

Pull Request 应包含简要说明、验证步骤、关联 issue（如有），以及可见前端变更的截图或录屏。涉及 `.env`、本地数据库或外部服务配置时，需要明确说明。

## 安全与配置提示

不要提交真实密钥。后端凭据放在 `.env`，前端环境变量以 `frontCode/.env.local.example` 为模板。除非任务明确要求，否则将 `.venv/`、`.next/`、`__pycache__/`、`.langgraph_api/` 以及 SQLite WAL/SHM 文件视为本地生成产物。
