# 健康管家模块API文档

## 概述

健康管家模块提供护理人员管理服务对象和设置客户关注的功能，包括两个主要子模块：

- **设置服务对象 (SERVICE_ASSIGNMENT)**: 管理护理人员负责的客户
- **服务关注 (SERVICE_FOCUS)**: 管理需要特别关注的客户

**API Base URL**: `/api/health`

**通用响应格式**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

------

## 一、设置服务对象 (SERVICE_ASSIGNMENT)

### 1.1 获取服务对象列表

**接口**: `GET /api/health/assignment/customers`

**描述**: 获取护理人员负责的客户列表（分页）

**请求参数**:

| 参数名        | 类型    | 必填 | 描述                             |
| ------------- | ------- | ---- | -------------------------------- |
| caregiverId   | Integer | 是   | 护理人员ID                       |
| searchKeyword | String  | 否   | 搜索关键字（客户姓名、床位号等） |
| auditStatus   | String  | 否   | 审核状态                         |
| page          | Integer | 否   | 页码，默认1                      |
| size          | Integer | 否   | 每页大小，默认10                 |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "customers": [
      {
        "id": 1,
        "name": "张三",
        "idCard": "110101195001011234",
        "gender": "男",
        "phone": "13901234567",
        "bedNumber": "A101-1",
        "roomNumber": "A101",
        "nursingLevel": "二级护理",
        "primaryCaregiver": "张护士"
      }
    ],
    "total": 15,
    "page": 1,
    "size": 10,
    "totalPages": 2
  }
}
```

### 1.2 获取工作概览

**接口**: `GET /api/health/assignment/overview`

**描述**: 获取护理人员的工作概览统计

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "totalCustomers": 15,
    "primaryCustomers": 8,
    "todayRecordCount": 25,
    "weekRecordCount": 120,
    "uncompletedCount": 3
  }
}
```

### 1.3 获取客户护理详情

**接口**: `GET /api/health/assignment/customer/{customerId}/detail`

**描述**: 获取特定客户的详细护理信息

**路径参数**:

| 参数名     | 类型    | 必填 | 描述   |
| ---------- | ------- | ---- | ------ |
| customerId | Integer | 是   | 客户ID |

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "customer": {
      "id": 1,
      "name": "张三",
      "gender": "男",
      "bedNumber": "A101-1"
    },
    "assignment": {
      "id": 1,
      "primaryCaregiver": 1,
      "assignmentDate": "2024-01-15"
    },
    "recentRecords": [],
    "careStats": {}
  }
}
```

### 1.4 申请服务新客户

**接口**: `POST /api/health/assignment/apply`

**描述**: 护理人员申请服务新客户

**请求体**:

```json
{
  "caregiverId": 2,
  "customerId": 5,
  "reason": "客户需要专业护理服务"
}
```

**响应示例**:

```json
{
  "code": 200,
  "msg": "申请服务客户成功",
  "data": null
}
```

### 1.5 移交客户

**接口**: `POST /api/health/assignment/transfer`

**描述**: 将客户移交给其他护理人员

**请求体**:

```json
{
  "customerId": 1,
  "fromCaregiverId": 2,
  "toCaregiverId": 3,
  "reason": "工作调整需要移交"
}
```

**响应示例**:

```json
{
  "code": 200,
  "msg": "移交客户成功",
  "data": null
}
```

### 1.6 申请主要护理人员

**接口**: `POST /api/health/assignment/apply-primary`

**描述**: 申请成为客户的主要护理人员

**请求体**:

```json
{
  "customerId": 1,
  "caregiverId": 2
}
```

### 1.7 获取可申请客户列表

**接口**: `GET /api/health/assignment/available-customers`

**描述**: 获取护理人员可申请服务的客户列表

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

### 1.8 获取工作统计

**接口**: `GET /api/health/assignment/work-stats`

**描述**: 获取护理人员工作统计数据

**请求参数**:

| 参数名      | 类型    | 必填 | 描述                  |
| ----------- | ------- | ---- | --------------------- |
| caregiverId | Integer | 是   | 护理人员ID            |
| startDate   | Date    | 否   | 开始日期 (yyyy-MM-dd) |
| endDate     | Date    | 否   | 结束日期 (yyyy-MM-dd) |

### 1.9 获取工作提醒

**接口**: `GET /api/health/assignment/work-reminders`

**描述**: 获取护理人员的工作提醒信息

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

------

## 二、服务关注 (SERVICE_FOCUS)

### 2.1 获取关注列表

**接口**: `GET /api/health/focus/list`

**描述**: 分页查询客户关注列表

**请求参数**:

| 参数名        | 类型    | 必填 | 描述                       |
| ------------- | ------- | ---- | -------------------------- |
| searchKeyword | String  | 否   | 搜索关键字                 |
| focusLevel    | String  | 否   | 关注级别（高、中、低）     |
| status        | Integer | 否   | 状态（1-关注中，0-已结束） |
| caregiverId   | Integer | 否   | 护理人员ID                 |
| startDate     | Date    | 否   | 开始日期                   |
| endDate       | Date    | 否   | 结束日期                   |
| page          | Integer | 否   | 页码，默认1                |
| size          | Integer | 否   | 每页大小，默认10           |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "focuses": [
      {
        "id": 1,
        "customerId": 1,
        "caregiverId": 2,
        "focusLevel": "高",
        "focusReason": "血糖控制需要密切观察",
        "focusContent": "客户糖尿病需要特别关注血糖变化",
        "startDate": "2024-06-15",
        "endDate": "2024-07-15",
        "status": 1,
        "customerName": "张三",
        "caregiverName": "张护士",
        "bedNumber": "A101-1",
        "roomNumber": "A101"
      }
    ],
    "total": 6,
    "page": 1,
    "size": 10,
    "totalPages": 1
  }
}
```

### 2.2 获取关注详情

**接口**: `GET /api/health/focus/{id}`

**描述**: 根据ID查询客户关注详细信息

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

### 2.3 添加客户关注

**接口**: `POST /api/health/focus/add`

**描述**: 添加新的客户关注记录

**请求体**:

```json
{
  "customerId": 1,
  "caregiverId": 2,
  "focusLevel": "高",
  "focusReason": "血糖控制需要密切观察",
  "focusContent": "客户糖尿病需要特别关注血糖变化，定时监测血糖值",
  "startDate": "2024-06-15",
  "endDate": "2024-07-15"
}
```

**响应示例**:

```json
{
  "code": 200,
  "msg": "添加客户关注成功",
  "data": null
}
```

### 2.4 更新客户关注

**接口**: `PUT /api/health/focus/update`

**描述**: 更新客户关注信息

**请求体**:

```json
{
  "id": 1,
  "customerId": 1,
  "caregiverId": 2,
  "focusLevel": "中",
  "focusReason": "更新后的关注原因",
  "focusContent": "更新后的关注内容",
  "startDate": "2024-06-15",
  "endDate": "2024-08-15"
}
```

### 2.5 删除客户关注

**接口**: `DELETE /api/health/focus/{id}`

**描述**: 删除客户关注记录

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

### 2.6 批量删除关注

**接口**: `DELETE /api/health/focus/batch`

**描述**: 批量删除客户关注记录

**请求体**:

```json
[1, 2, 3, 4]
```

### 2.7 更新关注状态

**接口**: `PUT /api/health/focus/{id}/status`

**描述**: 更新客户关注状态

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

**请求参数**:

| 参数名 | 类型    | 必填 | 描述                       |
| ------ | ------- | ---- | -------------------------- |
| status | Integer | 是   | 状态（1-关注中，0-已结束） |

### 2.8 根据护理人员查询关注

**接口**: `GET /api/health/focus/caregiver/{caregiverId}`

**描述**: 查询护理人员的关注客户列表

**路径参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

**请求参数**:

| 参数名 | 类型    | 必填 | 描述     |
| ------ | ------- | ---- | -------- |
| status | Integer | 否   | 状态过滤 |

### 2.9 获取关注统计

**接口**: `GET /api/health/focus/stats`

**描述**: 获取关注统计信息

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |
| startDate   | Date    | 否   | 开始日期   |
| endDate     | Date    | 否   | 结束日期   |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 15,
    "active": 12,
    "ended": 3
  }
}
```

### 2.10 查询即将到期关注

**接口**: `GET /api/health/focus/expiring`

**描述**: 查询即将到期的关注记录

**请求参数**:

| 参数名      | 类型    | 必填 | 描述              |
| ----------- | ------- | ---- | ----------------- |
| days        | Integer | 否   | 提前天数，默认7天 |
| caregiverId | Integer | 是   | 护理人员ID        |

### 2.11 查询高优先级关注

**接口**: `GET /api/health/focus/high-priority`

**描述**: 查询高关注级别的客户

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

### 2.12 关注级别统计

**接口**: `GET /api/health/focus/stats-by-level`

**描述**: 按关注级别统计数据

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "levelStats": {
      "高": 3,
      "中": 5,
      "低": 2
    },
    "high": 3,
    "medium": 5,
    "low": 2
  }
}
```

### 2.13 设置关注到期

**接口**: `POST /api/health/focus/{id}/expire`

**描述**: 设置关注记录到期

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

**请求参数**:

| 参数名  | 类型 | 必填 | 描述                  |
| ------- | ---- | ---- | --------------------- |
| endDate | Date | 是   | 结束日期 (yyyy-MM-dd) |

### 2.14 延长关注期限

**接口**: `POST /api/health/focus/{id}/extend`

**描述**: 延长关注期限

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

**请求参数**:

| 参数名     | 类型 | 必填 | 描述                      |
| ---------- | ---- | ---- | ------------------------- |
| newEndDate | Date | 是   | 新的结束日期 (yyyy-MM-dd) |

### 2.15 升级关注级别

**接口**: `POST /api/health/focus/{id}/upgrade`

**描述**: 升级关注级别

**路径参数**:

| 参数名 | 类型    | 必填 | 描述       |
| ------ | ------- | ---- | ---------- |
| id     | Integer | 是   | 关注记录ID |

**请求体**:

```json
{
  "newLevel": "高",
  "reason": "客户病情变化，需要提高关注级别"
}
```

### 2.16 获取关注级别选项

**接口**: `GET /api/health/focus/level-options`

**描述**: 获取关注级别选项列表

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ["高", "中", "低"]
}
```

### 2.17 获取关注原因模板

**接口**: `GET /api/health/focus/reason-templates`

**描述**: 获取关注原因模板列表

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    "身体状况需要密切观察",
    "心理状态不稳定",
    "用药需要特别注意",
    "家属特别要求",
    "新入住适应期",
    "康复训练关键期",
    "慢性疾病管理",
    "跌倒风险较高",
    "认知功能下降",
    "其他原因"
  ]
}
```

### 2.18 智能关注建议

**接口**: `GET /api/health/focus/recommendations`

**描述**: 获取智能关注建议

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

### 2.19 关注提醒检查

**接口**: `GET /api/health/focus/alerts`

**描述**: 检查关注相关的提醒信息

**请求参数**:

| 参数名      | 类型    | 必填 | 描述       |
| ----------- | ------- | ---- | ---------- |
| caregiverId | Integer | 是   | 护理人员ID |

**响应示例**:

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "expiring": [],
    "highPriority": [],
    "longTerm": []
  }
}
```

### 2.20 批量更新关注状态

**接口**: `PUT /api/health/focus/batch-status`

**描述**: 批量更新关注状态

**请求体**:

```json
{
  "ids": [1, 2, 3],
  "status": 0
}
```

------

## 三、错误码说明

| 错误码 | 描述           |
| ------ | -------------- |
| 200    | 操作成功       |
| 400    | 请求参数错误   |
| 401    | 未授权访问     |
| 403    | 权限不足       |
| 404    | 资源不存在     |
| 500    | 服务器内部错误 |

**错误响应格式**:

```json
{
  "code": 500,
  "msg": "操作失败：具体错误信息",
  "data": null
}
```

------

## 四、数据结构说明

### 4.1 Customer（客户）

```json
{
  "id": 1,
  "name": "张三",
  "idCard": "110101195001011234",
  "gender": "男",
  "birthDate": "1950-01-01",
  "phone": "13901234567",
  "emergencyContact": "张小明",
  "emergencyPhone": "13801234567",
  "bedId": 1,
  "bedNumber": "A101-1",
  "roomNumber": "A101",
  "nursingLevel": "二级护理",
  "primaryCaregiver": "张护士",
  "admissionDate": "2024-01-15",
  "auditStatus": "已通过",
  "status": 1
}
```

### 4.2 CustomerFocus（客户关注）

```json
{
  "id": 1,
  "customerId": 1,
  "caregiverId": 2,
  "focusLevel": "高",
  "focusReason": "血糖控制需要密切观察",
  "focusContent": "客户糖尿病需要特别关注血糖变化",
  "startDate": "2024-06-15",
  "endDate": "2024-07-15",
  "status": 1,
  "customerName": "张三",
  "caregiverName": "张护士",
  "bedNumber": "A101-1",
  "roomNumber": "A101",
  "createdAt": "2024-06-15T10:30:00",
  "updatedAt": "2024-06-15T10:30:00"
}
```

### 4.3 CustomerCaregiverAssignment（客户护理分配）

```json
{
  "id": 1,
  "customerId": 1,
  "caregiverId": 2,
  "assignmentDate": "2024-01-15",
  "endDate": null,
  "primaryCaregiver": 1,
  "status": 1,
  "customerName": "张三",
  "caregiverName": "张护士"
}
```

------

## 五、使用示例

### 5.1 护理人员查看服务对象

```bash
GET /api/health/assignment/customers?caregiverId=2&page=1&size=10
```

### 5.2 添加客户关注

```bash
POST /api/health/focus/add
Content-Type: application/json

{
  "customerId": 1,
  "caregiverId": 2,
  "focusLevel": "高",
  "focusReason": "血糖控制需要密切观察",
  "startDate": "2024-06-15"
}
```

### 5.3 查看关注提醒

```bash
GET /api/health/focus/alerts?caregiverId=2
```

------

## 六、注意事项

1. **权限控制**: 护理人员只能查看和操作自己负责的客户
2. **数据一致性**: 客户状态变更时需要同步更新相关关注记录
3. **日期格式**: 所有日期参数使用 `yyyy-MM-dd` 格式
4. **分页查询**: 大数据量接口都支持分页，建议合理设置页面大小
5. **错误处理**: 客户端需要根据响应码进行相应的错误处理
6. **接口幂等性**: 更新和删除操作具有幂等性，重复调用不会产生副作用

------

**文档版本**: v1.0
 **最后更新**: 2024-06-25
 **联系人**: 开发团队