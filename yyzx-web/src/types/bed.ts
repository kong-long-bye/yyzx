// 床位相关类型定义

// 床位实体接口
export interface Bed {
    id?: number
    bedNumber: string
    roomId: number
    bedType: string
    status: BedStatus
    dailyPrice: number
    description?: string
    createdAt?: string
    updatedAt?: string
    // 关联字段
    roomNumber?: string
    floorNumber?: number
    customerName?: string
}

// 房间实体接口
export interface Room {
    id: number
    roomNumber: string
    floorNumber: number
    roomType: string
    maxBeds: number
    description?: string
    status: number
    createdAt?: string
    updatedAt?: string
    occupiedBeds?: number
    availableBeds?: number
}

// 床位状态枚举
export enum BedStatus {
    AVAILABLE = '空闲',
    OCCUPIED = '占用',
    MAINTENANCE = '维修',
    RESERVED = '预留'
}

// 床位类型枚举
export enum BedType {
    NORMAL = '普通床',
    ELECTRIC = '电动床',
    NURSING = '护理床'
}

// 床位查询参数接口
export interface BedQueryParams {
    searchKeyword?: string
    status?: string
    floorNumber?: number
    page?: number
    size?: number
}

// 床位列表响应接口
export interface BedListResponse {
    beds: Bed[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 床位统计接口
export interface BedStats {
    total: number
    occupied: number
    available: number
    maintenance: number
    reserved: number
}

// 床位表单接口
export interface BedForm {
    id?: number
    bedNumber: string
    roomId: number | null
    bedType: string
    status: string
    dailyPrice: number | null
    description: string
}

// 床位状态选项
export const BedStatusOptions = [
    { label: '空闲', value: '空闲', color: '#27ae60' },
    { label: '占用', value: '占用', color: '#e74c3c' },
    { label: '维修', value: '维修', color: '#f39c12' },
    { label: '预留', value: '预留', color: '#3498db' }
]

// 床位类型选项
export const BedTypeOptions = [
    { label: '普通床', value: '普通床' },
    { label: '电动床', value: '电动床' },
    { label: '护理床', value: '护理床' }
]