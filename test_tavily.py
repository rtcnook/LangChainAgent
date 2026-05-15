import os
from dotenv import load_dotenv
from langchain_tavily import TavilySearch

load_dotenv()

tavily = TavilySearch(
    api_key=os.getenv("TAVILY_API_KEY"),
    max_results=1
)

try:
    print("Testing Tavily...")
    res = tavily.run("hello")
    print("Tavily SUCCESS")
    print(res[:100])
except Exception as e:
    print(f"Tavily FAILED: {e}")
