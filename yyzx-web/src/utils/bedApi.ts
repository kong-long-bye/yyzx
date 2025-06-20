import type {
    ApiResponse,
    Bed,
    Room,
    BedQueryParams,
    BedListResponse,
    BedStats,
    BedForm
} from '@/types'

// API基础URL
const BASE_URL = 'http://localhost:8080/api'

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

// 床位相关API
export const bedApi = {
    // 分页查询床位列表
    getBedList: (params: BedQueryParams): Promise<ApiResponse<BedListResponse>> => {
        const queryString = new URLSearchParams()

        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.status) queryString.append('status', params.status)
        if (params.floorNumber) queryString.append('floorNumber', params.floorNumber.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/bed/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<BedListResponse>(url)
    },

    // 根据ID查询床位详细信息
    getBedById: (id: number): Promise<ApiResponse<Bed>> => {
        return request<Bed>(`/bed/${id}`)
    },

    // 添加床位
    addBed: (bed: BedForm): Promise<ApiResponse<string>> => {
        return request<string>('/bed/add', {
            method: 'POST',
            body: JSON.stringify(bed)
        })
    },

    // 更新床位信息
    updateBed: (bed: BedForm): Promise<ApiResponse<string>> => {
        return request<string>('/bed/update', {
            method: 'PUT',
            body: JSON.stringify(bed)
        })
    },

    // 删除床位
    deleteBed: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/bed/${id}`, {
            method: 'DELETE'
        })
    },

    // 批量删除床位
    deleteBeds: (ids: number[]): Promise<ApiResponse<string>> => {
        return request<string>('/bed/batch', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        })
    },

    // 更新床位状态
    updateBedStatus: (id: number, status: string): Promise<ApiResponse<string>> => {
        return request<string>(`/bed/${id}/status?status=${encodeURIComponent(status)}`, {
            method: 'PUT'
        })
    },

    // 获取所有房间信息
    getAllRooms: (): Promise<ApiResponse<Room[]>> => {
        return request<Room[]>('/bed/rooms')
    },

    // 根据房间ID查询床位列表
    getBedsByRoomId: (roomId: number): Promise<ApiResponse<Bed[]>> => {
        return request<Bed[]>(`/bed/room/${roomId}`)
    },

    // 获取床位状态统计
    getBedStats: (): Promise<ApiResponse<BedStats>> => {
        return request<BedStats>('/bed/stats')
    },

    // 检查床位号是否可用
    checkBedNumber: (bedNumber: string, excludeId?: number): Promise<ApiResponse<boolean>> => {
        const url = `/bed/check-number?bedNumber=${encodeURIComponent(bedNumber)}${excludeId ? '&excludeId=' + excludeId : ''}`
        return request<boolean>(url)
    }
}

export default bedApi