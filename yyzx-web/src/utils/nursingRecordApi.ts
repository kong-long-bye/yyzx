import type {
    ApiResponse,
    NursingRecord,
    NursingRecordQueryParams,
    NursingRecordListResponse,
    NursingRecordStats,
    NursingRecordForm,
    NursingProject,
    Customer,
    User
} from '@/types'

// API基础URL
const BASE_URL = 'http://localhost:8080/api/nursing'

// 封装请求方法
const request = async <T = any>(
    url: string,
    options: RequestInit = {}
): Promise<ApiResponse<T>> => {
    const config: RequestInit = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        },
        ...options
    }

    try {
        const response = await fetch(`${BASE_URL}${url}`, config)
        const data: ApiResponse<T> = await response.json()
        return data
    } catch (error) {
        console.error('请求失败:', error)
        throw error
    }
}

// 护理记录相关API
export const nursingRecordApi = {
    // 分页查询护理记录列表
    getRecordList: (params: NursingRecordQueryParams): Promise<ApiResponse<NursingRecordListResponse>> => {
        const queryString = new URLSearchParams()

        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.executionStatus) queryString.append('executionStatus', params.executionStatus)
        if (params.startDate) queryString.append('startDate', params.startDate)
        if (params.endDate) queryString.append('endDate', params.endDate)
        if (params.customerId) queryString.append('customerId', params.customerId.toString())
        if (params.projectId) queryString.append('projectId', params.projectId.toString())
        if (params.caregiverId) queryString.append('caregiverId', params.caregiverId.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/record/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<NursingRecordListResponse>(url)
    },

    // 添加护理记录
    addRecord: (record: NursingRecordForm): Promise<ApiResponse<string>> => {
        return request<string>('/record/add', {
            method: 'POST',
            body: JSON.stringify(record)
        })
    },

    // 更新护理记录信息
    updateRecord: (record: NursingRecordForm): Promise<ApiResponse<string>> => {
        return request<string>('/record/update', {
            method: 'PUT',
            body: JSON.stringify(record)
        })
    },

    // 删除护理记录
    deleteRecord: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/record/${id}`, {
            method: 'DELETE'
        })
    },

    // 批量删除护理记录
    deleteRecords: (ids: number[]): Promise<ApiResponse<string>> => {
        return request<string>('/record/batch', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        })
    },

    // 根据客户ID查询护理记录
    getRecordsByCustomer: (customerId: number, limit?: number): Promise<ApiResponse<NursingRecord[]>> => {
        const url = `/record/customer/${customerId}${limit ? '?limit=' + limit : ''}`
        return request<NursingRecord[]>(url)
    },

    // 查询今日护理记录
    getTodayRecords: (caregiverId?: number): Promise<ApiResponse<NursingRecord[]>> => {
        const url = `/record/today${caregiverId ? '?caregiverId=' + caregiverId : ''}`
        return request<NursingRecord[]>(url)
    },

    // 查询未完成的护理记录
    getUncompletedRecords: (): Promise<ApiResponse<NursingRecord[]>> => {
        return request<NursingRecord[]>('/record/uncompleted')
    },

    // 获取护理记录统计信息
    getRecordStats: (startDate?: string, endDate?: string): Promise<ApiResponse<NursingRecordStats>> => {
        const queryString = new URLSearchParams()
        if (startDate) queryString.append('startDate', startDate)
        if (endDate) queryString.append('endDate', endDate)

        const url = `/record/stats${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<NursingRecordStats>(url)
    },

    // 获取护理人员工作量统计
    getCaregiverWorkload: (): Promise<ApiResponse<any[]>> => {
        return request<any[]>('/record/caregiver-workload')
    },

    // 快速记录护理
    quickRecord: (quickData: {
        customerId: number,
        projectId: number,
        caregiverId?: number,
        notes: string
    }): Promise<ApiResponse<string>> => {
        return request<string>('/record/quick-record', {
            method: 'POST',
            body: JSON.stringify(quickData)
        })
    },

    // 获取执行状态选项
    getStatusOptions: (): Promise<ApiResponse<string[]>> => {
        return request<string[]>('/record/status-options')
    },

    // 获取启用的护理项目列表
    getActiveProjects: (): Promise<ApiResponse<NursingProject[]>> => {
        return request<NursingProject[]>('/project/active')
    },

    // 获取客户列表
    getCustomerList: (): Promise<ApiResponse<Customer[]>> => {
        return fetch('http://localhost:8080/api/customer/list', {
            headers: {
                'Content-Type': 'application/json',
            }
        }).then(response => response.json()).catch(() => {
            // 如果接口不存在，返回模拟数据
            return {
                code: 404,
                msg: '获取失败',
                data: [

                ]
            }
        })
    },

    // 获取护理人员列表
    getCaregiverList: (): Promise<ApiResponse<User[]>> => {
        return fetch('http://localhost:8080/api/user/caregivers', {
            headers: {
                'Content-Type': 'application/json',
            }
        }).then(response => response.json()).catch(() => {
            // 如果接口不存在，返回模拟数据
            return {
                code: 404,
                msg: '获取失败',
                data: [

                ]
            }
        })
    }
}

export default nursingRecordApi