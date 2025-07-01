import type {
    ApiResponse,
    LoginRequest,
    LoginResponse,
    RegisterRequest, Role,
    User, UserForm, UserListResponse, UserQueryParams, UserStats
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

    // 分页查询用户列表
    getUserList: (params: UserQueryParams): Promise<ApiResponse<UserListResponse>> => {
        const queryString = new URLSearchParams()

        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.roleId) queryString.append('roleId', params.roleId.toString())
        if (params.status !== undefined) queryString.append('status', params.status.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/user/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<UserListResponse>(url)
    },

    // 根据ID查询用户详情
    getUserById: (id: number): Promise<ApiResponse<User>> => {
        return request<User>(`/user/${id}`)
    },

    // 添加用户
    addUser: (user: UserForm): Promise<ApiResponse<string>> => {
        return request<string>('/user/add', {
            method: 'POST',
            body: JSON.stringify(user)
        })
    },

    // 更新用户信息
    updateUser: (user: UserForm): Promise<ApiResponse<string>> => {
        return request<string>('/user/update', {
            method: 'PUT',
            body: JSON.stringify(user)
        })
    },

    // 删除用户
    deleteUser: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/user/${id}`, {
            method: 'DELETE'
        })
    },

    // 批量删除用户
    deleteUsers: (ids: number[]): Promise<ApiResponse<string>> => {
        return request<string>('/user/batch', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        })
    },

    // 更新用户状态
    updateUserStatus: (id: number, status: number): Promise<ApiResponse<string>> => {
        return request<string>(`/user/${id}/status?status=${status}`, {
            method: 'PUT'
        })
    },

    // 重置用户密码
    resetPassword: (id: number, data: { password: string }): Promise<ApiResponse<string>> => {
        return request<string>(`/user/${id}/reset-password`, {
            method: 'PUT',
            body: JSON.stringify(data)
        })
    },

    // 修改密码
    changePassword: (id: number, data: { oldPassword: string, newPassword: string }): Promise<ApiResponse<string>> => {
        return request<string>(`/user/${id}/change-password`, {
            method: 'PUT',
            body: JSON.stringify(data)
        })
    },

    // 检查用户名是否可用
    checkUsername: (username: string, excludeId?: number): Promise<ApiResponse<boolean>> => {
        const url = `/user/check-username?username=${encodeURIComponent(username)}${excludeId ? '&excludeId=' + excludeId : ''}`
        return request<boolean>(url)
    },

    // 检查邮箱是否可用
    checkEmail: (email: string, excludeId?: number): Promise<ApiResponse<boolean>> => {
        const url = `/user/check-email?email=${encodeURIComponent(email)}${excludeId ? '&excludeId=' + excludeId : ''}`
        return request<boolean>(url)
    },

    // 获取用户统计信息
    getUserStats: (): Promise<ApiResponse<UserStats>> => {
        return request<UserStats>('/user/stats')
    },

    // 查询最近注册的用户
    getRecentUsers: (limit?: number): Promise<ApiResponse<User[]>> => {
        const url = `/user/recent${limit ? '?limit=' + limit : ''}`
        return request<User[]>(url)
    },

    // 根据角色ID查询用户列表
    getUsersByRoleId: (roleId: number): Promise<ApiResponse<User[]>> => {
        return request<User[]>(`/user/role/${roleId}`)
    },

    // 批量更新用户状态
    batchUpdateUserStatus: (data: { ids: number[], status: number }): Promise<ApiResponse<string>> => {
        return request<string>('/user/batch-status', {
            method: 'PUT',
            body: JSON.stringify(data)
        })
    },

    // 获取所有角色列表
    getRoles: (): Promise<ApiResponse<Role[]>> => {
        return request<Role[]>('/user/roles')
    }
}








