
export const BedTypeOptions = [
    { label: '普通床', value: '普通床' },
    { label: '电动床', value: '电动床' },
    { label: '护理床', value: '护理床' }
]

// ==================== 护理项目相关类型 ====================

// 护理项目实体接口
export interface NursingProject {
    id?: number
    projectName: string
    projectCategory: string
    description?: string
    standardDuration: number
    caregiverId?: number
    caregiverName?: string
    frequency: string
    status: number
    createdAt?: string
    updatedAt?: string
}

// 护理项目查询参数接口
export interface NursingProjectQueryParams {
    searchKeyword?: string
    projectCategory?: string
    status?: number
    caregiverId?: number
    page?: number
    size?: number
}

// 护理项目列表响应接口
export interface NursingProjectListResponse {
    projects: NursingProject[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 护理项目统计接口
export interface NursingProjectStats {
    total: number
    active: number
    inactive: number
}

// 护理项目表单接口
export interface NursingProjectForm {
    id?: number
    projectName: string
    projectCategory: string
    description?: string
    standardDuration: number
    caregiverId?: number
    frequency: string
    status: number
}

// 护理项目分类选项
export const NursingProjectCategories = [
    '健康监护',
    '生活照料',
    '护理照料',
    '功能训练',
    '医疗护理',
    '心理关怀'
]

// 执行频率选项
export const FrequencyOptions = [
    '每日1次',
    '每日2次',
    '每日3次',
    '每2小时1次',
    '每周3次',
    '按医嘱'
]