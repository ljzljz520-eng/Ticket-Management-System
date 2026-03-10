# 景区票务管理系统 - API 接口文档

> Base URL: `http://localhost:8080/api`  
> Docker 部署后访问：`http://localhost:8080/api`  
> 认证方式: Bearer Token (JWT)  
> 响应格式: `{ "code": 200, "message": "操作成功", "data": {} }`

---

## 认证模块 `/api/auth`

### POST /api/auth/login — 用户登录
**请求体:**
```json
{ "username": "admin", "password": "admin123" }
```
**响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "ADMIN"
  }
}
```

### GET /api/auth/info — 获取当前用户信息
**Headers:** `Authorization: Bearer {token}`

### POST /api/auth/logout — 退出登录

---

## 景区管理 `/api/scenic-spots`

### GET /api/scenic-spots — 景区列表（分页）
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 10 |
| name | string | 否 | 景区名称（模糊搜索） |
| status | int | 否 | 状态：0-关闭 1-开放 |

### GET /api/scenic-spots/active — 获取启用景区列表
无参数，返回所有状态为开放的景区，用于下拉选择。

### GET /api/scenic-spots/{id} — 景区详情
### POST /api/scenic-spots — 新增景区（需 ADMIN 权限）
**请求体:**
```json
{
  "name": "九寨沟风景区",
  "description": "...",
  "address": "四川省阿坝州",
  "openTime": "07:00",
  "closeTime": "18:00",
  "maxCapacity": 20000,
  "contactPhone": "0837-7739753"
}
```

### PUT /api/scenic-spots/{id} — 修改景区（需 ADMIN 权限）
### DELETE /api/scenic-spots/{id} — 删除景区（需 ADMIN 权限）
### PUT /api/scenic-spots/{id}/status — 切换状态（需 ADMIN 权限）

---

## 票种管理 `/api/ticket-types`

### GET /api/ticket-types — 票种列表（分页）
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| scenicSpotId | long | 否 | 景区ID筛选 |
| category | string | 否 | 类别：ADULT/CHILD/SENIOR/STUDENT/GROUP |
| status | int | 否 | 状态：0-下架 1-上架 |
| name | string | 否 | 名称（模糊搜索） |

### GET /api/ticket-types/active?scenicSpotId=1 — 获取可售票种
### GET /api/ticket-types/{id} — 票种详情
### POST /api/ticket-types — 新增票种（需 ADMIN 权限）
**请求体:**
```json
{
  "scenicSpotId": 1,
  "name": "九寨沟成人票",
  "description": "全价门票",
  "price": 169.00,
  "originalPrice": 250.00,
  "stock": 5000,
  "dailyLimit": 2000,
  "validDays": 1,
  "category": "ADULT"
}
```

### PUT /api/ticket-types/{id} — 修改票种（需 ADMIN 权限）
### DELETE /api/ticket-types/{id} — 删除票种（需 ADMIN 权限）
### PUT /api/ticket-types/{id}/status — 上架/下架（需 ADMIN 权限）
### PUT /api/ticket-types/{id}/stock — 调整库存（需 ADMIN 权限）
**请求体:** `{ "stock": 5000 }`

---

## 订单管理 `/api/orders`

### GET /api/orders — 订单列表（分页）
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| status | string | 否 | PENDING/PAID/CANCELLED/REFUNDED/USED |
| orderNo | string | 否 | 订单号（模糊搜索） |
| visitorName | string | 否 | 游客姓名（模糊搜索） |
| scenicSpotId | long | 否 | 景区ID |
| startDate | date | 否 | 开始日期 |
| endDate | date | 否 | 结束日期 |

### GET /api/orders/{id} — 订单详情
### GET /api/orders/check/{orderNo} — 验证订单（检票用）

### POST /api/orders — 创建订单（售票）
**请求体:**
```json
{
  "scenicSpotId": 1,
  "visitorName": "张三",
  "visitorPhone": "13800138000",
  "visitorIdCard": "510107199001012345",
  "visitDate": "2026-02-14",
  "remark": "",
  "items": [
    { "ticketTypeId": 1, "quantity": 2 },
    { "ticketTypeId": 2, "quantity": 1 }
  ]
}
```

### PUT /api/orders/{id}/pay — 支付订单
### PUT /api/orders/{id}/cancel — 取消订单
### PUT /api/orders/{id}/refund — 退款

---

## 检票管理 `/api/check-in`

### POST /api/check-in — 检票入园
**请求体:**
```json
{ "orderNo": "TK20260213120000123456", "gateNo": "A1" }
```

### PUT /api/check-in/{id}/check-out — 出园登记

### GET /api/check-in/records — 检票记录列表
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码 |
| size | int | 否 | 每页条数 |
| scenicSpotId | long | 否 | 景区ID |
| status | string | 否 | CHECKED_IN/CHECKED_OUT |
| date | date | 否 | 日期 |

### GET /api/check-in/today — 今日检票统计

---

## 数据统计 `/api/statistics`

### GET /api/statistics/overview — 总览数据
**响应字段:** todayIncome, monthIncome, todayOrders, totalOrders, todayVisitors, currentVisitors, scenicSpotCount

### GET /api/statistics/sales — 销售趋势
| 参数 | 类型 | 说明 |
|------|------|------|
| startDate | date | 开始日期（默认近30天） |
| endDate | date | 结束日期 |

### GET /api/statistics/ticket-types — 票种销售占比
### GET /api/statistics/visitors — 游客趋势
### GET /api/statistics/scenic-spots — 景区数据对比

---

## 用户管理 `/api/users`（需 ADMIN 权限）

### GET /api/users — 用户列表
### POST /api/users — 新增用户
### PUT /api/users/{id} — 修改用户
### DELETE /api/users/{id} — 删除用户
### PUT /api/users/{id}/status — 启用/禁用
### PUT /api/users/{id}/reset-password — 重置密码（重置为 123456）

---

## 操作日志 `/api/logs`（需 ADMIN 权限）

### GET /api/logs — 日志列表
| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| username | string | 操作人 |
| operation | string | 操作描述 |
| startDate | date | 开始日期 |
| endDate | date | 结束日期 |

---

## 状态码说明

| 状态码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 参数校验失败 |
| 401 | 未认证/登录过期 |
| 403 | 权限不足 |
| 500 | 系统内部错误 |

## 订单状态流转

```
PENDING（待支付）→ PAID（已支付）→ USED（已使用/检票）
       ↓                ↓
  CANCELLED（已取消）  REFUNDED（已退款）
```
