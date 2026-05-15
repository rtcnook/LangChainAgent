import requests
import time

url = "http://127.0.0.1:8001/api/v1/chat/stream"
data = {"message": "你好", "thread_id": "test_curl", "image_url": ""}

print("Starting request...")
start_time = time.time()
with requests.post(url, json=data, stream=True) as r:
    print(f"Headers received in {time.time() - start_time:.2f} seconds")
    for chunk in r.iter_content(chunk_size=1024):
        if chunk:
            print(f"[{time.time() - start_time:.2f}s] Chunk: {chunk.decode('utf-8')}")
