import langchain.agents
import inspect

try:
    from langchain.agents import create_agent
    print(f"create_agent found in: {inspect.getfile(create_agent)}")
except ImportError:
    print("create_agent NOT found in langchain.agents")
except Exception as e:
    print(f"Error: {e}")
