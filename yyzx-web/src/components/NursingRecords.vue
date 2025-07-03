<template>
  <div class="nursing-records">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>护理记录管理</h1>
      <p>管理和查看所有护理执行记录</p>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索客户姓名、项目名称或护理人员..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.executionStatus" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="status in statusOptions"
                    :key="status"
                    :value="status">
              {{ status }}
            </option>
          </select>

          <input
              type="date"
              v-model="searchForm.startDate"
              @change="handleSearch"
              class="date-input"
              title="开始日期"
          >
          <input
              type="date"
              v-model="searchForm.endDate"
              @change="handleSearch"
              class="date-input"
              title="结束日期"
          >

          <select v-model="searchForm.customerId" @change="handleSearch">
            <option value="">全部客户</option>
            <option v-for="customer in customerList"
                    :key="customer.id"
                    :value="customer.id">
              {{ customer.name }}
            </option>
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
          添加记录
        </button>

        <button @click="showQuickRecordModal" class="quick-btn">
          <span class="icon">⚡</span>
          快速记录
        </button>

        <button
            @click="batchDelete"
            :disabled="selectedRecords.length === 0"
            class="delete-btn"
        >
          <span class="icon">🗑️</span>
          批量删除 ({{ selectedRecords.length }})
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ recordStats.total }}</div>
        <div class="stat-label">总记录数</div>
      </div>
      <div class="stat-card completed">
        <div class="stat-number">{{ recordStats.completed }}</div>
        <div class="stat-label">已完成</div>
      </div>
      <div class="stat-card partial">
        <div class="stat-number">{{ recordStats.partialCompleted }}</div>
        <div class="stat-label">部分完成</div>
      </div>
      <div class="stat-card unexecuted">
        <div class="stat-number">{{ recordStats.unExecuted }}</div>
        <div class="stat-label">未执行</div>
      </div>
      <div class="stat-card abnormal">
        <div class="stat-number">{{ recordStats.abnormal }}</div>
        <div class="stat-label">异常</div>
      </div>
      <div class="stat-card rate">
        <div class="stat-number">{{ recordStats.completionRate }}%</div>
        <div class="stat-label">完成率</div>
      </div>
    </div>

    <!-- 快捷操作区域 -->
    <div class="quick-actions">
      <div class="action-group">
        <h4>快捷查看</h4>
        <button @click="loadTodayRecords" class="quick-action-btn">今日记录</button>
        <button @click="loadUncompletedRecords" class="quick-action-btn">未完成记录</button>
        <button @click="loadMyRecords" class="quick-action-btn" v-if="currentUser">我的记录</button>
      </div>
    </div>

    <!-- 护理记录列表表格 -->
    <div class="table-container">
      <table class="records-table">
        <thead>
        <tr>
          <th width="50">
            <input
                type="checkbox"
                v-model="selectAll"
                @change="handleSelectAll"
            >
          </th>
          <th>客户姓名</th>
          <th>护理项目</th>
          <th>项目分类</th>
          <th>护理人员</th>
          <th>执行日期</th>
          <th>执行时间</th>
          <th>时长(分钟)</th>
          <th>执行状态</th>
          <th>备注</th>
          <th width="200">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="11" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="recordList.length === 0" class="empty-row">
          <td colspan="11" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">📝</span>
              <p>暂无护理记录数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="record in recordList" :key="record.id" class="record-row">
          <td>
            <input
                type="checkbox"
                :value="record.id"
                v-model="selectedRecords"
            >
          </td>
          <td class="customer-name">{{ record.customerName }}</td>
          <td class="project-name">{{ record.projectName }}</td>
          <td>
            <span class="category-tag">{{ record.projectCategory }}</span>
          </td>
          <td class="caregiver-name">{{ record.caregiverName }}</td>
          <td>{{ formatDate(record.executionDate) }}</td>
          <td>{{ record.executionTime || '-' }}</td>
          <td class="duration">
            <span class="actual">{{ record.duration }}</span>
            <span class="standard" v-if="record.standardDuration">/{{ record.standardDuration }}</span>
          </td>
          <td>
            <span
                class="status-badge"
                :class="getStatusClass(record.executionStatus)"
            >
              {{ record.executionStatus }}
            </span>
          </td>
          <td class="notes" :title="record.notes">
            {{ record.notes || '-' }}
          </td>
          <td class="actions">
            <button @click="editRecord(record)" class="edit-btn">编辑</button>
            <button @click="deleteRecord(record)" class="delete-btn">删除</button>
            <button @click="viewDetails(record)" class="view-btn">详情</button>
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

    <!-- 添加/编辑记录弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑护理记录' : '添加护理记录' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="record-form">
          <div class="form-row">
            <div class="form-group">
              <label>客户 *</label>
              <select v-model="recordForm.customerId" required>
                <option value="">请选择客户</option>
                <option v-for="customer in customerList"
                        :key="customer.id"
                        :value="customer.id">
                  {{ customer.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>护理项目 *</label>
              <select v-model="recordForm.projectId" required @change="onProjectChange">
                <option value="">请选择护理项目</option>
                <option v-for="project in projectList"
                        :key="project.id"
                        :value="project.id">
                  {{ project.projectName }} - {{ project.projectCategory }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>护理人员 *</label>
              <select v-model="recordForm.caregiverId" required>
                <option value="">请选择护理人员</option>
                <option v-for="caregiver in caregiverList"
                        :key="caregiver.id"
                        :value="caregiver.id">
                  {{ caregiver.realName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>执行状态 *</label>
              <select v-model="recordForm.executionStatus" required>
                <option v-for="status in statusOptions"
                        :key="status"
                        :value="status">
                  {{ status }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>执行日期 *</label>
              <input
                  type="date"
                  v-model="recordForm.executionDate"
                  required
              >
            </div>

            <div class="form-group">
              <label>执行时间</label>
              <input
                  type="time"
                  v-model="recordForm.executionTime"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>实际时长(分钟)</label>
              <input
                  type="number"
                  v-model="recordForm.duration"
                  placeholder="15"
                  min="1"
                  max="480"
              >
              <small v-if="selectedProject">标准时长: {{ selectedProject.standardDuration }} 分钟</small>
            </div>
          </div>

          <div class="form-group full-width">
            <label>备注</label>
            <textarea
                v-model="recordForm.notes"
                placeholder="请输入护理记录备注..."
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

    <!-- 快速记录弹窗 -->
    <div v-if="showQuickRecord" class="modal-overlay" @click="closeQuickRecordModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>快速护理记录</h3>
          <button @click="closeQuickRecordModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitQuickRecord" class="quick-record-form">
          <div class="form-group">
            <label>客户 *</label>
            <select v-model="quickRecordForm.customerId" required>
              <option value="">请选择客户</option>
              <option v-for="customer in customerList"
                      :key="customer.id"
                      :value="customer.id">
                {{ customer.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>护理项目 *</label>
            <select v-model="quickRecordForm.projectId" required>
              <option value="">请选择护理项目</option>
              <option v-for="project in projectList"
                      :key="project.id"
                      :value="project.id">
                {{ project.projectName }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>护理人员</label>
            <select v-model="quickRecordForm.caregiverId">
              <option value="">请选择护理人员</option>
              <option v-for="caregiver in caregiverList"
                      :key="caregiver.id"
                      :value="caregiver.id">
                {{ caregiver.realName }}
              </option>
            </select>
            <small>留空则使用当前登录用户</small>
          </div>

          <div class="form-group">
            <label>备注</label>
            <textarea
                v-model="quickRecordForm.notes"
                placeholder="快速记录备注..."
                rows="3"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeQuickRecordModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '记录中...' : '快速记录' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 记录详情弹窗 -->
    <div v-if="showDetails" class="modal-overlay" @click="closeDetailsModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>护理记录详情</h3>
          <button @click="closeDetailsModal" class="close-btn">×</button>
        </div>

        <div class="details-content" v-if="currentRecord">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">客户姓名：</span>
                <span class="value">{{ currentRecord.customerName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">护理项目：</span>
                <span class="value">{{ currentRecord.projectName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">项目分类：</span>
                <span class="value category-tag">{{ currentRecord.projectCategory }}</span>
              </div>
              <div class="detail-item">
                <span class="label">护理人员：</span>
                <span class="value">{{ currentRecord.caregiverName }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h4>执行信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="label">执行日期：</span>
                <span class="value">{{ formatDate(currentRecord.executionDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">执行时间：</span>
                <span class="value">{{ currentRecord.executionTime || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">实际时长：</span>
                <span class="value">{{ currentRecord.duration }} 分钟</span>
              </div>
              <div class="detail-item">
                <span class="label">标准时长：</span>
                <span class="value">{{ currentRecord.standardDuration || '-' }} 分钟</span>
              </div>
              <div class="detail-item">
                <span class="label">执行状态：</span>
                <span class="value status-badge" :class="getStatusClass(currentRecord.executionStatus)">
                  {{ currentRecord.executionStatus }}
                </span>
              </div>
              <div class="detail-item">
                <span class="label">记录时间：</span>
                <span class="value">{{ formatDateTime(currentRecord.createdAt) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section" v-if="currentRecord.notes">
            <h4>备注信息</h4>
            <p class="notes-content">{{ currentRecord.notes }}</p>
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
import { nursingRecordApi } from '@/utils/nursingRecordApi'
import type {
  NursingRecord,
  NursingRecordQueryParams,
  NursingRecordStats,
  NursingRecordForm,
  NursingProject,
  Customer,
  User
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const recordList = ref<NursingRecord[]>([])
const customerList = ref<Customer[]>([])
const caregiverList = ref<User[]>([])
const projectList = ref<NursingProject[]>([])
const statusOptions = ref<string[]>([])
const selectedRecords = ref<number[]>([])
const selectAll = ref<boolean>(false)
const showModal = ref<boolean>(false)
const showQuickRecord = ref<boolean>(false)
const showDetails = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentRecord = ref<NursingRecord | null>(null)
const selectedProject = ref<NursingProject | null>(null)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const recordStats = ref<NursingRecordStats>({
  total: 0,
  completed: 0,
  partialCompleted: 0,
  unExecuted: 0,
  abnormal: 0,
  completionRate: 0
})

// 当前用户
const currentUser = ref<User | null>(null)

// 搜索表单
const searchForm = reactive<NursingRecordQueryParams>({
  searchKeyword: '',
  executionStatus: '',
  startDate: '',
  endDate: '',
  customerId: undefined,
  projectId: undefined,
  caregiverId: undefined,
  page: 1,
  size: 10
})

// 护理记录表单
const recordForm = reactive<NursingRecordForm>({
  customerId: undefined,
  projectId: undefined,
  caregiverId: undefined,
  executionDate: new Date().toISOString().split('T')[0],
  executionTime: '',
  duration: undefined,
  executionStatus: '已完成',
  notes: ''
})

// 快速记录表单
const quickRecordForm = reactive({
  customerId: undefined as number | undefined,
  projectId: undefined as number | undefined,
  caregiverId: undefined as number | undefined,
  notes: ''
})

// 计算属性
const selectAllState = computed(() => {
  if (recordList.value.length === 0) return false
  return selectedRecords.value.length === recordList.value.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const formatDate = (dateStr: string): string => {
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const formatDateTime = (dateStr?: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '已完成': 'completed',
    '部分完成': 'partial',
    '未执行': 'unexecuted',
    '异常': 'abnormal'
  }
  return statusMap[status] || 'default'
}

const loadRecordList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await nursingRecordApi.getRecordList(params)

    if (response.code === 200) {
      recordList.value = response.data.records
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载护理记录失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadCustomerList = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getCustomerList()
    if (response.code === 200) {
      customerList.value = response.data
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const loadCaregiverList = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getCaregiverList()
    if (response.code === 200) {
      caregiverList.value = response.data
    }
  } catch (error) {
    console.error('加载护理人员列表失败:', error)
  }
}

const loadProjectList = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getActiveProjects()
    if (response.code === 200) {
      projectList.value = response.data
    }
  } catch (error) {
    console.error('加载护理项目列表失败:', error)
  }
}

const loadStatusOptions = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getStatusOptions()
    if (response.code === 200) {
      statusOptions.value = response.data
    }
  } catch (error) {
    console.error('加载状态选项失败:', error)
  }
}

const loadRecordStats = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getRecordStats()
    if (response.code === 200) {
      recordStats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadRecordList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    executionStatus: '',
    startDate: '',
    endDate: '',
    customerId: undefined,
    projectId: undefined,
    caregiverId: undefined
  })
  handleSearch()
}

const loadTodayRecords = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getTodayRecords()
    if (response.code === 200) {
      recordList.value = response.data
      showMessage('已加载今日记录')
    }
  } catch (error) {
    showMessage('加载今日记录失败', 'error')
  }
}

const loadUncompletedRecords = async (): Promise<void> => {
  try {
    const response = await nursingRecordApi.getUncompletedRecords()
    if (response.code === 200) {
      recordList.value = response.data
      showMessage('已加载未完成记录')
    }
  } catch (error) {
    showMessage('加载未完成记录失败', 'error')
  }
}

const loadMyRecords = (): void => {
  if (currentUser.value) {
    searchForm.caregiverId = currentUser.value.id
    handleSearch()
  }
}

const handleSelectAll = (): void => {
  if (selectAll.value) {
    selectedRecords.value = recordList.value.map(record => record.id!).filter(id => id !== undefined)
  } else {
    selectedRecords.value = []
  }
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadRecordList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadRecordList()
}

const showAddModal = (): void => {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

const editRecord = (record: NursingRecord): void => {
  isEditing.value = true
  Object.assign(recordForm, {
    id: record.id,
    customerId: record.customerId,
    projectId: record.projectId,
    caregiverId: record.caregiverId,
    executionDate: record.executionDate,
    executionTime: record.executionTime || '',
    duration: record.duration,
    executionStatus: record.executionStatus,
    notes: record.notes || ''
  })
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(recordForm, {
    id: undefined,
    customerId: undefined,
    projectId: undefined,
    caregiverId: currentUser.value?.id,
    executionDate: new Date().toISOString().split('T')[0],
    executionTime: '',
    duration: undefined,
    executionStatus: '已完成',
    notes: ''
  })
  selectedProject.value = null
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const onProjectChange = (): void => {
  if (recordForm.projectId) {
    selectedProject.value = projectList.value.find(p => p.id === recordForm.projectId) || null
    if (selectedProject.value) {
      recordForm.duration = selectedProject.value.standardDuration
    }
  } else {
    selectedProject.value = null
  }
}

const submitForm = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = isEditing.value
        ? await nursingRecordApi.updateRecord(recordForm)
        : await nursingRecordApi.addRecord(recordForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadRecordList()
      loadRecordStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('操作失败', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteRecord = async (record: NursingRecord): Promise<void> => {
  if (!confirm(`确认删除客户 ${record.customerName} 的护理记录吗？`)) return

  try {
    const response = await nursingRecordApi.deleteRecord(record.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadRecordList()
      loadRecordStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchDelete = async (): Promise<void> => {
  if (selectedRecords.value.length === 0) return

  if (!confirm(`确认删除选中的 ${selectedRecords.value.length} 条护理记录吗？`)) return

  try {
    const response = await nursingRecordApi.deleteRecords(selectedRecords.value)
    if (response.code === 200) {
      showMessage('批量删除成功', 'success')
      selectedRecords.value = []
      selectAll.value = false
      loadRecordList()
      loadRecordStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const showQuickRecordModal = (): void => {
  Object.assign(quickRecordForm, {
    customerId: undefined,
    projectId: undefined,
    caregiverId: currentUser.value?.id,
    notes: ''
  })
  showQuickRecord.value = true
}

const closeQuickRecordModal = (): void => {
  showQuickRecord.value = false
}

const submitQuickRecord = async (): Promise<void> => {
  if (!quickRecordForm.customerId || !quickRecordForm.projectId) {
    showMessage('请选择客户和护理项目', 'error')
    return
  }

  submitting.value = true
  try {
    const response = await nursingRecordApi.quickRecord({
      customerId: quickRecordForm.customerId,
      projectId: quickRecordForm.projectId,
      caregiverId: quickRecordForm.caregiverId || currentUser.value?.id,
      notes: quickRecordForm.notes
    })

    if (response.code === 200) {
      showMessage('快速记录成功', 'success')
      closeQuickRecordModal()
      loadRecordList()
      loadRecordStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('快速记录失败', 'error')
  } finally {
    submitting.value = false
  }
}

const viewDetails = (record: NursingRecord): void => {
  currentRecord.value = record
  showDetails.value = true
}

const closeDetailsModal = (): void => {
  showDetails.value = false
  currentRecord.value = null
}

// 生命周期
onMounted(() => {
  // 获取当前登录用户信息
  const userStr = localStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr) as User
  }

  loadRecordList()
  loadCustomerList()
  loadCaregiverList()
  loadProjectList()
  loadStatusOptions()
  loadRecordStats()
})

// 监听选中状态
watch(selectedRecords, () => {
  selectAll.value = selectAllState.value
}, { deep: true })
</script>

<style scoped>
.nursing-records {
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

.filter-group select,
.date-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.add-btn, .quick-btn, .delete-btn {
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

.quick-btn {
  background: #f39c12;
  color: white;
}

.quick-btn:hover {
  background: #e67e22;
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
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
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

.stat-card.completed {
  border-left-color: #27ae60;
}

.stat-card.partial {
  border-left-color: #f39c12;
}

.stat-card.unexecuted {
  border-left-color: #e74c3c;
}

.stat-card.abnormal {
  border-left-color: #9b59b6;
}

.stat-card.rate {
  border-left-color: #1abc9c;
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

/* 快捷操作样式 */
.quick-actions {
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.action-group h4 {
  margin: 0 0 10px 0;
  color: #2c3e50;
  font-size: 16px;
}

.quick-action-btn {
  background: #ecf0f1;
  color: #2c3e50;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
  transition: all 0.3s;
}

.quick-action-btn:hover {
  background: #3498db;
  color: white;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.records-table {
  width: 100%;
  border-collapse: collapse;
}

.records-table th,
.records-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.records-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.records-table tr:hover {
  background: #f8f9fa;
}

.customer-name {
  font-weight: 600;
  color: #3498db;
}

.project-name {
  font-weight: 600;
  color: #27ae60;
}

.category-tag {
  background: #e8f5e8;
  color: #27ae60;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.caregiver-name {
  color: #8e44ad;
}

.duration .actual {
  font-weight: 600;
  color: #2c3e50;
}

.duration .standard {
  color: #7f8c8d;
  font-size: 12px;
}

.notes {
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
}

.status-badge.completed {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.partial {
  background: #fdeaa7;
  color: #f39c12;
}

.status-badge.unexecuted {
  background: #fadbd8;
  color: #e74c3c;
}

.status-badge.abnormal {
  background: #e8d5ff;
  color: #9b59b6;
}

.actions {
  display: flex;
  gap: 5px;
}

.actions button {
  padding: 5px 10px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.edit-btn {
  background: #3498db;
  color: white;
}

.edit-btn:hover {
  background: #2980b9;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background: #c0392b;
}

.view-btn {
  background: #95a5a6;
  color: white;
}

.view-btn:hover {
  background: #7f8c8d;
}

/* 分页、弹窗等样式与之前的组件类似 */
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
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.small {
  max-width: 500px;
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
.record-form,
.quick-record-form {
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

.form-group small {
  margin-top: 5px;
  color: #7f8c8d;
  font-size: 12px;
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

/* 详情样式 */
.details-content {
  padding: 0 20px 20px;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 16px;
  border-bottom: 2px solid #3498db;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.detail-item .label {
  font-weight: 600;
  color: #7f8c8d;
  font-size: 14px;
}

.detail-item .value {
  color: #2c3e50;
  font-size: 14px;
}

.notes-content {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  color: #2c3e50;
  line-height: 1.6;
  margin: 0;
}

/* 加载和空状态 */
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
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .records-table {
    font-size: 12px;
  }

  .records-table th,
  .records-table td {
    padding: 8px 6px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>