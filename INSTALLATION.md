# EcoTrade Global 安装说明

## 1. 系统要求

### 1.1 操作系统要求
- **Windows 10/11**
  - 版本：1909或更高
  - 架构：x64
  - 内存：8GB或更高
  - 存储空间：20GB可用空间

- **Linux (推荐)**
  - 发行版：Ubuntu 20.04 LTS或更高
  - 架构：x64
  - 内存：8GB或更高
  - 存储空间：20GB可用空间

### 1.2 开发环境要求
- **Java Development Kit (JDK)**
  - 版本：JDK 1.8或更高
  - 下载地址：https://www.oracle.com/java/technologies/downloads/

- **Maven**
  - 版本：3.6或更高
  - 下载地址：https://maven.apache.org/download.cgi

- **Git**
  - 版本：2.30或更高
  - 下载地址：https://git-scm.com/downloads

## 2. 数据库配置

### 2.1 MySQL服务器
- **版本要求**：MySQL 8.0或更高
- **服务器配置**：
  ```sql
  # 创建数据库
  CREATE DATABASE greenfree CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  
  # 创建用户并授权
  CREATE USER 'users'@'localhost' IDENTIFIED BY 'your_password';
  GRANT ALL PRIVILEGES ON ecotrade.* TO 'users'@'localhost';
  FLUSH PRIVILEGES;
  ```

### 2.2 Redis服务器
- **版本要求**：Redis 6.0或更高
- **配置要求**：
  ```conf
  # redis.conf
  maxmemory 2gb
  maxmemory-policy allkeys-lru
  ```

## 3. 安装步骤

### 3.1 获取源代码
```bash
# 克隆项目
git clone https://github.com/your-username/ecotrade-global.git
cd ecotrade-global
```

### 3.2 配置环境变量
```bash
# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk1.8.0_xxx"
$env:MAVEN_HOME = "C:\apache-maven-3.6.3"
$env:PATH = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:PATH"

# Linux
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export MAVEN_HOME=/opt/apache-maven-3.6.3
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
```

### 3.3 配置应用
1. 修改数据库配置
```properties
# src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecotrade
spring.datasource.username=ecotrade_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_redis_password
```

2. 配置API密钥
```properties
# Google Maps API
google.maps.api.key=your_google_maps_api_key

# DeepSeek API
deepseek.api.key=your_deepseek_api_key
```

### 3.4 构建项目
```bash
# 安装依赖并构建
mvn clean install
```

### 3.5 运行应用
```bash
# 开发环境运行
mvn spring-boot:run

# 生产环境运行
java -jar target/ecotrade-global-1.0.0.jar
```

## 4. 数据导入

### 4.1 初始化数据库
```bash
# 使用Flyway迁移数据库
mvn flyway:migrate
```

### 4.2 导入基础数据
```bash
# 导入初始数据
mysql -u ecotrade_user -p ecotrade < src/main/resources/data/init_data.sql
```

## 5. 验证安装

### 5.1 检查服务状态
```bash
# 检查MySQL服务
systemctl status mysql

# 检查Redis服务
systemctl status redis
```

### 5.2 访问应用
- 打开浏览器访问：http://localhost:8080
- 默认管理员账号：admin@ecotrade.com
- 默认密码：admin123

## 6. 常见问题解决

### 6.1 端口冲突
如果8080端口被占用，修改`application.properties`：
```properties
server.port=8081
```

### 6.2 数据库连接失败
检查：
1. MySQL服务是否运行
2. 数据库用户权限
3. 防火墙设置

### 6.3 内存不足
修改JVM参数：
```bash
java -Xmx2g -Xms1g -jar target/ecotrade-global-1.0.0.jar
```

## 7. 保存修改

完成所有配置后，请确保：
1. 保存所有修改的文件
2. 提交更改到版本控制系统
3. 备份配置文件

```bash
# 提交更改
git add .
git commit -m "Initial setup and configuration"
git push origin main
```

## 8. 技术支持

如遇安装问题，请联系：
- 邮箱：396889003@qq.com
- 项目文档：https://github.com/your-username/ecotrade-global/wiki 