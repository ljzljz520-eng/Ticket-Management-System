# 项目设计文档 - 景区票务管理系统

## 1. 系统架构

```mermaid
flowchart TD
    subgraph Frontend["前端 - Vue 3 + Element Plus"]
        A[登录页] --> B[管理后台]
        B --> C[景区管理]
        B --> D[票种管理]
        B --> E[订单管理]
        B --> F[检票管理]
        B --> G[数据统计]
        B --> H[用户管理]
        B --> I[操作日志]
    end

    subgraph Backend["后端 - Spring Boot 3"]
        J[AuthController] --> K[SecurityFilter]
        K --> L[JWT验证]
        L --> M[业务Controller]
        M --> N[Service层]
        N --> O[MyBatis Mapper]
        N --> P[缓存管理]
    end

    subgraph Database["数据层"]
        Q[(MySQL 8.0)]
        R[(Redis Cache)]
    end

    Frontend -->|HTTP/REST API| Backend
    O --> Q
    P --> R
```

## 2. 数据模型

```mermaid
erDiagram
    SYS_USER ||--o{ TICKET_ORDER : "创建"
    SYS_USER ||--o{ OPERATION_LOG : "产生"
    SCENIC_SPOT ||--o{ TICKET_TYPE : "包含"
    SCENIC_SPOT ||--o{ TICKET_ORDER : "关联"
    SCENIC_SPOT ||--o{ CHECK_IN_RECORD : "关联"
    TICKET_ORDER ||--o{ TICKET_ORDER_ITEM : "包含"
    TICKET_ORDER ||--o{ CHECK_IN_RECORD : "关联"
    TICKET_TYPE ||--o{ TICKET_ORDER_ITEM : "关联"

    SYS_USER {
        bigint id PK
        varchar username UK
        varchar password
        varchar real_name
        varchar phone
        varchar email
        varchar role
        tinyint status
        datetime created_at
    }

    SCENIC_SPOT {
        bigint id PK
        varchar name
        text description
        varchar address
        varchar open_time
        varchar close_time
        int max_capacity
        int current_visitors
        tinyint status
    }

    TICKET_TYPE {
        bigint id PK
        bigint scenic_spot_id FK
        varchar name
        decimal price
        decimal original_price
        int stock
        int daily_limit
        int valid_days
        varchar category
        tinyint status
    }

    TICKET_ORDER {
        bigint id PK
        varchar order_no UK
        bigint user_id FK
        bigint scenic_spot_id FK
        decimal total_amount
        decimal actual_amount
        varchar status
        varchar visitor_name
        varchar visitor_phone
        varchar visitor_id_card
        date visit_date
        datetime paid_at
    }

    TICKET_ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        bigint ticket_type_id FK
        varchar ticket_name
        decimal price
        int quantity
        decimal subtotal
    }

    CHECK_IN_RECORD {
        bigint id PK
        bigint order_id FK
        varchar order_no
        bigint scenic_spot_id FK
        varchar visitor_name
        datetime check_in_time
        datetime check_out_time
        varchar status
        bigint operator_id
    }

    OPERATION_LOG {
        bigint id PK
        bigint user_id
        varchar username
        varchar operation
        varchar method
        text params
        varchar ip
        tinyint status
    }
```

## 3. 接口清单

### 认证模块
- `POST /api/auth/login` - 用户登录（返回 JWT Token）
- `POST /api/auth/logout` - 用户退出
- `GET  /api/auth/info` - 获取当前用户信息

### 景区管理模块
- `GET    /api/scenic-spots` - 景区列表（分页）
- `GET    /api/scenic-spots/{id}` - 景区详情
- `POST   /api/scenic-spots` - 新增景区
- `PUT    /api/scenic-spots/{id}` - 修改景区
- `DELETE /api/scenic-spots/{id}` - 删除景区
- `PUT    /api/scenic-spots/{id}/status` - 启用/禁用景区

### 票种管理模块
- `GET    /api/ticket-types` - 票种列表（分页）
- `GET    /api/ticket-types/{id}` - 票种详情
- `POST   /api/ticket-types` - 新增票种
- `PUT    /api/ticket-types/{id}` - 修改票种
- `DELETE /api/ticket-types/{id}` - 删除票种
- `PUT    /api/ticket-types/{id}/status` - 上架/下架票种
- `PUT    /api/ticket-types/{id}/stock` - 调整库存

### 订单管理模块
- `GET    /api/orders` - 订单列表（分页，支持多条件筛选）
- `GET    /api/orders/{id}` - 订单详情
- `POST   /api/orders` - 创建订单（售票）
- `PUT    /api/orders/{id}/pay` - 订单支付
- `PUT    /api/orders/{id}/cancel` - 取消订单
- `PUT    /api/orders/{id}/refund` - 订单退款
- `GET    /api/orders/{orderNo}/check` - 验证订单（检票用）

### 检票管理模块
- `POST   /api/check-in` - 检票入园
- `PUT    /api/check-in/{id}/check-out` - 检票出园
- `GET    /api/check-in/records` - 检票记录列表（分页）
- `GET    /api/check-in/today` - 今日检票统计

### 数据统计模块
- `GET /api/statistics/overview` - 总览数据（总收入、总订单、今日游客等）
- `GET /api/statistics/sales` - 销售趋势（按日/周/月）
- `GET /api/statistics/ticket-types` - 票种销售占比
- `GET /api/statistics/visitors` - 游客趋势
- `GET /api/statistics/scenic-spots` - 各景区数据对比

### 用户管理模块
- `GET    /api/users` - 用户列表（分页）
- `POST   /api/users` - 新增用户
- `PUT    /api/users/{id}` - 修改用户
- `DELETE /api/users/{id}` - 删除用户
- `PUT    /api/users/{id}/status` - 启用/禁用用户
- `PUT    /api/users/{id}/reset-password` - 重置密码

### 操作日志模块
- `GET /api/logs` - 操作日志列表（分页）

## 4. 页面清单

| 页面 | 路径 | 功能描述 |
|------|------|---------|
| 登录页 | `/login` | 用户名密码登录，JWT 认证 |
| 数据总览 | `/dashboard` | 核心数据卡片 + 销售趋势图 + 票种占比图 + 游客趋势 |
| 景区管理 | `/scenic` | 景区 CRUD 列表，支持启用/禁用 |
| 票种管理 | `/ticket` | 票种 CRUD 列表，支持上架/下架、库存调整 |
| 订单管理 | `/order` | 订单列表（多条件筛选），支持售票/支付/取消/退款 |
| 检票管理 | `/checkin` | 检票入园/出园操作，今日检票统计 |
| 用户管理 | `/user` | 系统用户 CRUD，角色分配，重置密码 |
| 操作日志 | `/log` | 操作日志查询，支持时间范围筛选 |

## 5. 前端设计规范（遵循 frontend-master 标准）

### 5.1 设计方向
- 美学风格：现代专业 + 自然清新 — 结合景区主题，使用自然色系与现代化管理界面的融合
- 设计关键词：专业、清新、自然、高效、沉浸

### 5.2 色彩体系
- 主色 (Primary)：`#2563EB`（天空蓝）— 导航、主要按钮、链接
- 辅色 (Secondary)：`#059669`（翠绿色）— 成功状态、自然主题强化
- 强调色 (Accent)：`#F59E0B`（琥珀金）— 重要提示、票务金色主题
- 中性色阶梯：`#F8FAFC`(50) / `#F1F5F9`(100) / `#E2E8F0`(200) / `#CBD5E1`(300) / `#94A3B8`(400) / `#64748B`(500) / `#475569`(600) / `#334155`(700) / `#1E293B`(800) / `#0F172A`(900) / `#020617`(950)
- 语义色：Success `#10B981` / Warning `#F59E0B` / Error `#EF4444` / Info `#3B82F6`
- 60-30-10 分配：60% 浅灰背景 / 30% 白色卡片 / 10% 蓝色强调

### 5.3 字体体系
- 标题字体：Noto Serif SC（思源宋体）— 中文标题有文化质感
- 正文字体：DM Sans + Noto Sans SC — 正文清晰易读
- 字号阶梯：xs(12px) / sm(14px) / base(16px) / lg(18px) / xl(20px) / 2xl(24px) / 3xl(30px) / 4xl(36px)
- 行高：正文 1.6 / 标题 1.2-1.3
- 字重：Regular(400) / Medium(500) / Semibold(600) / Bold(700)

### 5.4 间距与布局
- 基准单位：4px
- 间距阶梯：4 / 8 / 12 / 16 / 24 / 32 / 48 / 64
- 布局方案：左侧固定侧边栏(240px) + 右侧弹性内容区
- 响应式断点：sm(640px) / md(768px) / lg(1024px) / xl(1280px) / 2xl(1536px)

### 5.5 组件规范
- 圆角：sm(4px) / md(8px) / lg(12px) / xl(16px) / full(9999px)
- 阴影层级：sm(0 1px 2px) / md(0 4px 6px) / lg(0 10px 15px) / xl(0 20px 25px)
- 卡片样式：白色背景 + md 阴影 + lg 圆角 + hover 时阴影提升
- 按钮四态：Default → Hover(亮度+5%) → Active(亮度-5%) → Disabled(opacity 0.5)

### 5.6 动效规范
- 过渡时长：快(150ms) / 中(250ms) / 慢(400ms)
- 缓动函数：`cubic-bezier(0.4, 0, 0.2, 1)` 默认 / `cubic-bezier(0, 0, 0.2, 1)` 进入 / `cubic-bezier(0.4, 0, 1, 1)` 离开
- 三层动效：
  - 页面级：路由切换 fade + slide-up 过渡，首屏卡片 staggered reveal
  - 区块级：数据卡片入场从下方淡入，列表行交错入场(stagger 50ms)
  - 元素级：按钮 hover scale(1.02) + shadow 提升，输入框 focus 蓝色边框过渡

### 5.7 平台适配说明
- 目标平台：Web 桌面端管理后台
- 交互范式：鼠标 hover + 键盘快捷键
- 最小支持宽度：1024px
- 推荐宽度：1280px+

## 6. 核心业务流程

### 售票流程
```mermaid
flowchart TD
    A[选择景区] --> B[选择票种]
    B --> C[填写游客信息]
    C --> D[确认订单]
    D --> E[创建订单 - PENDING]
    E --> F[支付]
    F --> G{支付结果}
    G -->|成功| H[订单状态 → PAID]
    G -->|失败| I[保持 PENDING]
    I --> J[重新支付/取消]
    J -->|取消| K[订单状态 → CANCELLED]
    J -->|重新支付| F
    H --> L[可进行检票]
```

### 检票流程
```mermaid
flowchart TD
    A[输入订单号] --> B[验证订单]
    B --> C{订单状态}
    C -->|PAID| D[检票入园]
    C -->|其他| E[拒绝入园，提示原因]
    D --> F[记录检票]
    F --> G[更新景区在园人数]
    G --> H[游客游览]
    H --> I[检票出园]
    I --> J[更新在园人数]
    J --> K[流程结束]
```

### 退款流程
```mermaid
flowchart TD
    A[选择已支付订单] --> B[申请退款]
    B --> C{是否已检票}
    C -->|未检票| D[全额退款]
    C -->|已检票| E[拒绝退款]
    D --> F[订单状态 → REFUNDED]
    F --> G[恢复票种库存]
    G --> H[退款完成]
```
