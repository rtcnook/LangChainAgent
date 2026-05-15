import os
import requests
from dotenv import load_dotenv

load_dotenv()

api_key = os.getenv("SILICONFLOW_API_KEY")
base_url = "https://api.siliconflow.cn/v1/chat/completions"

headers = {
    "Authorization": f"Bearer {api_key}",
    "Content-Type": "application/json"
}

data = {
    "model": os.getenv("MODEL_NAME", "Qwen/Qwen3.5-4B"),
    "messages": [{"role": "user", "content": "hello"}],
    "stream": False
}

print(f"Testing SiliconFlow API WITHOUT PROXY...")
try:
    # Disable proxy for this request
    os.environ['NO_PROXY'] = 'api.siliconflow.cn'
    response = requests.post(base_url, json=data, headers=headers, timeout=30)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text[:200]}")
except Exception as e:
    print(f"Error: {e}")
