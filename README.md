# LeTucOJ

<div align="center">
  <!-- 预留项目logo位置 -->
  <img src="#" alt="LeTucOJ Logo" width="200" height="200">
  
  <p>一个试图让出题变简单的OJ系统</p>
  
  <!-- 预留项目截图位置 -->
  <img src="#" alt="系统首页截图" width="800">
</div>

## 项目简介

LeTucOJ 是一个功能完善的在线评测系统（Online Judge），专注于提供简洁高效的题目管理、代码评测和竞赛组织功能。系统采用微服务架构，支持多种编程语言，旨在为教育机构和个人提供便捷的编程学习和竞赛环境。

## 主要功能

### 题目管理
- 支持多种编程语言（C、C++、Java、Python等）的代码评测
- 题目编辑与预览，支持Markdown格式
- 测试用例管理和在线调试
- 题目难度和标签分类

### 代码评测
- 安全沙箱环境执行用户代码
- 实时评测结果反馈
- 详细的错误信息展示
- 支持自定义时间和内存限制

### 竞赛系统
- 创建和管理编程竞赛
- 实时排行榜更新
- 竞赛题目配置和分数设置
- 竞赛结果统计和分析

### 用户权限
- 三级权限体系（USER/MANAGER/ROOT）
- 基于JWT的身份认证
- 用户历史记录和成绩查询
- 个人信息管理

## 技术栈

### 后端
- **框架**: Spring Boot 3.3.0, Spring Cloud 2023.0.1
- **数据库**: MySQL
- **缓存**: Redis
- **消息队列**: RocketMQ 5.3.3
- **对象存储**: MinIO
- **ORM**: MyBatis-Plus
- **微服务组件**: Spring Cloud LoadBalancer, OpenFeign

### 前端
- **框架**: Vue 3.5.17, TypeScript 5.9.2
- **构建工具**: Vite 6.3.5
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **编辑器**: Monaco Editor (VS Code的编辑器核心)

### 部署
- **容器化**: Docker
- **服务编排**: Docker Compose
- **负载均衡**: Nginx

## 系统架构

<!-- 预留系统架构图位置 -->
<img src="#" alt="系统架构图" width="800">

系统采用微服务架构，包含以下主要服务模块：

- **gateway**: API网关，统一入口，路由转发
- **user**: 用户服务，认证授权和用户管理
- **practice**: 题目服务，题目管理和查询
- **contest**: 竞赛服务，竞赛组织和管理
- **run**: 判题服务，代码执行和评测
- **sys**: 系统服务，系统配置和管理
- **advice**: 建议服务，可能提供算法建议或反馈

## 目录结构

```
LeTucOJ/
├── advice/          # 建议服务模块
├── common/          # 公共组件和工具类
├── contest/         # 竞赛服务模块
├── deploy/          # 部署配置和脚本
│   ├── docker-compose/  # Docker Compose配置
│   ├── nginx/           # Nginx配置
│   ├── run_c/           # C语言运行环境
│   ├── run_cpp/         # C++运行环境
│   ├── run_java/        # Java运行环境
│   ├── run_py/          # Python运行环境
│   ├── run_js/          # JavaScript运行环境
│   └── ...              # 其他部署配置
├── gateway/         # API网关服务
├── practice/        # 题目服务模块
├── run/             # 判题服务模块
├── sys/             # 系统服务模块
├── user/            # 用户服务模块
├── ai_pages/        # AI功能相关前端页面
├── ts_pages/        # 主要前端页面(TypeScript)
└── letucoj.sql      # 数据库初始化脚本
```

## 部署指南

### 前置要求
- Docker 20.10+
- Docker Compose 2.0+
- 至少4GB RAM (推荐8GB+)
- 至少20GB磁盘空间

### 快速部署

1. 克隆项目代码
```bash
git clone https://github.com/your-org/LeTucOJ.git
cd LeTucOJ
```

2. 配置环境变量
编辑 `deploy/docker-compose/.env` 文件（如果不存在则创建）：
```dotenv
HOST_DIR=/path/to/host/directory
```

3. 启动服务
```bash
cd deploy/docker-compose
docker-compose up -d
```

4. 访问系统
服务启动后，可以通过 http://localhost 访问系统首页

<!-- 预留部署流程图位置 -->
<img src="#" alt="部署流程图" width="800">

### 数据库初始化

系统首次启动时会自动执行 `deploy/mysql/letucoj.sql` 进行数据库初始化。如需手动导入，请执行：

```bash
docker exec -i mysql mysql -u root -p letucoj < letucoj.sql
```

## 开发指南

### 后端开发

1. 环境准备
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+

2. 编译打包
```bash
mvn clean package -DskipTests
```

### 前端开发

1. 进入前端目录
```bash
cd ts_pages  # 或 cd ai_pages
```

2. 安装依赖
```bash
npm install
```

3. 开发模式
```bash
npm run dev
```

4. 构建生产版本
```bash
npm run build
```

## 安全措施

- 代码在Docker容器沙箱中执行，确保系统安全
- 基于JWT的身份验证和基于角色的权限控制
- 输入验证和XSS防护
- 防越权访问控制
- 敏感信息加密存储

## 用户指南

### 注册与登录
1. 访问系统首页，点击"注册"按钮
2. 填写用户名、邮箱和密码
3. 注册成功后，使用账号密码登录系统

### 创建和提交题目
1. 登录后，进入"练习"页面
2. 选择题目，阅读题目描述
3. 在编辑器中编写代码
4. 选择编程语言，点击"提交"按钮
5. 查看评测结果和详细信息

### 创建竞赛
<!-- 预留竞赛创建流程图位置 -->
<img src="#" alt="竞赛创建流程图" width="800">

## API文档

系统提供了完整的REST API，详情请查看项目中的API文档：

- [API接口文档](http://localhost:7777/api-docs) （部署后访问）
- 或直接查看项目中的API文档文件：`ts_pages/public/doc.md` 和 `ai_pages/public/doc.md`

## 常见问题

### 1. 如何添加新的编程语言支持？

1. 在 `deploy` 目录下创建新的语言运行环境目录（如 `run_xxx`）
2. 编写对应的Dockerfile和运行脚本
3. 在 `docker-compose.yml` 中添加对应的服务配置
4. 在配置文件中设置时间和内存限制

### 2. 如何修改题目评测限制？

编辑 `config.yaml` 文件，可以设置不同编程语言的默认时间和内存限制。

### 3. 判题结果如何解释？

- **AC (Accepted)**: 代码正确通过所有测试用例
- **WA (Wrong Answer)**: 代码输出与预期不符
- **TLE (Time Limit Exceeded)**: 代码运行超时
- **MLE (Memory Limit Exceeded)**: 代码内存使用超限
- **RE (Runtime Error)**: 代码运行时出错
- **CE (Compile Error)**: 代码编译失败

## 许可证

<!-- 预留许可证信息位置 -->

## 贡献指南

我们欢迎社区贡献！如果您有兴趣参与项目开发，请遵循以下步骤：

1. Fork 项目仓库
2. 创建您的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 开启一个 Pull Request

## 致谢

<!-- 预留致谢信息位置 -->

## 联系方式

项目维护者：[项目团队邮箱/联系方式]

<!-- 预留联系信息二维码位置 -->
<img src="#" alt="联系方式二维码" width="200">
