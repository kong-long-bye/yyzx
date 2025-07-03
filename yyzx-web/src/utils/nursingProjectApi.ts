import type {
    ApiResponse,
    NursingProject,
    NursingProjectQueryParams,
    NursingProjectListResponse,
    NursingProjectStats,
    NursingProjectForm,
    User
} from '@/types'

// API基础URL
const BASE_URL = 'http://localhost:8080/api/nursing'

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

// 护理项目相关API
export const nursingProjectApi = {
    // 分页查询护理项目列表
    getProjectList: (params: NursingProjectQueryParams): Promise<ApiResponse<NursingProjectListResponse>> => {
        const queryString = new URLSearchParams()

        if (params.searchKeyword) queryString.append('searchKeyword', params.searchKeyword)
        if (params.projectCategory) queryString.append('projectCategory', params.projectCategory)
        if (params.status !== undefined) queryString.append('status', params.status.toString())
        if (params.caregiverId) queryString.append('caregiverId', params.caregiverId.toString())
        if (params.page) queryString.append('page', params.page.toString())
        if (params.size) queryString.append('size', params.size.toString())

        const url = `/project/list${queryString.toString() ? '?' + queryString.toString() : ''}`
        return request<NursingProjectListResponse>(url)
    },

    // 根据ID查询护理项目详细信息
    getProjectById: (id: number): Promise<ApiResponse<NursingProject>> => {
        return request<NursingProject>(`/project/${id}`)
    },

    // 添加护理项目
    addProject: (project: NursingProjectForm): Promise<ApiResponse<string>> => {
        return request<string>('/project/add', {
            method: 'POST',
            body: JSON.stringify(project)
        })
    },

    // 更新护理项目信息
    updateProject: (project: NursingProjectForm): Promise<ApiResponse<string>> => {
        return request<string>('/project/update', {
            method: 'PUT',
            body: JSON.stringify(project)
        })
    },

    // 删除护理项目
    deleteProject: (id: number): Promise<ApiResponse<string>> => {
        return request<string>(`/project/${id}`, {
            method: 'DELETE'
        })
    },

    // 批量删除护理项目
    deleteProjects: (ids: number[]): Promise<ApiResponse<string>> => {
        return request<string>('/project/batch', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        })
    },

    // 更新护理项目状态
    updateProjectStatus: (id: number, status: number): Promise<ApiResponse<string>> => {
        return request<string>(`/project/${id}/status?status=${status}`, {
            method: 'PUT'
        })
    },

    // 获取项目分类选项
    getProjectCategories: (): Promise<ApiResponse<string[]>> => {
        return request<string[]>('/project/categories')
    },

    // 获取护理项目统计信息
    getProjectStats: (): Promise<ApiResponse<NursingProjectStats>> => {
        return request<NursingProjectStats>('/project/stats')
    },

    // 获取启用状态的护理项目
    getActiveProjects: (): Promise<ApiResponse<NursingProject[]>> => {
        return request<NursingProject[]>('/project/active')
    },

    // 获取执行频率选项
    getFrequencyOptions: (): Promise<ApiResponse<string[]>> => {
        return request<string[]>('/project/frequency-options')
    },

    // 根据护理等级推荐项目
    getRecommendedProjects: (nursingLevel: string): Promise<ApiResponse<NursingProject[]>> => {
        return request<NursingProject[]>(`/project/recommend?nursingLevel=${encodeURIComponent(nursingLevel)}`)
    },

    // 获取护理人员列表（从用户API获取角色为护理人员的用户）
    getCaregiverList: (): Promise<ApiResponse<User[]>> => {

        return fetch('http://localhost:8080/api/user/role/2', {headers: {
                'Content-Type': 'application/json',
            }})
            .then(response => response.json())

    }
}

export default nursingProjectApi