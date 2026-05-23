# EcoTrade Global - 可持续跨境电商平台

## 项目简介
“EcoTradee智汇贸通”--EcoTrade Global 是一个融合人工智能与可持续理念的跨境电商平台，旨在响应海南自贸港“零关税、低税率、绿色发展”政策号召，打造兼顾环保效益与用户体验的国际交易新范式。平台以绿色加权的 Apriori 关联推荐算法为核心，通过引入商品碳足迹、回收评分、耐用性评分等环保维度，对推荐结果进行多重优化，实现“推荐即低碳”的智能引导。同时，平台接入 DeepSeek 大语言模型，为用户提供多语言智能客服与商品咨询服务，显著提升交易效率与服务质量。

## 核心特性
- 🍃 **绿色智能推荐系统**
  - 基于绿色加权Apriori算法的商品推荐
  - DeepSeek大模型驱动的智能客服系统
  - 环保评分机制与碳足迹追踪

- 🌍 **自贸港政策支持**
  - 海南自贸港免税商品管理
  - 跨境支付与结算系统
  - 多语言本地化支持

- 🔒 **安全与合规**
  - 多层安全认证机制
  - 数据加密与隐私保护
  - 合规性检查与审计

## 技术架构
- **后端技术栈**
  - Spring Boot 2.7.x
  - Spring Security
  - Spring Data JPA
  - MySQL 8.0
  - Redis缓存

- **前端技术栈**
  - Bootstrap 5
  - Thymeleaf
  - jQuery
  - Google Maps API

- **AI与算法**
  - 绿色加权Apriori算法
  - DeepSeek大模型集成
  - 智能推荐引擎

## 快速开始
1. 克隆项目
```bash
git clone https://github.com/your-username/ecotrade-global.git
```

2. 配置环境
```bash
# 安装依赖
mvn clean install

# 配置数据库
# 修改application.properties中的数据库连接信息
```

3. 运行项目
```bash
mvn spring-boot:run
```

## 项目结构
```
src/
├── main/
│   ├── java/
│   │   └── com/example/Secondhand/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── model/
│   │       ├── repository/
│   │       └── config/
│   └── resources/
│       ├── static/
│       └── templates/
```

## 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

## 贡献指南
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证
本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式
- 项目负责人：[彭思静，罗语涵，张浩辰，闫闪闪]
- 邮箱：[396889003@qq.com]
- 项目链接：[https://github.com/your-username/ecotrade-global]

## 致谢
- 感谢海南自贸港政策支持
- 感谢DeepSeek团队的技术支持
- 感谢所有贡献者的辛勤付出 
