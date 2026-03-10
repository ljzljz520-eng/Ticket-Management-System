# 景区票务管理系统

基于 Spring Boot + MyBatis-Plus + MySQL + Vue 3 + Element Plus 的景区票务管理系统，覆盖景区管理、票种管理、订单管理、检票管理四大核心模块，支持 Spring Security + JWT 认证授权和 Redis 缓存管理。

## 1. How to Run

### 使用 Docker（推荐）

```bash
docker-compose up --build -d
```

**注意**：若本机已占用 3306/6379/8080/8081，需先停止冲突服务或修改 `docker-compose.yml` 中的端口映射。后端启动约需 20–40 秒，请稍候再访问登录页。

**登录 502 时**：多为后端未就绪或 MySQL 未启动。可执行 `docker-compose ps` 确认四类服务均为 Up；执行 `docker-compose logs backend` 查看后端日志，待出现 “Started ScenicTicketApplication” 后再试登录。

启动后访问：
- 管理后台：http://localhost:8081
- 后端 API：http://localhost:8080

### 本地开发

**后端**

```bash
# 确保 MySQL 和 Redis 已启动（可单独运行 docker-compose 中的 mysql 和 redis）
docker-compose up -d mysql redis

cd backend
./mvnw spring-boot:run
# 或
mvn spring-boot:run
```

**前端**

```bash
cd frontend-admin
npm install
npm run dev
```

前端开发服务器默认运行在 http://localhost:8081，自动代理 `/api` 请求到后端 8080 端口。

## 2. Services

| 服务 | 端口 | 说明 |
|------|------|------|
| 管理后台 | 8081 | Vue 3 + Element Plus |
| 后端 API | 8080 | Spring Boot 3 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 3. 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 拥有所有权限（景区管理、票种管理、订单管理、检票管理、用户管理、操作日志） |
| 普通用户 | user | user123 | 基本功能权限（订单管理、检票管理） |

## 4. 题目内容

帮我写一个景区票务管理系统系统，需覆盖4 个核心功能模块，技术栈与开发流程要求 

（一）技术栈要求 

推荐使用技术栈：Spring Boot + Mybatis/Spring Data JPA + MySQL + Maven 

+ Vue，需综合应用以下技术： 

1.前端技术：页面布局美观、交互流畅，实现功能可视化展示。 

2.后端技术：Spring Web 应用支持（处理前端请求）、缓存管理、Spring 

Security（用户登录验证、页面授权）。 

3.数据库技术：实现数据的增删改查操作，设计合理的数据库结构。

## 5. 项目结构

```
label-01747/
├── backend/                     # 后端 - Spring Boot 3
│   ├── src/main/java/com/scenic/ticket/
│   │   ├── config/              # 配置（Security, JWT, CORS, Cache, AOP日志, 全局异常处理）
│   │   ├── common/              # 通用类（Result, PageResult, BusinessException）
│   │   ├── entity/              # 实体类（7张表对应7个Entity）
│   │   ├── dto/                 # 数据传输对象（含参数校验注解）
│   │   ├── mapper/              # MyBatis-Plus Mapper 接口
│   │   ├── service/             # 业务逻辑层
│   │   ├── controller/          # RESTful 控制器层
│   │   └── util/                # 工具类（JWT）
│   ├── src/main/resources/
│   │   ├── mapper/              # MyBatis XML 映射
│   │   ├── application.yml      # 应用配置（支持环境变量覆盖）
│   │   └── schema.sql           # 数据库初始化 + 种子数据
│   ├── .mvn/                    # Maven Wrapper 配置
│   ├── mvnw                     # Maven Wrapper 脚本
│   ├── pom.xml                  # Maven 依赖
│   └── Dockerfile               # 多阶段构建
├── frontend-admin/              # 前端 - Vue 3 + Element Plus
│   ├── src/
│   │   ├── api/                 # 接口请求封装（按模块划分）
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── views/               # 页面组件（8个视图）
│   │   ├── components/          # 可复用组件
│   │   ├── layouts/             # 布局组件
│   │   ├── router/              # 路由配置（含权限守卫）
│   │   ├── styles/              # 全局样式 + Design Tokens
│   │   └── utils/               # 工具函数（Axios 封装）
│   ├── package.json
│   ├── vite.config.ts
│   ├── nginx.conf               # Nginx 反向代理配置
│   └── Dockerfile
├── docs/
│   ├── project_design.md        # 系统设计文档
│   └── api_documentation.md     # API 接口文档
├── docker-compose.yml           # 容器编排（MySQL + Redis + Backend + Frontend）
├── .gitignore
└── README.md
```

## 6. 功能清单

### 核心模块

- [x] **景区管理**：景区 CRUD、开放/关闭状态切换、容量管理
- [x] **票种管理**：票种 CRUD、上架/下架、库存管理、每日限额、多票种类别（成人/儿童/老人/学生/团体）
- [x] **订单管理**：售票开单、支付、取消、退款、订单详情、多条件查询
- [x] **检票管理**：扫码/输入订单号检票入园、出园登记、实时在园统计

### 基础功能

- [x] **数据统计**：今日收入/订单/游客实时卡片、销售趋势折线图、票种占比饼图、游客柱状图、景区对比
- [x] **用户认证**：Spring Security + JWT 认证、角色权限控制（ADMIN/USER）
- [x] **用户管理**：用户 CRUD、启用/禁用、重置密码（仅管理员）
- [x] **操作日志**：AOP 自动记录 POST/PUT/DELETE 操作，含用户、IP、耗时、参数
- [x] **缓存管理**：Redis 缓存统计数据和景区列表

### 工程特性

- [x] 全局异常处理（@RestControllerAdvice，覆盖业务异常、校验异常、权限异常）
- [x] 入参校验（@Valid + DTO，含手机号、身份证、邮箱等格式校验）
- [x] 统一响应格式（Result<T>）
- [x] AOP 操作日志切面（自动记录写操作）
- [x] 敏感信息脱敏（手机号、邮箱在接口返回时脱敏）
- [x] JWT Secret 环境变量化（支持部署时自定义）
- [x] CORS 跨域配置
- [x] UTF-8 中文编码全链路保障
- [x] Maven Wrapper（跨平台一致构建）
- [x] Docker Compose 一键部署
- [x] API 接口文档（docs/api_documentation.md）

## 7. 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 安全 | Spring Security + JWT | - |
| 缓存 | Redis | 7 |
| 数据库 | MySQL | 8.0 |
| 前端框架 | Vue 3 (Composition API) | 3.x |
| UI 组件库 | Element Plus | 2.x |
| 状态管理 | Pinia | 2.x |
| 构建工具 | Vite | 5.x |
| 数据可视化 | ECharts | 5.x |
| 容器化 | Docker + Docker Compose | - |

## 编码说明

本项目所有文件使用 UTF-8 编码，确保中文正常显示：
- 源代码：UTF-8 without BOM
- 数据库：utf8mb4
- 配置文件：UTF-8
- Docker 容器：LANG=C.UTF-8
