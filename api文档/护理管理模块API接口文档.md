# 东软颐养中心护理管理模块API接口文档

## 📋 接口概述

本文档描述了护理管理模块的四个子模块API接口：

- 护理项目管理
- 护理记录管理
- 护理级别管理
- 客户护理设置

## 🔧 基础信息

- **Base URL**: `http://localhost:8080/api/nursing`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`
- **响应格式**: JSON

## 📦 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

| 字段 | 类型    | 说明                          |
| ---- | ------- | ----------------------------- |
| code | Integer | 状态码，200为成功，其他为失败 |
| msg  | String  | 响应消息                      |
| data | Object  | 响应数据                      |

------

## 🏥 1. 护理项目管理

### 1.1 分页查询护理项目列表

**接口地址**: `GET /project/list`

**请求参数**:

| 参数名          | 类型    | 必填 | 说明                | 默认值 |
| --------------- | ------- | ---- | ------------------- | ------ |
| searchKeyword   | String  | 否   | 搜索关键词          | -      |
| projectCategory | String  | 否   | 项目分类            | -      |
| status          | Integer | 否   | 状态(1-启用,0-停用) | -      |
| caregiverId     | Integer | 否   | 护理人员ID          | -      |
| page            | Integer | 否   | 页码                | 1      |
| size            | Integer | 否   | 每页数量            | 10     |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "projects": [
      {
        "id": 1,
        "projectName": "生命体征监测",
        "projectCategory": "健康监护",
        "description": "测量血压、心率、体温、血氧饱和度",
        "standardDuration": 15,
        "caregiverId": 2,
        "caregiverName": "张护士",
        "frequency": "每日3次",
        "status": 1,
        "createdAt": "2024-06-19T09:00:00",
        "updatedAt": "2024-06-19T09:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10,
    "totalPages": 1
  }
}
```

### 1.2 根据ID查询护理项目详细信息

**接口地址**: `GET /project/{id}`

**路径参数**:

| 参数名 | 类型    | 必填 | 说明   |
| ------ | ------- | ---- | ------ |
| id     | Integer | 是   | 项目ID |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "projectName": "生命体征监测",
    "projectCategory": "健康监护",
    "description": "测量血压、心率、体温、血氧饱和度",
    "standardDuration": 15,
    "caregiverId": 2,
    "caregiverName": "张护士",
    "frequency": "每日3次",
    "status": 1
  }
}
```

### 1.3 添加护理项目

**接口地址**: `POST /project/add`

**请求体**:

```json
{
  "projectName": "生命体征监测",
  "projectCategory": "健康监护",
  "description": "测量血压、心率、体温、血氧饱和度",
  "standardDuration": 15,
  "caregiverId": 2,
  "frequency": "每日3次",
  "status": 1
}
```

**响应示例**:

```json
{
  "code": 200,
  "msg": "添加护理项目成功",
  "data": null
}
```

### 1.4 更新护理项目信息

**接口地址**: `PUT /project/update`

**请求体**:

```json
{
  "id": 1,
  "projectName": "生命体征监测",
  "projectCategory": "健康监护",
  "description": "测量血压、心率、体温、血氧饱和度",
  "standardDuration": 15,
  "caregiverId": 2,
  "frequency": "每日3次",
  "status": 1
}
```

### 1.5 删除护理项目

**接口地址**: `DELETE /project/{id}`

### 1.6 批量删除护理项目

**接口地址**: `DELETE /project/batch`

**请求体**:

```json
[1, 2, 3]
```

### 1.7 更新护理项目状态

**接口地址**: `PUT /project/{id}/status`

**请求参数**:

| 参数名 | 类型    | 必填 | 说明                |
| ------ | ------- | ---- | ------------------- |
| status | Integer | 是   | 状态(1-启用,0-停用) |

### 1.8 获取项目分类选项

**接口地址**: `GET /project/categories`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ["健康监护", "生活照料", "护理照料", "功能训练", "医疗护理", "心理关怀"]
}
```

### 1.9 获取护理项目统计信息

**接口地址**: `GET /project/stats`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 20,
    "active": 18,
    "inactive": 2
  }
}
```

### 1.10 获取启用状态的护理项目

**接口地址**: `GET /project/active`

### 1.11 获取执行频率选项

**接口地址**: `GET /project/frequency-options`

### 1.12 根据护理等级推荐项目

**接口地址**: `GET /project/recommend`

**请求参数**:

| 参数名       | 类型   | 必填 | 说明     |
| ------------ | ------ | ---- | -------- |
| nursingLevel | String | 是   | 护理等级 |

------

## 📝 2. 护理记录管理

### 2.1 分页查询护理记录列表

**接口地址**: `GET /record/list`

**请求参数**:

| 参数名          | 类型    | 必填 | 说明       | 格式       |
| --------------- | ------- | ---- | ---------- | ---------- |
| searchKeyword   | String  | 否   | 搜索关键词 | -          |
| executionStatus | String  | 否   | 执行状态   | -          |
| startDate       | String  | 否   | 开始日期   | yyyy-MM-dd |
| endDate         | String  | 否   | 结束日期   | yyyy-MM-dd |
| customerId      | Integer | 否   | 客户ID     | -          |
| projectId       | Integer | 否   | 项目ID     | -          |
| caregiverId     | Integer | 否   | 护理人员ID | -          |
| page            | Integer | 否   | 页码       | 1          |
| size            | Integer | 否   | 每页数量   | 10         |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "customerId": 1,
        "customerName": "张三",
        "projectId": 1,
        "projectName": "生命体征监测",
        "projectCategory": "健康监护",
        "caregiverId": 2,
        "caregiverName": "张护士",
        "executionDate": "2024-06-19",
        "executionTime": "09:00:00",
        "duration": 15,
        "standardDuration": 15,
        "executionStatus": "已完成",
        "notes": "血压130/85，心率72，体温36.5℃",
        "createdAt": "2024-06-19T09:00:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10,
    "totalPages": 5
  }
}
```

### 2.2 添加护理记录

**接口地址**: `POST /record/add`

**请求体**:

```json
{
  "customerId": 1,
  "projectId": 1,
  "caregiverId": 2,
  "executionDate": "2024-06-19",
  "executionTime": "09:00:00",
  "duration": 15,
  "executionStatus": "已完成",
  "notes": "血压130/85，心率72，体温36.5℃"
}
```

### 2.3 更新护理记录信息

**接口地址**: `PUT /record/update`

### 2.4 删除护理记录

**接口地址**: `DELETE /record/{id}`

### 2.5 批量删除护理记录

**接口地址**: `DELETE /record/batch`

### 2.6 根据客户ID查询护理记录

**接口地址**: `GET /record/customer/{customerId}`

**请求参数**:

| 参数名 | 类型    | 必填 | 说明     |
| ------ | ------- | ---- | -------- |
| limit  | Integer | 否   | 限制数量 |

### 2.7 查询今日护理记录

**接口地址**: `GET /record/today`

**请求参数**:

| 参数名      | 类型    | 必填 | 说明       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 否   | 护理人员ID |

### 2.8 查询未完成的护理记录

**接口地址**: `GET /record/uncompleted`

### 2.9 获取护理记录统计信息

**接口地址**: `GET /record/stats`

**请求参数**:

| 参数名    | 类型   | 必填 | 说明     | 格式       |
| --------- | ------ | ---- | -------- | ---------- |
| startDate | String | 否   | 开始日期 | yyyy-MM-dd |
| endDate   | String | 否   | 结束日期 | yyyy-MM-dd |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 120,
    "completed": 100,
    "partialCompleted": 10,
    "unExecuted": 8,
    "abnormal": 2,
    "completionRate": 83.33
  }
}
```

### 2.10 获取护理人员工作量统计

**接口地址**: `GET /record/caregiver-workload`

### 2.11 快速记录护理

**接口地址**: `POST /record/quick-record`

**请求体**:

```json
{
  "customerId": 1,
  "projectId": 1,
  "caregiverId": 2,
  "notes": "快速记录备注"
}
```

### 2.12 获取执行状态选项

**接口地址**: `GET /record/status-options`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ["已完成", "部分完成", "未执行", "异常"]
}
```

------

## 🏅 3. 护理级别管理

### 3.1 获取护理等级选项

**接口地址**: `GET /level/options`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "level_code": "L001",
      "level_name": "一级护理",
      "service_content": "24小时专业护理、医疗监护、生活照料、康复训练",
      "monthly_fee": 8000.00
    },
    {
      "level_code": "L002",
      "level_name": "二级护理",
      "service_content": "日常护理、健康监测、生活协助、血糖监测",
      "monthly_fee": 5000.00
    },
    {
      "level_code": "L003",
      "level_name": "三级护理",
      "service_content": "基础护理、生活照料、健康指导",
      "monthly_fee": 3000.00
    }
  ]
}
```

### 3.2 根据客户ID查询有效的护理协议

**接口地址**: `GET /level/customer/{customerId}/active`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "id": 1,
    "customerId": 1,
    "levelName": "二级护理",
    "levelCode": "L002",
    "levelStatus": "生效",
    "monthlyFee": 5000.00,
    "serviceContent": "日常护理、健康监测、生活协助、血糖监测",
    "startDate": "2024-01-15",
    "endDate": null,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

### 3.3 根据客户ID查询所有护理协议

**接口地址**: `GET /level/customer/{customerId}/all`

### 3.4 创建护理协议

**接口地址**: `POST /level/create`

**请求体**:

```json
{
  "customerId": 1,
  "levelName": "二级护理",
  "levelCode": "L002",
  "levelStatus": "生效",
  "monthlyFee": 5000.00,
  "serviceContent": "日常护理、健康监测、生活协助、血糖监测",
  "startDate": "2024-06-19",
  "endDate": null
}
```

### 3.5 更新护理协议信息

**接口地址**: `PUT /level/update`

### 3.6 删除护理协议

**接口地址**: `DELETE /level/{id}`

### 3.7 更新护理协议状态

**接口地址**: `PUT /level/{id}/status`

**请求参数**:

| 参数名 | 类型   | 必填 | 说明                 |
| ------ | ------ | ---- | -------------------- |
| status | String | 是   | 状态(生效/暂停/终止) |

### 3.8 护理等级升级

**接口地址**: `POST /level/upgrade`

**请求体**:

```json
{
  "customerId": 1,
  "newLevelCode": "L001",
  "newLevelName": "一级护理",
  "newMonthlyFee": 8000.00,
  "serviceContent": "24小时专业护理、医疗监护、生活照料、康复训练",
  "reason": "客户身体状况需要更高级别护理"
}
```

### 3.9 护理等级降级

**接口地址**: `POST /level/downgrade`

### 3.10 暂停护理服务

**接口地址**: `POST /level/suspend`

**请求体**:

```json
{
  "customerId": 1,
  "reason": "客户临时外出"
}
```

### 3.11 恢复护理服务

**接口地址**: `POST /level/resume`

**请求参数**:

| 参数名     | 类型    | 必填 | 说明   |
| ---------- | ------- | ---- | ------ |
| customerId | Integer | 是   | 客户ID |

### 3.12 续签护理协议

**接口地址**: `POST /level/renew`

**请求体**:

```json
{
  "customerId": 1,
  "newEndDate": "2025-12-31",
  "newMonthlyFee": 5200.00
}
```

### 3.13 查询即将到期的协议

**接口地址**: `GET /level/expiring`

**请求参数**:

| 参数名 | 类型    | 必填 | 说明     | 默认值 |
| ------ | ------- | ---- | -------- | ------ |
| days   | Integer | 否   | 提前天数 | 30     |

### 3.14 获取护理协议统计信息

**接口地址**: `GET /level/stats`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalActive": 50,
    "totalSuspended": 5,
    "totalTerminated": 10,
    "total": 65,
    "levelStats": {
      "一级护理": {"生效": 15, "暂停": 2, "终止": 3},
      "二级护理": {"生效": 25, "暂停": 2, "终止": 5},
      "三级护理": {"生效": 10, "暂停": 1, "终止": 2}
    }
  }
}
```

### 3.15 获取护理等级分布统计

**接口地址**: `GET /level/distribution`

------

## 👥 4. 客户护理设置

### 4.1 根据客户ID查询护理人员分配

**接口地址**: `GET /care/customer/{customerId}/assignments`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "张三",
      "caregiverId": 2,
      "caregiverName": "张护士",
      "assignmentDate": "2024-01-15",
      "endDate": null,
      "primaryCaregiver": 1,
      "status": 1,
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    }
  ]
}
```

### 4.2 根据护理人员ID查询客户分配

**接口地址**: `GET /care/caregiver/{caregiverId}/assignments`

### 4.3 创建护理人员分配

**接口地址**: `POST /care/assignment/create`

**请求体**:

```json
{
  "customerId": 1,
  "caregiverId": 2,
  "assignmentDate": "2024-06-19",
  "primaryCaregiver": 1,
  "status": 1
}
```

### 4.4 更新护理人员分配

**接口地址**: `PUT /care/assignment/update`

### 4.5 删除护理人员分配

**接口地址**: `DELETE /care/assignment/{id}`

### 4.6 查询客户的主要护理人员

**接口地址**: `GET /care/customer/{customerId}/primary-caregiver`

### 4.7 设置主要护理人员

**接口地址**: `POST /care/customer/{customerId}/primary-caregiver`

**请求参数**:

| 参数名      | 类型    | 必填 | 说明       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

### 4.8 批量分配护理人员

**接口地址**: `POST /care/customer/{customerId}/batch-assign`

**请求体**:

```json
{
  "caregiverIds": [2, 3, 4],
  "primaryCaregiverId": 2
}
```

### 4.9 更换护理人员

**接口地址**: `POST /care/customer/{customerId}/replace-caregivers`

### 4.10 获取护理人员工作负载统计

**接口地址**: `GET /care/caregiver/workload-stats`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "caregiver_id": 2,
      "caregiver_name": "张护士",
      "phone": "13800138001",
      "role_name": "健康管家",
      "customer_count": 5,
      "primary_count": 3
    }
  ]
}
```

### 4.11 获取可用护理人员列表

**接口地址**: `GET /care/available-caregivers`

### 4.12 护理人员分配统计

**接口地址**: `GET /care/assignment-stats`

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalCaregivers": 10,
    "activeCaregivers": 8,
    "totalCustomers": 25,
    "averageCustomersPerCaregiver": 3
  }
}
```

### 4.13 智能护理人员推荐

**接口地址**: `GET /care/recommend-caregivers`

**请求参数**:

| 参数名       | 类型    | 必填 | 说明     |
| ------------ | ------- | ---- | -------- |
| customerId   | Integer | 是   | 客户ID   |
| nursingLevel | String  | 否   | 护理等级 |

### 4.14 检查护理人员是否可以分配

**接口地址**: `GET /care/caregiver/{caregiverId}/can-assign`

**请求参数**:

| 参数名       | 类型    | 必填 | 说明       | 默认值 |
| ------------ | ------- | ---- | ---------- | ------ |
| maxCustomers | Integer | 否   | 最大客户数 | 10     |

### 4.15 获取护理团队配置

**接口地址**: `GET /care/customer/{customerId}/team-config`

------

## ❌ 错误码说明

| 错误码 | 说明           |
| ------ | -------------- |
| 200    | 操作成功       |
| 400    | 请求参数错误   |
| 404    | 资源不存在     |
| 500    | 服务器内部错误 |

## 📝 注意事项

1. 所有日期格式统一使用 `yyyy-MM-dd`
2. 所有时间格式统一使用 `HH:mm:ss`
3. 分页查询的页码从1开始
4. 删除操作需要确认相关数据无关联
5. 状态更新操作会自动更新 `updated_at` 字段
6. 批量操作失败时会回滚所有操作