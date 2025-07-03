<template>
  <div class="nursing-project">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>护理项目管理</h1>
      <p>管理所有护理项目信息</p>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索项目名称、分类或护理人员..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.projectCategory" @change="handleSearch">
            <option value="">全部分类</option>
            <option v-for="category in projectCategories"
                    :key="category"
                    :value="category">
              {{ category }}
            </option>
          </select>

          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">停用</option>
          </select>

          <select v-model="searchForm.caregiverId" @change="handleSearch">
            <option value="">全部护理人员</option>
            <option v-for="caregiver in caregiverList"
                    :key="caregiver.id"
                    :value="caregiver.id">
              {{ caregiver.realName }}
            </option>
          </select>

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showAddModal" class="add-btn">
          <span class="icon">+</span>
          添加项目
        </button>

        <button
            @click="batchDelete"
            :disabled="selectedProjects.length === 0"
            class="delete-btn"
        >
          <span class="icon">🗑️</span>
          批量删除 ({{ selectedProjects.length }})
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总项目数</div>
      </div>
      <div class="stat-card active">
        <div class="stat-number">{{ stats.active }}</div>
        <div class="stat-label">启用项目</div>
      </div>
      <div class="stat-card inactive">
        <div class="stat-number">{{ stats.inactive }}</div>
        <div class="stat-label">停用项目</div>
      </div>
    </div>

    <!-- 项目列表表格 -->
    <div class="table-container">
      <table class="project-table">
        <thead>
        <tr>
          <th width="50">
            <input
                type="checkbox"
                v-model="selectAll"
                @change="handleSelectAll"
            >
          </th>
          <th>项目名称</th>
          <th>项目分类</th>
          <th>标准时长(分钟)</th>
          <th>执行频率</th>
          <th>护理人员</th>
          <th>状态</th>
          <th>描述</th>
          <th width="200">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="9" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="projectList.length === 0" class="empty-row">
          <td colspan="9" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无护理项目数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="project in projectList" :key="project.id" class="project-row">
          <td>
            <input
                type="checkbox"
                :value="project.id"
                v-model="selectedProjects"
            >
          </td>
          <td class="project-name">{{ project.projectName }}</td>
          <td>
            <span class="category-tag">{{ project.projectCategory }}</span>
          </td>
          <td class="duration">{{ project.standardDuration }}</td>
          <td class="frequency">{{ project.frequency }}</td>
          <td class="caregiver">{{ project.caregiverName }}</td>
          <td>
              <span
                  class="status-badge"
                  :class="project.status === 1 ? 'active' : 'inactive'"
                  @click="showStatusModal(project)"
              >
                {{ project.status === 1 ? '启用' : '停用' }}
              </span>
          </td>
          <td class="description" :title="project.description">
            {{ project.description || '-' }}
          </td>
          <td class="actions">
            <button @click="editProject(project)" class="edit-btn">编辑</button>
            <button @click="deleteProject(project)" class="delete-btn">删除</button>
            <button @click="viewDetails(project)" class="view-btn">详情</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
          @click="goToPage(currentPage - 1)"
          :disabled="currentPage <= 1"
          class="page-btn"
      >
        上一页
      </button>

      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ totalCount }} 条
      </span>

      <button
          @click="goToPage(currentPage + 1)"
          :disabled="currentPage >= totalPages"
          class="page-btn"
      >
        下一页
      </button>

      <select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
        <option value="10">10条/页</option>
        <option value="20">20条/页</option>
        <option value="50">50条/页</option>
      </select>
    </div>

    <!-- 添加/编辑项目弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑护理项目' : '添加护理项目' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="project-form">
          <div class="form-row">
            <div class="form-group">
              <label>项目名称 *</label>
              <input
                  type="text"
                  v-model="projectForm.projectName"
                  placeholder="请输入项目名称"
                  required
              >
            </div>

            <div class="form-group">
              <label>项目分类 *</label>
              <select v-model="projectForm.projectCategory" required>
                <option value="">请选择分类</option>
                <option v-for="category in projectCategories"
                        :key="category"
                        :value="category">
                  {{ category }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>标准时长(分钟)</label>
              <input
                  type="number"
                  v-model="projectForm.standardDuration"
                  placeholder="15"
                  min="1"
                  max="300"
              >
            </div>

            <div class="form-group">
              <label>执行频率</label>
              <select v-model="projectForm.frequency">
                <option value="">请选择频率</option>
                <option v-for="freq in frequencyOptions"
                        :key="freq"
                        :value="freq">
                  {{ freq }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>护理人员</label>
              <select v-model="projectForm.caregiverId">
                <option value="">请选择护理人员</option>
                <option v-for="caregiver in caregiverList"
                        :key="caregiver.id"
                        :value="caregiver.id">
                  {{ caregiver.realName }} - {{ caregiver.phone }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>状态</label>
              <select v-model="projectForm.status">
                <option :value="1">启用</option>
                <option :value="0">停用</option>
              </select>
            </div>
          </div>

          <div class="form-group full-width">
            <label>项目描述</label>
            <textarea
                v-model="projectForm.description"
                placeholder="请输入项目描述"
                rows="4"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : (isEditing ? '更新' : '添加') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 状态修改弹窗 -->
    <div v-if="showStatusChange" class="modal-overlay" @click="closeStatusModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>修改项目状态</h3>
          <button @click="closeStatusModal" class="close-btn">×</button>
        </div>

        <div class="status-form">
          <p>项目：{{ currentProject?.projectName }}</p>
          <p>当前状态：<span class="current-status">{{ currentProject?.status === 1 ? '启用' : '停用' }}</span></p>

          <div class="form-group">
            <label>新状态：</label>
            <select v-model="newStatus">
              <option :value="1">启用</option>
              <option :value="0">停用</option>
            </select>
          </div>

          <div class="form-actions">
            <button @click="closeStatusModal" class="cancel-btn">取消</button>
            <button @click="updateStatus" :disabled="submitting" class="submit-btn">
              {{ submitting ? '更新中...' : '确认更新' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目详情弹窗 -->
    <div v-if="showDetails" class="modal-overlay" @click="closeDetailsModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>护理项目详情</h3>
          <button @click="closeDetailsModal" class="close-btn">×</button>
        </div>

        <div class="details-content" v-if="currentProject">
          <div class="detail-row">
            <span class="detail-label">项目名称：</span>
            <span class="detail-value">{{ currentProject.projectName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">项目分类：</span>
            <span class="detail-value category-tag">{{ currentProject.projectCategory }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">标准时长：</span>
            <span class="detail-value">{{ currentProject.standardDuration }} 分钟</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">执行频率：</span>
            <span class="detail-value">{{ currentProject.frequency }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">护理人员：</span>
            <span class="detail-value">{{ currentProject.caregiverName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态：</span>
            <span class="detail-value status-badge" :class="currentProject.status === 1 ? 'active' : 'inactive'">
              {{ currentProject.status === 1 ? '启用' : '停用' }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatDate(currentProject.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">更新时间：</span>
            <span class="detail-value">{{ formatDate(currentProject.updatedAt) }}</span>
          </div>
          <div class="detail-row full-width">
            <span class="detail-label">项目描述：</span>
            <p class="detail-description">{{ currentProject.description || '暂无描述' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { nursingProjectApi } from '@/utils/nursingProjectApi'
import type {
  NursingProject,
  NursingProjectQueryParams,
  NursingProjectStats,
  NursingProjectForm,
  User
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const projectList = ref<NursingProject[]>([])
const caregiverList = ref<User[]>([])
const projectCategories = ref<string[]>([])
const frequencyOptions = ref<string[]>([])
const selectedProjects = ref<number[]>([])
const selectAll = ref<boolean>(false)
const showModal = ref<boolean>(false)
const showStatusChange = ref<boolean>(false)
const showDetails = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentProject = ref<NursingProject | null>(null)
const newStatus = ref<number>(1)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<NursingProjectStats>({
  total: 0,
  active: 0,
  inactive: 0
})

// 搜索表单
const searchForm = reactive<NursingProjectQueryParams>({
  searchKeyword: '',
  projectCategory: '',
  status: undefined,
  caregiverId: undefined,
  page: 1,
  size: 10
})

// 项目表单
const projectForm = reactive<NursingProjectForm>({
  projectName: '',
  projectCategory: '',
  description: '',
  standardDuration: 15,
  caregiverId: undefined,
  frequency: '',
  status: 1
})

// 计算属性
const selectAllState = computed(() => {
  if (projectList.value.length === 0) return false
  return selectedProjects.value.length === projectList.value.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadProjectList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await nursingProjectApi.getProjectList(params)

    if (response.code === 200) {
      projectList.value = response.data.projects
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载项目列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadCaregiverList = async (): Promise<void> => {
  try {
    const response = await nursingProjectApi.getCaregiverList()
    if (response.code === 200) {
      caregiverList.value = response.data
    }
  } catch (error) {
    console.error('加载护理人员列表失败:', error)
  }
}

const loadCategories = async (): Promise<void> => {
  try {
    const response = await nursingProjectApi.getProjectCategories()
    if (response.code === 200) {
      projectCategories.value = response.data
    }
  } catch (error) {
    console.error('加载项目分类失败:', error)
  }
}

const loadFrequencyOptions = async (): Promise<void> => {
  try {
    const response = await nursingProjectApi.getFrequencyOptions()
    if (response.code === 200) {
      frequencyOptions.value = response.data
    }
  } catch (error) {
    console.error('加载执行频率选项失败:', error)
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await nursingProjectApi.getProjectStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadProjectList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    projectCategory: '',
    status: undefined,
    caregiverId: undefined
  })
  handleSearch()
}

const handleSelectAll = (): void => {
  if (selectAll.value) {
    selectedProjects.value = projectList.value.map(project => project.id!).filter(id => id !== undefined)
  } else {
    selectedProjects.value = []
  }
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadProjectList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadProjectList()
}

const showAddModal = (): void => {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

const editProject = (project: NursingProject): void => {
  isEditing.value = true
  Object.assign(projectForm, {
    id: project.id,
    projectName: project.projectName,
    projectCategory: project.projectCategory,
    description: project.description || '',
    standardDuration: project.standardDuration,
    caregiverId: project.caregiverId,
    frequency: project.frequency,
    status: project.status
  })
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(projectForm, {
    id: undefined,
    projectName: '',
    projectCategory: '',
    description: '',
    standardDuration: 15,
    caregiverId: undefined,
    frequency: '',
    status: 1
  })
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const submitForm = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = isEditing.value
        ? await nursingProjectApi.updateProject(projectForm)
        : await nursingProjectApi.addProject(projectForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadProjectList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('操作失败', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteProject = async (project: NursingProject): Promise<void> => {
  if (!confirm(`确认删除护理项目 "${project.projectName}" 吗？`)) return

  try {
    const response = await nursingProjectApi.deleteProject(project.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadProjectList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchDelete = async (): Promise<void> => {
  if (selectedProjects.value.length === 0) return

  if (!confirm(`确认删除选中的 ${selectedProjects.value.length} 个护理项目吗？`)) return

  try {
    const response = await nursingProjectApi.deleteProjects(selectedProjects.value)
    if (response.code === 200) {
      showMessage('批量删除成功', 'success')
      selectedProjects.value = []
      selectAll.value = false
      loadProjectList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const showStatusModal = (project: NursingProject): void => {
  currentProject.value = project
  newStatus.value = project.status
  showStatusChange.value = true
}

const closeStatusModal = (): void => {
  showStatusChange.value = false
  currentProject.value = null
  newStatus.value = 1
}

const updateStatus = async (): Promise<void> => {
  if (!currentProject.value || newStatus.value === currentProject.value.status) {
    closeStatusModal()
    return
  }

  submitting.value = true
  try {
    const response = await nursingProjectApi.updateProjectStatus(currentProject.value.id!, newStatus.value)
    if (response.code === 200) {
      showMessage('状态更新成功', 'success')
      closeStatusModal()
      loadProjectList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('状态更新失败', 'error')
  } finally {
    submitting.value = false
  }
}

const viewDetails = (project: NursingProject): void => {
  currentProject.value = project
  showDetails.value = true
}

const closeDetailsModal = (): void => {
  showDetails.value = false
  currentProject.value = null
}

// 生命周期
onMounted(() => {
  loadProjectList()
  loadCaregiverList()
  loadCategories()
  loadFrequencyOptions()
  loadStats()
})

// 监听选中状态
watch(selectedProjects, () => {
  selectAll.value = selectAllState.value
}, { deep: true })
</script>

<style scoped>
.nursing-project {
  padding: 20px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 8px;
}

.page-header p {
  color: #7f8c8d;
  font-size: 16px;
}

/* 工具栏样式 */
.toolbar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.search-filters {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
}

.search-group {
  display: flex;
  gap: 10px;
}

.search-input {
  width: 300px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.search-btn {
  background: #3498db;
  color: white;
}

.search-btn:hover {
  background: #2980b9;
}

.reset-btn {
  background: #95a5a6;
  color: white;
}

.reset-btn:hover {
  background: #7f8c8d;
}

.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.filter-group select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.add-btn, .delete-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
}

.add-btn {
  background: #27ae60;
  color: white;
}

.add-btn:hover {
  background: #219a52;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover:not(:disabled) {
  background: #c0392b;
}

.delete-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 统计卡片样式 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
  border-left: 4px solid;
}

.stat-card.total {
  border-left-color: #3498db;
}

.stat-card.active {
  border-left-color: #27ae60;
}

.stat-card.inactive {
  border-left-color: #e74c3c;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.project-table {
  width: 100%;
  border-collapse: collapse;
}

.project-table th,
.project-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.project-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.project-table tr:hover {
  background: #f8f9fa;
}

.project-name {
  font-weight: 600;
  color: #3498db;
}

.category-tag {
  background: #e8f5e8;
  color: #27ae60;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.duration {
  font-weight: 600;
  color: #f39c12;
}

.frequency {
  color: #8e44ad;
  font-weight: 500;
}

.caregiver {
  color: #2980b9;
}

.description {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.3s;
}

.status-badge:hover {
  opacity: 0.8;
}

.status-badge.active {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.inactive {
  background: #fadbd8;
  color: #e74c3c;
}

.actions {
  display: flex;
  gap: 5px;
}

.actions .edit-btn,
.actions .delete-btn,
.actions .view-btn {
  padding: 5px 10px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.actions .edit-btn {
  background: #3498db;
  color: white;
}

.actions .edit-btn:hover {
  background: #2980b9;
}

.actions .delete-btn {
  background: #e74c3c;
  color: white;
}

.actions .delete-btn:hover {
  background: #c0392b;
}

.actions .view-btn {
  background: #95a5a6;
  color: white;
}

.actions .view-btn:hover {
  background: #7f8c8d;
}

.loading-row,
.empty-row {
  text-align: center;
}

.loading-cell,
.empty-cell {
  padding: 40px;
}

.loading-spinner {
  color: #7f8c8d;
  font-size: 16px;
}

.empty-state {
  color: #7f8c8d;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 10px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  background: #ecf0f1;
  color: #bdc3c7;
  cursor: not-allowed;
}

.page-info {
  color: #7f8c8d;
  font-size: 14px;
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.small {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 20px 0;
  border-bottom: 1px solid #ecf0f1;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #7f8c8d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #e74c3c;
}

/* 表单样式 */
.project-form,
.status-form {
  padding: 0 20px 20px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 500;
  color: #2c3e50;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ecf0f1;
}

.cancel-btn,
.submit-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.cancel-btn {
  background: #ecf0f1;
  color: #7f8c8d;
}

.cancel-btn:hover {
  background: #d5dbdb;
}

.submit-btn {
  background: #3498db;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #2980b9;
}

.submit-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.status-form p {
  margin-bottom: 10px;
  color: #2c3e50;
}

.current-status {
  font-weight: 600;
  color: #e74c3c;
}

/* 详情样式 */
.details-content {
  padding: 0 20px 20px;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}

.detail-row.full-width {
  flex-direction: column;
  align-items: flex-start;
}

.detail-label {
  font-weight: 600;
  color: #2c3e50;
  min-width: 120px;
}

.detail-value {
  color: #34495e;
}

.detail-description {
  margin-top: 10px;
  line-height: 1.6;
  color: #7f8c8d;
}

/* 消息提示样式 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 20px;
  border-radius: 4px;
  font-size: 14px;
  z-index: 1001;
  min-width: 200px;
  text-align: center;
  animation: slideIn 0.3s ease;
}

.message.success {
  background: #d5f4e6;
  color: #27ae60;
  border: 1px solid #27ae60;
}

.message.error {
  background: #fadbd8;
  color: #e74c3c;
  border: 1px solid #e74c3c;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-filters {
    flex-direction: column;
  }

  .stats-section {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .project-table {
    font-size: 12px;
  }

  .project-table th,
  .project-table td {
    padding: 8px 6px;
  }
}
</style>