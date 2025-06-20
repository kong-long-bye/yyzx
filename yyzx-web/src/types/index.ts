// 统一响应格式
export interface ApiResponse<T = any> {
    code: number
    msg: string
    data: T
}

// 用户信息接口
export interface User {
    id?: number
    username: string
    password?: string
    realName: string
    phone?: string
    email?: string
    roleId: number
    status?: number
    lastLoginTime?: string
    createdAt?: string
    updatedAt?: string
}

// 登录请求接口
export interface LoginRequest {
    username: string
    password: string
}

// 登录响应接口
export interface LoginResponse {
    user: User
    token: string
}

// 注册请求接口
export interface RegisterRequest {
    username: string
    password: string
    confirmPassword: string
    realName: string
    phone?: string
    email?: string
    roleId: number
}

// 角色枚举
export enum UserRole {
    ADMIN = 1,
    NURSE_MANAGER = 2,
    MEDICAL_STAFF = 3
}

// 菜单项接口
export interface MenuItem {
    id: string
    title: string
    icon: string
    children?: SubMenuItem[]
}

export interface SubMenuItem {
    id: string
    title: string
    parentId: string
}

