// 护理记录实体接口
export interface NursingRecord {
    id?: number
    customerId: number
    customerName: string
    projectId: number
    projectName: string
    projectCategory: string
    caregiverId: number
    caregiverName: string
    executionDate: string
    executionTime?: string
    duration?: number
    standardDuration?: number
    executionStatus: string
    notes?: string
    createdAt?: string
}

// 护理记录查询参数接口
export interface NursingRecordQueryParams {
    searchKeyword?: string
    executionStatus?: string
    startDate?: string
    endDate?: string
    customerId?: number
    projectId?: number
    caregiverId?: number
    page?: number
    size?: number
}

// 护理记录列表响应接口
export interface NursingRecordListResponse {
    records: NursingRecord[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 护理记录统计接口
export interface NursingRecordStats {
    total: number
    completed: number
    partialCompleted: number
    unExecuted: number
    abnormal: number
    completionRate: number
}

// 护理记录表单接口
export interface NursingRecordForm {
    id?: number
    customerId?: number
    projectId?: number
    caregiverId?: number
    executionDate: string
    executionTime?: string
    duration?: number
    executionStatus: string
    notes?: string
}

// 护理记录执行状态选项
export const NursingRecordStatusOptions = [
    '已完成',
    '部分完成',
    '未执行',
    '异常'
]