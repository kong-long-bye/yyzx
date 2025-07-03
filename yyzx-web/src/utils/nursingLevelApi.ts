import type {
    ApiResponse,
    NursingAgreement,
    NursingLevel,
    Customer,
    AgreementQueryParams,
    AgreementListResponse,
    AgreementStats,
    AgreementForm,
    UpgradeForm,
    RenewForm
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

// 护理级别管理API
export const nursingLevelApi = {
    // 获取护理等级选项
    getNursingLevels: (): Promise<ApiResponse<NursingLevel[]>> => {
        return request<NursingLevel[]>('/level/options')
    },

    // 分页查询护理协议列表
    getAgreementList: (params: AgreementQueryParams): Promise<ApiResponse<AgreementListResponse>> => {
        const queryString = new URLSearchParams()

        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.levelStatus) queryString.append('levelStatus', params.levelStatus)
        if (params.levelCode) queryString.append('levelCode', params.levelCode)
        if (params.customerId) queryString.append('customerId', params.customerId.toString())
        if (params.startDate) queryString.append('startDate', params.startDate)
        if (params.endDate) queryString.append('endDate', params.endDate)
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/level/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<AgreementListResponse>(url)
    },

    // 根据客户ID查询有效的护理协议
    getActiveAgreementByCustomerId: (customerId: number): Promise<ApiResponse<NursingAgreement>> => {
        return request<NursingAgreement>(`/level/customer/${customerId}/active`)
    },

    // 根据客户ID查询所有护理协议
    getAllAgreementsByCustomerId: (customerId: number): Promise<ApiResponse<NursingAgreement[]>> => {
        return request<NursingAgreement[]>(`/level/customer/${customerId}/all`)
    },

    // 创建护理协议
    createAgreement: (agreement: AgreementForm): Promise<ApiResponse<string>> => {
        return request<string>('/level/create', {
            method: 'POST',
            body: JSON.stringify(agreement)
        })
    },

    // 更新护理协议信息
    updateAgreement: (agreement: AgreementForm): Promise<ApiResponse<string>> => {
        return request<string>('/level/update', {
            method: 'PUT',
            body: JSON.stringify(agreement)
        })
    },

    // 删除护理协议
    deleteAgreement: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/level/${id}`, {
            method: 'DELETE'
        })
    },

    // 更新护理协议状态
    updateAgreementStatus: (id: number, status: string): Promise<ApiResponse<string>> => {
        return request<string>(`/level/${id}/status?status=${encodeURIComponent(status)}`, {
            method: 'PUT'
        })
    },

    // 护理等级升级
    upgradeLevel: (upgradeData: UpgradeForm): Promise<ApiResponse<string>> => {
        return request<string>('/level/upgrade', {
            method: 'POST',
            body: JSON.stringify(upgradeData)
        })
    },

    // 护理等级降级
    downgradeLevel: (downgradeData: UpgradeForm): Promise<ApiResponse<string>> => {
        return request<string>('/level/downgrade', {
            method: 'POST',
            body: JSON.stringify(downgradeData)
        })
    },

    // 暂停护理服务
    suspendService: (customerId: number, reason: string): Promise<ApiResponse<string>> => {
        return request<string>('/level/suspend', {
            method: 'POST',
            body: JSON.stringify({ customerId, reason })
        })
    },

    // 恢复护理服务
    resumeService: (customerId: number): Promise<ApiResponse<string>> => {
        return request<string>(`/level/resume?customerId=${customerId}`, {
            method: 'POST'
        })
    },

    // 续签护理协议
    renewAgreement: (renewData: RenewForm): Promise<ApiResponse<string>> => {
        return request<string>('/level/renew', {
            method: 'POST',
            body: JSON.stringify(renewData)
        })
    },

    // 查询即将到期的协议
    getExpiringAgreements: (days: number = 30): Promise<ApiResponse<NursingAgreement[]>> => {
        return request<NursingAgreement[]>(`/level/expiring?days=${days}`)
    },

    // 获取护理协议统计信息
    getAgreementStats: (): Promise<ApiResponse<AgreementStats>> => {
        return request<AgreementStats>('/level/stats')
    },

    // 获取护理等级分布统计
    getLevelDistribution: (): Promise<ApiResponse<any>> => {
        return request<any>('/level/distribution')
    },

    // 获取客户列表（用于下拉选择）
    getCustomerList: (): Promise<ApiResponse<Customer[]>> => {
        return request<Customer[]>('/level/customers')
    },

    // 检查客户是否已有有效协议
    checkCustomerActiveAgreement: (customerId: number): Promise<ApiResponse<boolean>> => {
        return request<boolean>(`/level/customer/${customerId}/check-active`)
    }
}

export default nursingLevelApi