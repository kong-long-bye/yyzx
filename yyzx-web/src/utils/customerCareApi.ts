import type {
    ApiResponse,
    CustomerCareAssignment,
    CustomerCareAssignmentForm,
    CustomerCareStats,
    CaregiverWorkload,
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

// 客户护理设置相关API
export const customerCareApi = {
    // 根据客户ID查询护理人员分配
    getCustomerAssignments: (customerId: number): Promise<ApiResponse<CustomerCareAssignment[]>> => {
        return request<CustomerCareAssignment[]>(`/care/customer/${customerId}/assignments`)
    },

    // 根据护理人员ID查询客户分配
    getCaregiverAssignments: (caregiverId: number): Promise<ApiResponse<CustomerCareAssignment[]>> => {
        return request<CustomerCareAssignment[]>(`/care/caregiver/${caregiverId}/assignments`)
    },

    // 获取所有分配关系（需要后端提供这个接口）
    getAllAssignments: (): Promise<ApiResponse<CustomerCareAssignment[]>> => {
        return request<CustomerCareAssignment[]>('/care/assignments/all')
    },

    // 创建护理人员分配
    createAssignment: (assignment: CustomerCareAssignmentForm): Promise<ApiResponse<string>> => {
        return request<string>('/care/assignment/create', {
            method: 'POST',
            body: JSON.stringify(assignment)
        })
    },

    // 更新护理人员分配
    updateAssignment: (assignment: CustomerCareAssignmentForm): Promise<ApiResponse<string>> => {
        return request<string>('/care/assignment/update', {
            method: 'PUT',
            body: JSON.stringify(assignment)
        })
    },

    // 删除护理人员分配
    deleteAssignment: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/care/assignment/${id}`, {
            method: 'DELETE'
        })
    },

    // 查询客户的主要护理人员
    getPrimaryCaregiver: (customerId: number): Promise<ApiResponse<CustomerCareAssignment>> => {
        return request<CustomerCareAssignment>(`/care/customer/${customerId}/primary-caregiver`)
    },

    // 设置主要护理人员
    setPrimaryCaregiver: (customerId: number, caregiverId: number): Promise<ApiResponse<string>> => {
        return request<string>(`/care/customer/${customerId}/primary-caregiver?caregiverId=${caregiverId}`, {
            method: 'POST'
        })
    },

    // 批量分配护理人员
    batchAssignCaregivers: (customerId: number, caregiverIds: number[], primaryCaregiverId?: number): Promise<ApiResponse<string>> => {
        const requestBody = {
            caregiverIds,
            primaryCaregiverId
        }
        return request<string>(`/care/customer/${customerId}/batch-assign`, {
            method: 'POST',
            body: JSON.stringify(requestBody)
        })
    },

    // 更换护理人员
    replaceCaregivers: (customerId: number, newCaregiverIds: number[], primaryCaregiverId?: number): Promise<ApiResponse<string>> => {
        const requestBody = {
            caregiverIds: newCaregiverIds,
            primaryCaregiverId
        }
        return request<string>(`/care/customer/${customerId}/replace-caregivers`, {
            method: 'POST',
            body: JSON.stringify(requestBody)
        })
    },

    // 获取护理人员工作负载统计
    getCaregiverWorkloadStats: (): Promise<ApiResponse<CaregiverWorkload[]>> => {
        return request<CaregiverWorkload[]>('/care/caregiver/workload-stats')
    },

    // 获取可用护理人员列表
    getAvailableCaregivers: (): Promise<ApiResponse<User[]>> => {
        return request<User[]>('/care/available-caregivers')
    },

    // 护理人员分配统计
    getAssignmentStats: (): Promise<ApiResponse<CustomerCareStats>> => {
        return request<CustomerCareStats>('/care/assignment-stats')
    },

    // 智能护理人员推荐
    getRecommendedCaregivers: (customerId: number, nursingLevel?: string): Promise<ApiResponse<User[]>> => {
        const url = `/care/recommend-caregivers?customerId=${customerId}${nursingLevel ? '&nursingLevel=' + encodeURIComponent(nursingLevel) : ''}`
        return request<User[]>(url)
    },

    // 检查护理人员是否可以分配
    canAssignCaregiver: (caregiverId: number, maxCustomers: number = 10): Promise<ApiResponse<boolean>> => {
        return request<boolean>(`/care/caregiver/${caregiverId}/can-assign?maxCustomers=${maxCustomers}`)
    },

    // 获取护理团队配置
    getTeamConfig: (customerId: number): Promise<ApiResponse<any>> => {
        return request<any>(`/care/customer/${customerId}/team-config`)
    },

    // 获取客户列表（用于下拉选择）
    getCustomerList: (): Promise<ApiResponse<Customer[]>> => {
        // 这里可能需要调用客户管理API
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

export default customerCareApi