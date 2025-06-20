import type {
    ApiResponse,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    User
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

// 用户相关API
export const userApi = {
    // 用户登录
    login: (loginData: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
        return request<LoginResponse>('/user/login', {
            method: 'POST',
            body: JSON.stringify(loginData)
        })
    },

    // 用户注册
    register: (userData: RegisterRequest): Promise<ApiResponse<string>> => {
        return request<string>('/user/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        })
    },

    // 检查用户名是否可用
    checkUsername: (username: string): Promise<ApiResponse<boolean>> => {
        return request<boolean>(`/user/check-username?username=${encodeURIComponent(username)}`)
    }
}

export default {
    userApi
}


