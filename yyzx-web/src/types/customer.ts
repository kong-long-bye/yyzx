
// 客户信息接口
export interface Customer {
    id?: number
    name: string
    idCard: string
    gender: '男' | '女'
    birthDate: string
    phone?: string
    emergencyContact?: string
    emergencyPhone?: string
    address?: string
    medicalHistory?: string
    allergies?: string
    bedId?: number
    admissionDate?: string
    checkoutInfoId?: number
    auditStatus: '待审核' | '已通过' | '已拒绝'
    status: number // 1-在住，0-已退住
    createdAt?: string
    updatedAt?: string
    // 关联字段
    bedNumber?: string
    roomNumber?: string
    floorNumber?: number
    nursingLevel?: string
    primaryCaregiver?: string
}

// 护理协议接口
export interface NursingAgreement {
    levelName: string
    levelCode: string
    monthlyFee: number
    serviceContent?: string
    startDate?: string
}

// 外出记录接口
export interface OutingRecord {
    id?: number
    customerId: number
    outingDate: string
    outingTime: string
    returnTime?: string
    destination?: string
    companion?: string
    reason?: string
    approvalStatus: '待批准' | '已批准' | '已拒绝'
    actualReturnTime?: string
    notes?: string
    caregiverId?: number
    createdAt?: string
    updatedAt?: string
    // 关联字段
    customerName?: string
    caregiverName?: string
}


// 客户查询参数
export interface CustomerQueryParams {
    searchKeyword?: string
    auditStatus?: string
    status?: number
    page?: number
    size?: number
}

// 外出记录查询参数
export interface OutingQueryParams {
    searchKeyword?: string
    approvalStatus?: string
    outingDate?: string
    customerId?: number
    page?: number
    size?: number
}

// 退住信息查询参数
export interface CheckoutQueryParams {
    searchKeyword?: string
    checkoutType?: string
    startDate?: string
    endDate?: string
    page?: number
    size?: number
}

// 客户统计数据
export interface CustomerStats {
    total: number
    inService: number
    checkedOut: number
    pendingAudit: number
}

// 外出统计数据
export interface OutingStats {
    total: number
    approved: number
    pending: number
    rejected: number
    unreturned: number
}

// 床位选项
export interface BedOption {
    id: number
    bed_number: string
    bed_type: string
    daily_price: number
    room_number: string
    floor_number: number
    room_type: string
}

// 护理人员选项
export interface CaregiverOption {
    id: number
    real_name: string
    phone?: string
    role_name: string
    customer_count: number
}
export interface CheckoutInfo {
    id: number
    checkoutType: string
    checkoutReason: string
    checkoutDate: string
    remarks: string
    createdAt: string
}

// 添加退住统计接口
export interface CheckoutStats {
    totalInPeriod: number
    typeStats: Record<string, number>
    dailyStats?: Array<{
        checkout_date: string
        count: number
    }>
}


export interface CheckoutFormData{
    customerId: number
    checkoutType: string
    checkoutReason: string
    checkoutDate: string
    remarks: string
}

export interface BatchCheckoutFormData {
    customerIds: number[]
    checkoutType: string
    checkoutReason: string
    checkoutDate: string
    remarks: string
}