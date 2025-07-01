// 用户查询参数接口
import type {User} from "@/types/index.ts";

export interface UserQueryParams {
    searchKeyword?: string
    roleId?: number
    status?: number
    page?: number
    size?: number
}

// 用户列表响应接口
export interface UserListResponse {
    users: User[]
    total: number
    page: number
    size: number
    totalPages: number
}

// 用户统计接口
export interface UserStats {
    total: number
    active: number
    inactive: number
}

// 用户表单接口
export interface UserForm {
    id?: number
    username: string
    password?: string
    confirmPassword?: string
    realName: string
    phone?: string
    email?: string
    roleId: number | null
    status?: number
}

// 角色接口
export interface Role {
    id: number
    roleName: string
    roleCode: string
    description?: string
    status?: number
    createdAt?: string
    updatedAt?: string
}