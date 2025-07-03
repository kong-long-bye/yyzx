
// ==================== 客户护理设置相关类型 ====================
// 客户护理分配实体接口
export interface CustomerCareAssignment {
    id?: number
    customerId: number
    customerName: string
    caregiverId: number
    caregiverName: string
    assignmentDate: string
    endDate?: string
    primaryCaregiver: number
    status: number
    createdAt?: string
    updatedAt?: string
}

// 客户护理分配表单接口
export interface CustomerCareAssignmentForm {
    id?: number
    customerId?: number
    caregiverId?: number
    assignmentDate: string
    endDate?: string
    primaryCaregiver: number
    status: number
}

// 护理人员工作负载统计接口
export interface CaregiverWorkload {
    caregiver_id: number
    caregiver_name: string
    phone: string
    role_name: string
    customer_count: number
    primary_count: number
}

// 客户护理统计接口
export interface CustomerCareStats {
    totalCaregivers: number
    activeCaregivers: number
    totalCustomers: number
    averageCustomersPerCaregiver: number
}

