import requests
import json

def test_ai():
    # 测试商品推荐
    print("测试商品推荐功能：")
    response = requests.post(
        "http://localhost:5000/chat",
        headers={"Content-Type": "application/json"},
        json={"message": "我是一个二手交易平台的用户，想买一台笔记本电脑，预算5000元左右，请推荐一些合适的商品"}
    )
    print(json.dumps(response.json(), ensure_ascii=False, indent=2))
    
    # 测试普通对话
    print("\n测试普通对话功能：")
    response = requests.post(
        "http://localhost:5000/chat",
        headers={"Content-Type": "application/json"},
        json={"message": "你好，请介绍一下你自己"}
    )
    print(json.dumps(response.json(), ensure_ascii=False, indent=2))

if __name__ == "__main__":
    test_ai() 