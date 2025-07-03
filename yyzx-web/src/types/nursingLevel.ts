// 护理级别相关类型定义



// 护理等级接口
export interface NursingLevel {
    level_code: string
    level_name: string
    service_content: string
    monthly_fee: number
}

// 护理协议接口
export interface NursingAgreement {
    id?: number
    customerId?: number
    customerName?: string
    levelName: string
    levelCode: string
    levelStatus?: string
    monthlyFee?: number
    serviceContent?: string
    startDate?: string
    endDate?: string | null
    createdAt?: string
    updatedAt?: string
}



// 护理协议查询参数接口
export interface AgreementQueryParams {
    searchKeyword?: string
    levelStatus?: string
    levelCode?: string
    customerId?: number
    startDate?: string
    endDate?: string
    page?: number
    size?: number
}

// 护理协议列表响应接口
export interface AgreementListResponse {
    agreements: NursingAgreement[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 护理协议统计接口
export interface AgreementStats {
    totalActive: number
    totalSuspended: number
    totalTerminated: number
    total: number
    levelStats: Record<string, Record<string, number>>
}

// 护理协议表单接口
export interface AgreementForm {
    id?: number
    customerId: number | null
    levelName: string
    levelCode: string
    levelStatus: string
    monthlyFee: number
    serviceContent: string
    startDate: string
    endDate: string
}

// 等级调整表单接口
export interface UpgradeForm {
    customerId: number | null
    newLevelCode: string
    newLevelName: string
    newMonthlyFee: number
    serviceContent: string
    reason: string
}

// 续签表单接口
export interface RenewForm {
    customerId: number
    newEndDate: string | null
    newMonthlyFee: number
}

// 护理协议状态枚举
export enum AgreementStatus {
    ACTIVE = '生效',
    SUSPENDED = '暂停',
    TERMINATED = '终止'
}

// 护理等级代码枚举
export enum NursingLevelCode {
    LEVEL_1 = 'L001',
    LEVEL_2 = 'L002',
    LEVEL_3 = 'L003'
}

// 护理协议状态选项
export const AgreementStatusOptions = [
    { label: '生效', value: '生效', color: '#27ae60' },
    { label: '暂停', value: '暂停', color: '#f39c12' },
    { label: '终止', value: '终止', color: '#e74c3c' }
]

// 操作类型枚举
export enum OperationType {
    CREATE = '创建',
    UPDATE = '更新',
    UPGRADE = '升级',
    DOWNGRADE = '降级',
    SUSPEND = '暂停',
    RESUME = '恢复',
    RENEW = '续签',
    TERMINATE = '终止'
}

// 护理等级详情接口
export interface NursingLevelDetail extends NursingLevel {
    description?: string
    requirements?: string[]
    includedServices?: string[]
    additionalFees?: Array<{
        name: string
        fee: number
        unit: string
    }>
}

// 协议变更记录接口
export interface AgreementChangeLog {
    id: number
    agreementId: number
    changeType: string
    oldValue?: string
    newValue?: string
    reason?: string
    operatorId: number
    operatorName: string
    changeDate: string
}

// 即将到期协议提醒接口
export interface ExpiringAgreementAlert {
    id: number
    customerId: number
    customerName: string
    levelName: string
    endDate: string
    daysLeft: number
    alertLevel: 'warning' | 'danger'
}

// 护理服务项目接口
export interface NursingServiceItem {
    id: number
    serviceName: string
    serviceCategory: string
    levelCode: string
    frequency: string
    duration: number
    isRequired: boolean
}

// 月度护理报告接口
export interface MonthlyNursingReport {
    customerId: number
    customerName: string
    levelName: string
    reportMonth: string
    servicesSummary: Array<{
        serviceName: string
        completedCount: number
        totalCount: number
        completionRate: number
    }>
    totalFee: number
    actualFee: number
    notes?: string
}

// 护理质量评估接口
export interface NursingQualityAssessment {
    id: number
    customerId: number
    assessmentDate: string
    assessorId: number
    assessorName: string
    overallScore: number
    dimensions: Array<{
        name: string
        score: number
        maxScore: number
        comments?: string
    }>
    recommendations?: string[]
    nextAssessmentDate?: string
}

// 护理费用明细接口
export interface NursingFeeDetail {
    id: number
    agreementId: number
    feeMonth: string
    baseFee: number
    additionalFees: Array<{
        itemName: string
        quantity: number
        unitPrice: number
        subtotal: number
    }>
    discounts: Array<{
        reason: string
        amount: number
    }>
    totalFee: number
    paymentStatus: 'unpaid' | 'paid' | 'overdue'
    paymentDate?: string
}

