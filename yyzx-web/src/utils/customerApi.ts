// src/utils/customerApi.ts
import type {
    ApiResponse,
    Customer,
    OutingRecord,
    CheckoutInfo,
    CustomerQueryParams,
    OutingQueryParams,
    CheckoutQueryParams,
    CustomerStats,
    OutingStats,
    BedOption,
    CaregiverOption
} from '@/types'

const BASE_URL = 'http://localhost:8080/api'

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

// 客户管理API
export const customerApi = {
    // 分页查询客户列表
    getCustomerList: (params: CustomerQueryParams): Promise<ApiResponse<any>> => {
        const queryString = new URLSearchParams()
        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.auditStatus) queryString.append('auditStatus', params.auditStatus)
        if (params.status !== undefined) queryString.append('status', params.status.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/customer/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request(url)
    },

    // 客户入住登记
    checkinCustomer: (data: any): Promise<ApiResponse<string>> => {
        return request('/customer/checkin', {
            method: 'POST',
            body: JSON.stringify(data)
        })
    },

    // 获取可用床位
    getAvailableBeds: (): Promise<ApiResponse<BedOption[]>> => {
        return request('/customer/available-beds')
    },

    // 获取护理人员列表
    getCaregivers: (): Promise<ApiResponse<CaregiverOption[]>> => {
        return request('/customer/caregivers')
    },

    // 获取待审核客户
    getPendingAuditCustomers: (): Promise<ApiResponse<Customer[]>> => {
        return request('/customer/pending-audit')
    },

    // 更新审核状态
    updateAuditStatus: (id: number, auditStatus: string): Promise<ApiResponse<string>> => {
        return request(`/customer/${id}/audit-status?auditStatus=${encodeURIComponent(auditStatus)}`, {
            method: 'PUT'
        })
    },

    // 获取客户统计
    getCustomerStats: (): Promise<ApiResponse<CustomerStats>> => {
        return request('/customer/stats')
    },

    // 检查身份证号
    checkIdCard: (idCard: string, excludeId?: number): Promise<ApiResponse<boolean>> => {
        const url = `/customer/check-idcard?idCard=${encodeURIComponent(idCard)}${excludeId ? '&excludeId=' + excludeId : ''}`
        return request(url)
    }
}

// 外出记录API
export const outingApi = {
    // 分页查询外出记录
    getOutingList: (params: OutingQueryParams): Promise<ApiResponse<any>> => {
        const queryString = new URLSearchParams()
        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.approvalStatus) queryString.append('approvalStatus', params.approvalStatus)
        if (params.outingDate) queryString.append('outingDate', params.outingDate)
        if (params.customerId) queryString.append('customerId', params.customerId.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/customer/outing/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request(url)
    },

    // 添加外出记录
    addOutingRecord: (data: OutingRecord): Promise<ApiResponse<string>> => {
        return request('/customer/outing/add', {
            method: 'POST',
            body: JSON.stringify(data)
        })
    },

    // 更新外出记录
    updateOutingRecord: (data: OutingRecord): Promise<ApiResponse<string>> => {
        return request('/customer/outing/update', {
            method: 'PUT',
            body: JSON.stringify(data)
        })
    },

    // 删除外出记录
    deleteOutingRecord: (id: number): Promise<ApiResponse<string>> => {
        return request(`/customer/outing/${id}`, {
            method: 'DELETE'
        })
    },

    // 批量删除外出记录
    deleteOutingRecords: (ids: number[]): Promise<ApiResponse<string>> => {
        return request('/customer/outing/batch', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        })
    },

    // 更新审批状态
    updateApprovalStatus: (id: number, approvalStatus: string): Promise<ApiResponse<string>> => {
        return request(`/customer/outing/${id}/approval-status?approvalStatus=${encodeURIComponent(approvalStatus)}`, {
            method: 'PUT'
        })
    },

    // 快速返回
    quickReturn: (id: number): Promise<ApiResponse<string>> => {
        return request(`/customer/outing/${id}/quick-return`, {
            method: 'PUT'
        })
    },

    // 获取未返回记录
    getUnreturnedRecords: (): Promise<ApiResponse<OutingRecord[]>> => {
        return request('/customer/outing/unreturned')
    },

    // 获取待审批记录
    getPendingApprovalRecords: (): Promise<ApiResponse<OutingRecord[]>> => {
        return request('/customer/outing/pending-approval')
    },

    // 获取外出统计
    getOutingStats: (startDate?: string, endDate?: string): Promise<ApiResponse<OutingStats>> => {
        const queryString = new URLSearchParams()
        if (startDate) queryString.append('startDate', startDate)
        if (endDate) queryString.append('endDate', endDate)

        const url = `/customer/outing/stats${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request(url)
    },

    // 批量审批
    batchApprove: (ids: number[], approvalStatus: string): Promise<ApiResponse<string>> => {
        return request('/customer/outing/batch-approve', {
            method: 'PUT',
            body: JSON.stringify({ ids, approvalStatus })
        })
    }
}

// 退住管理API
export const checkoutApi = {
    // 分页查询退住信息
    getCheckoutList: (params: CheckoutQueryParams): Promise<ApiResponse<any>> => {
        const queryString = new URLSearchParams()
        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.checkoutType) queryString.append('checkoutType', params.checkoutType)
        if (params.startDate) queryString.append('startDate', params.startDate)
        if (params.endDate) queryString.append('endDate', params.endDate)
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/customer/checkout/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request(url)
    },

    // 客户退住登记
    checkoutCustomer: (data: any): Promise<ApiResponse<string>> => {
        return request('/customer/checkout', {
            method: 'POST',
            body: JSON.stringify(data)
        })
    },

    // 获取退住类型
    getCheckoutTypes: (): Promise<ApiResponse<string[]>> => {
        return request('/customer/checkout/types')
    },

    // 获取退住统计
    getCheckoutStats: (startDate?: string, endDate?: string): Promise<ApiResponse<any>> => {
        const queryString = new URLSearchParams()
        if (startDate) queryString.append('startDate', startDate)
        if (endDate) queryString.append('endDate', endDate)

        const url = `/customer/checkout/stats${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request(url)
    },

    // 批量退住
    batchCheckout: (data: any): Promise<ApiResponse<string>> => {
        return request('/customer/checkout/batch', {
            method: 'POST',
            body: JSON.stringify(data)
        })
    }
}