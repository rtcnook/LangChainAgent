from langchain.chat_models import init_chat_model
from langchain_core.messages import HumanMessage, AIMessageChunk, AIMessage
from langchain_core.tools import tool
from langchain_tavily import TavilySearch
from langchain.agents import create_agent
from app.common.logger import logger
import os
from pathlib import Path
from langgraph.checkpoint.sqlite import SqliteSaver
import sqlite3

# 加载环境变量
from dotenv import load_dotenv

load_dotenv()

# web搜索工具，使用tavily作为web搜索工具
tavily = TavilySearch(
    max_results=5,
    topic="general"
)


# 多模态模型
model = init_chat_model(
    model=os.getenv("MODEL_NAME"),  # 模型名称，这里选择qwen3.5-plus，这是一个多模态模型，支持图片、文本、音频、视频
    model_provider="openai",
    base_url=os.getenv("BASE_URL"),
    api_key=os.getenv("SILICONFLOW_API_KEY")
)


# 初始化checkpointer
# 获取项目根目录下的数据库路径 (位于 app 的上一级)
db_dir = Path(__file__).parent.parent.parent / "db"
db_dir.mkdir(exist_ok=True)
db_path = db_dir / "personal_chief.db"

checkpointer = SqliteSaver(sqlite3.connect(str(db_path), check_same_thread=False))
# 自动建表
checkpointer.setup()

# Agent系统提示词
system_prompt = """
你是一名私人厨师。收到用户提供的食材照片或清单后，请按以下流程操作：
1.识别和评估食材：若用户提供照片，首先辨识所有可见食材。基于食材的外观状态，评估其新鲜度与可用量，整理出一份“当前可用食材清单”。
2.智能食谱检索：优先调用 web_search 工具，以“可用食材清单”为核心关键词，查找可行菜谱。
3.多维度评估与排序：从营养价值和制作难度两个维度对检索到的候选食谱进行量化打分，并根据得分排序，制作简单且营养丰富的排名靠前。
4.结构化方案输出：把排序后的食谱整理为一份结构清晰的建议报告，要包含食谱信息、得分、推荐理由、食谱的参考图片，帮助用户快速做出决策。

请严格按照流程，优先调用 web_search 工具搜索食谱，搜索不到的情况下才能自己发挥。
"""

# 创建代理
agent = create_agent(
    model=model,
    tools=[tavily],
    system_prompt=system_prompt
)

# 流式对话
async def search_recipes(prompt: str, image: str, thread_id: str):
    """调用agent搜索食谱"""
    logger.info(f"[用户]: {prompt}, image: {image}, thread_id: {thread_id}")
    try:
        # 判断是否有图片，封装不同格式的消息
        if not image or image.strip() == "":
            message = HumanMessage(content=prompt)
        else:
            message = HumanMessage(content=[
                {"type": "text", "text": prompt},
                {"type": "image_url", "image_url": {"url": image}}
            ])

        # 流式调用Agent
        logger.info("开始流式调用 Agent...")
        yielded_any = False
        async for chunk, metadata in agent.astream(
            {"messages": [message]},
            {"configurable": {"thread_id": thread_id}},
            stream_mode="messages"
        ):
            # 如果是 AIMessageChunk，检查内容和工具调用
            if isinstance(chunk, AIMessageChunk):
                if chunk.content:
                    logger.info(f"Chunk: {chunk.content}")
                    yielded_any = True
                    yield chunk.content
            
            # 兼容普通 AIMessage
            elif isinstance(chunk, AIMessage) and chunk.content:
                logger.info(f"Message: {chunk.content}")
                yielded_any = True
                yield chunk.content
                
        if not yielded_any:
            logger.warning("大模型没有返回任何有效文本。")
            yield "大模型由于某些原因没有返回内容，这通常是开源多模态模型的上下文兼容问题。请点击右上角【新建会话】重新提问。"
                
        logger.info("Agent 流式调用结束")

    except Exception as e:
        logger.error(f"\n[错误]: {str(e)}")
        yield "信息检索失败，试试看手动输入食物列表？"

# 清空会话
def clear_messages(thread_id: str):
    """清空会话"""
    logger.info(f"清空历史消息，thread_id: {thread_id}")
    checkpointer.delete_thread(thread_id)

# 查询会话历史
def get_messages(thread_id: str) -> list[dict[str, str]]:
    """获取会话历史"""
    logger.info(f"获取历史消息，thread_id: {thread_id}")

    # 根据 thread_id 查询 checkpoint
    checkpoint_tuple = checkpointer.get({"configurable": {"thread_id": thread_id}})

    # 如果不存在，返回空列表
    if not checkpoint_tuple:
        return []

    # 安全获取 messages
    # checkpoint_tuple 是 CheckpointTuple 类型，包含 config, checkpoint, metadata 等
    checkpoint = checkpoint_tuple.checkpoint
    channel_values = checkpoint.get("channel_values")
    if not channel_values:
        return []

    messages = channel_values.get("messages", [])
    if not messages:
        return []

    # 转换消息格式
    result = []
    for msg in messages:
        if not msg.content:
            continue

        if isinstance(msg, HumanMessage):
            result.append({"role": "user", "content": msg.content})
        elif isinstance(msg, AIMessage):
            result.append({"role": "assistant", "content": msg.content})

    return result