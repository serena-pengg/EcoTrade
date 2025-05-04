import os
import json
import logging
import socket
from flask import Flask, request, jsonify
from flask_cors import CORS
import pymysql
from waitress import serve
import psutil
from together import Together

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 设置环境变量以禁用 Flask 的调试模式
os.environ['FLASK_ENV'] = 'production'
os.environ['FLASK_DEBUG'] = '0'

app = Flask(__name__)
CORS(app)

# Together API 客户端
client = Together(api_key="c668647397315ce4de760c71e39db88cbd4b30575ec7aafa40cc19c5ad08b80b")

# 数据库配置
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'root',
    'db': 'secondhand',
    'charset': 'utf8mb4'
}

def get_db_connection():
    return pymysql.connect(**DB_CONFIG)

def get_available_products():
    """获取数据库中所有可用的商品"""
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            sql = """
                SELECT id, name, description, price, image_url
                FROM products
                ORDER BY created_at DESC
            """
            cursor.execute(sql)
            products = cursor.fetchall()
            return [{
                'id': p[0],
                'name': p[1],
                'description': p[2],
                'price': float(p[3]),
                'image_url': p[4]
            } for p in products]
    except Exception as e:
        logger.error(f"获取商品列表失败: {str(e)}")
        return []
    finally:
        if 'conn' in locals():
            conn.close()

def create_product_context(products):
    """创建商品上下文提示"""
    if not products:
        return "目前没有可用的商品。"
    
    context = "以下是当前可用的商品列表：\n\n"
    for product in products:
        context += f"商品名称：{product['name']}\n"
        context += f"描述：{product['description']}\n"
        context += f"价格：{product['price']}元\n"
        context += "-------------------\n"
    return context

@app.route('/health', methods=['GET'])
def health_check():
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            cursor.execute("SELECT 1")
            result = cursor.fetchone()
            return jsonify({"status": "ok", "database": "ok" if result else "error"})
    except Exception as e:
        logger.error(f"健康检查失败: {str(e)}")
        return jsonify({"status": "error", "database": "error"}), 500
    finally:
        if 'conn' in locals():
            conn.close()

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        if not data or 'message' not in data:
            return jsonify({"error": "Invalid request format"}), 400
            
        message = data['message']
        logger.info(f"收到消息: {message}")
        
        if not message:
            return jsonify({"error": "消息不能为空"}), 400
        
        # 获取当前可用的商品
        products = get_available_products()
        product_context = create_product_context(products)
        
        # 构建系统提示
        system_prompt = f"""你是一个绿色免税跨境电商平台的智能助手。你的任务是帮助用户找到他们需要的商品。首推更加绿色环保的商品。
{product_context}

请根据以上商品信息回答用户的问题。如果用户询问的商品不在列表中，请明确告知用户，并推荐类似的商品。
回答要简洁明了，直接推荐具体的商品。"""

        # 使用 Together API 生成回复
        response = client.chat.completions.create(
            model="deepseek-ai/DeepSeek-V3",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ]
        )
        
        logger.info(f"生成回复: {response.choices[0].message.content}")
        return jsonify({"response": response.choices[0].message.content})
        
    except Exception as e:
        logger.error(f"处理聊天请求时出错: {str(e)}")
        return jsonify({"error": "处理请求时出错"}), 500

def is_port_in_use(port):
    """检查端口是否被占用"""
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(('localhost', port)) == 0

def kill_process_on_port(port):
    """终止占用指定端口的进程"""
    for proc in psutil.process_iter(['pid', 'name']):
        try:
            for conn in proc.connections():
                if conn.laddr.port == port:
                    proc.terminate()
                    proc.wait(timeout=5)
                    logger.info(f"已终止进程 {proc.pid}")
                    return True
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.TimeoutExpired):
            continue
    return False

if __name__ == '__main__':
    port = 5000
    
    # 检查端口是否被占用
    if is_port_in_use(port):
        logger.warning(f"端口 {port} 已被占用，尝试终止现有进程...")
        kill_process_on_port(port)
    
    # 启动服务器
    logger.info(f"启动服务器，监听端口 {port}")
    serve(app, host='0.0.0.0', port=port) 