<template>
  <div class="customer-care-settings">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>客户护理设置</h1>
      <p>管理客户与护理人员的分配关系</p>
    </div>

    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchKeyword"
              placeholder="搜索客户姓名或护理人员..."
              class="search-input"
              @keyup.enter="loadAssignmentList"
          >
          <button @click="loadAssignmentList" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="selectedCustomer" @change="handleCustomerChange">
            <option value="">全部客户</option>
            <option v-for="customer in customerList"
                    :key="customer.id"
                    :value="customer.id">
              {{ customer.name }} - {{ customer.idCard?.slice(-4) }}
            </option>
          </select>

          <select v-model="selectedCaregiver" @change="handleCaregiverChange">
            <option value="">全部护理人员</option>
            <option v-for="caregiver in caregiverList"
                    :key="caregiver.id"
                    :value="caregiver.id">
              {{ caregiver.realName }} - {{ caregiver.phone }}
            </option>
          </select>

          <button @click="resetFilters" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showAssignModal" class="add-btn">
          <span class="icon">+</span>
          新增分配
        </button>
        <button @click="showBatchAssignModal" class="batch-btn">
          <span class="icon">👥</span>
          批量分配
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ assignmentStats.totalCaregivers }}</div>
        <div class="stat-label">护理人员总数</div>
      </div>
      <div class="stat-card active">
        <div class="stat-number">{{ assignmentStats.activeCaregivers }}</div>
        <div class="stat-label">活跃护理人员</div>
      </div>
      <div class="stat-card customers">
        <div class="stat-number">{{ assignmentStats.totalCustomers }}</div>
        <div class="stat-label">客户总数</div>
      </div>
      <div class="stat-card average">
        <div class="stat-number">{{ assignmentStats.averageCustomersPerCaregiver }}</div>
        <div class="stat-label">平均客户数/人</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-tabs">
      <div class="tab-buttons">
        <button
            :class="['tab-btn', { active: activeTab === 'assignments' }]"
            @click="activeTab = 'assignments'"
        >
          分配关系
        </button>
        <button
            :class="['tab-btn', { active: activeTab === 'workload' }]"
            @click="switchToWorkload"
        >
          工作负载
        </button>
        <button
            :class="['tab-btn', { active: activeTab === 'available' }]"
            @click="switchToAvailable"
        >
          可用护理人员
        </button>
      </div>

      <!-- 分配关系列表 -->
      <div v-if="activeTab === 'assignments'" class="tab-content">
        <div class="table-container">
          <table class="assignment-table">
            <thead>
            <tr>
              <th>客户姓名</th>
              <th>护理人员</th>
              <th>分配日期</th>
              <th>结束日期</th>
              <th>主要护理人</th>
              <th>状态</th>
              <th width="200">操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-if="loading" class="loading-row">
              <td colspan="7" class="loading-cell">
                <div class="loading-spinner">加载中...</div>
              </td>
            </tr>
            <tr v-else-if="assignmentList.length === 0" class="empty-row">
              <td colspan="7" class="empty-cell">
                <div class="empty-state">
                  <span class="empty-icon">👥</span>
                  <p>暂无分配关系数据</p>
                </div>
              </td>
            </tr>
            <tr v-else v-for="assignment in assignmentList" :key="assignment.id" class="assignment-row">
              <td class="customer-name">{{ assignment.customerName }}</td>
              <td class="caregiver-name">{{ assignment.caregiverName }}</td>
              <td>{{ formatDate(assignment.assignmentDate) }}</td>
              <td>{{ assignment.endDate ? formatDate(assignment.endDate) : '-' }}</td>
              <td>
                <span
                    class="primary-badge"
                    :class="assignment.primaryCaregiver ? 'primary' : 'secondary'"
                >
                  {{ assignment.primaryCaregiver ? '主要' : '辅助' }}
                </span>
              </td>
              <td>
                <span
                    class="status-badge"
                    :class="assignment.status === 1 ? 'active' : 'inactive'"
                >
                  {{ assignment.status === 1 ? '有效' : '无效' }}
                </span>
              </td>
              <td class="actions">
                <button @click="editAssignment(assignment)" class="edit-btn">编辑</button>
                <button @click="setPrimary(assignment)" class="primary-btn" v-if="!assignment.primaryCaregiver">设为主要</button>
                <button @click="deleteAssignment(assignment)" class="delete-btn">删除</button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 工作负载统计 -->
      <div v-if="activeTab === 'workload'" class="tab-content">
        <div class="workload-grid">
          <div v-for="workload in workloadList" :key="workload.caregiver_id" class="workload-card">
            <div class="caregiver-info">
              <h4>{{ workload.caregiver_name }}</h4>
              <p class="phone">{{ workload.phone }}</p>
              <p class="role">{{ workload.role_name }}</p>
            </div>
            <div class="stats">
              <div class="stat-item">
                <span class="number">{{ workload.customer_count }}</span>
                <span class="label">客户总数</span>
              </div>
              <div class="stat-item">
                <span class="number">{{ workload.primary_count }}</span>
                <span class="label">主要客户</span>
              </div>
            </div>
            <div class="progress-bar">
              <div class="progress" :style="{ width: getWorkloadPercent(workload.customer_count) + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 可用护理人员 -->
      <div v-if="activeTab === 'available'" class="tab-content">
        <div class="available-grid">
          <div v-for="caregiver in availableCaregivers" :key="caregiver.id" class="caregiver-card">
            <div class="avatar">
              {{ caregiver.realName.charAt(0) }}
            </div>
            <div class="info">
              <h4>{{ caregiver.realName }}</h4>
              <p>{{ caregiver.phone }}</p>
              <p class="role">{{ getRoleName(caregiver.roleId) }}</p>
            </div>
            <div class="actions">
              <button @click="quickAssign(caregiver)" class="assign-btn">快速分配</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑分配弹窗 -->
    <div v-if="showAssignmentModal" class="modal-overlay" @click="closeAssignmentModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑分配关系' : '新增分配关系' }}</h3>
          <button @click="closeAssignmentModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitAssignment" class="assignment-form">
          <div class="form-row">
            <div class="form-group">
              <label>客户 *</label>
              <select v-model="assignmentForm.customerId" required :disabled="isEditing">
                <option value="">请选择客户</option>
                <option v-for="customer in customerList"
                        :key="customer.id"
                        :value="customer.id">
                  {{ customer.name }} - {{ customer.idCard?.slice(-4) }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>护理人员 *</label>
              <select v-model="assignmentForm.caregiverId" required>
                <option value="">请选择护理人员</option>
                <option v-for="caregiver in caregiverList"
                        :key="caregiver.id"
                        :value="caregiver.id">
                  {{ caregiver.realName }} - {{ caregiver.phone }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>分配日期 *</label>
              <input
                  type="date"
                  v-model="assignmentForm.assignmentDate"
                  required
              >
            </div>

            <div class="form-group">
              <label>结束日期</label>
              <input
                  type="date"
                  v-model="assignmentForm.endDate"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>是否主要护理人</label>
              <select v-model="assignmentForm.primaryCaregiver">
                <option :value="1">是</option>
                <option :value="0">否</option>
              </select>
            </div>

            <div class="form-group">
              <label>状态</label>
              <select v-model="assignmentForm.status">
                <option :value="1">有效</option>
                <option :value="0">无效</option>
              </select>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeAssignmentModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : (isEditing ? '更新' : '添加') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 批量分配弹窗 -->
    <div v-if="showBatchModal" class="modal-overlay" @click="closeBatchModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>批量分配护理人员</h3>
          <button @click="closeBatchModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitBatchAssign" class="batch-form">
          <div class="form-group">
            <label>选择客户 *</label>
            <select v-model="batchForm.customerId" required @change="loadCustomerInfo">
              <option value="">请选择客户</option>
              <option v-for="customer in customerList"
                      :key="customer.id"
                      :value="customer.id">
                {{ customer.name }} - {{ customer.idCard?.slice(-4) }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>护理人员列表 *</label>
            <div class="caregiver-selection">
              <div v-for="caregiver in caregiverList" :key="caregiver.id" class="caregiver-item">
                <input
                    type="checkbox"
                    :id="'caregiver_' + caregiver.id"
                    v-model="batchForm.caregiverIds"
                    :value="caregiver.id"
                >
                <label :for="'caregiver_' + caregiver.id">
                  {{ caregiver.realName }} - {{ caregiver.phone }}
                </label>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>主要护理人员</label>
            <select v-model="batchForm.primaryCaregiverId">
              <option value="">请选择主要护理人员</option>
              <option v-for="id in batchForm.caregiverIds" :key="id" :value="id">
                {{ getCaregiverName(id) }}
              </option>
            </select>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeBatchModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting || batchForm.caregiverIds.length === 0" class="submit-btn">
              {{ submitting ? '分配中...' : '确认分配' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 快速分配弹窗 -->
    <div v-if="showQuickAssignModal" class="modal-overlay" @click="closeQuickAssignModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>快速分配 - {{ selectedCaregiverForQuick?.realName }}</h3>
          <button @click="closeQuickAssignModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitQuickAssign" class="quick-form">
          <div class="form-group">
            <label>选择客户 *</label>
            <select v-model="quickAssignForm.customerId" required>
              <option value="">请选择客户</option>
              <option v-for="customer in customerList"
                      :key="customer.id"
                      :value="customer.id">
                {{ customer.name }} - {{ customer.idCard?.slice(-4) }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>是否设为主要护理人</label>
            <select v-model="quickAssignForm.primaryCaregiver">
              <option :value="1">是</option>
              <option :value="0">否</option>
            </select>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeQuickAssignModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '分配中...' : '确认分配' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { customerCareApi } from '@/utils/customerCareApi'
import type {
  CustomerCareAssignment,
  CustomerCareAssignmentForm,
  CustomerCareStats,
  CaregiverWorkload,
  Customer,
  User
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const activeTab = ref<string>('assignments')
const searchKeyword = ref<string>('')
const selectedCustomer = ref<string>('')
const selectedCaregiver = ref<string>('')

const assignmentList = ref<CustomerCareAssignment[]>([])
const workloadList = ref<CaregiverWorkload[]>([])
const availableCaregivers = ref<User[]>([])
const customerList = ref<Customer[]>([])
const caregiverList = ref<User[]>([])

const showAssignmentModal = ref<boolean>(false)
const showBatchModal = ref<boolean>(false)
const showQuickAssignModal = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const selectedCaregiverForQuick = ref<User | null>(null)

const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 统计数据
const assignmentStats = ref<CustomerCareStats>({
  totalCaregivers: 0,
  activeCaregivers: 0,
  totalCustomers: 0,
  averageCustomersPerCaregiver: 0
})

// 表单数据
const assignmentForm = reactive<CustomerCareAssignmentForm>({
  customerId: undefined,
  caregiverId: undefined,
  assignmentDate: new Date().toISOString().split('T')[0],
  endDate: '',
  primaryCaregiver: 0,
  status: 1
})

const batchForm = reactive({
  customerId: undefined as number | undefined,
  caregiverIds: [] as number[],
  primaryCaregiverId: undefined as number | undefined
})

const quickAssignForm = reactive({
  customerId: undefined as number | undefined,
  caregiverId: undefined as number | undefined,
  primaryCaregiver: 0
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

const getRoleName = (roleId: number): string => {
  const roleMap: Record<number, string> = {
    1: '系统管理员',
    2: '健康管家',
    3: '医护人员'
  }
  return roleMap[roleId] || '未知角色'
}

const getCaregiverName = (id: number): string => {
  const caregiver = caregiverList.value.find(c => c.id === id)
  return caregiver ? caregiver.realName : ''
}

const getWorkloadPercent = (count: number): number => {
  const maxCount = Math.max(...workloadList.value.map(w => w.customer_count))
  return maxCount > 0 ? (count / maxCount) * 100 : 0
}

const loadAssignmentList = async (): Promise<void> => {
  loading.value = true
  try {
    let assignments: CustomerCareAssignment[] = []

    if (selectedCustomer.value) {
      const response = await customerCareApi.getCustomerAssignments(Number(selectedCustomer.value))
      if (response.code === 200) {
        assignments = response.data
      }
    } else if (selectedCaregiver.value) {
      const response = await customerCareApi.getCaregiverAssignments(Number(selectedCaregiver.value))
      if (response.code === 200) {
        assignments = response.data
      }
    } else {
      // 加载所有分配关系 - 这里需要一个通用的列表接口
      const response = await customerCareApi.getAllAssignments()
      if (response.code === 200) {
        assignments = response.data
      }
    }

    assignmentList.value = assignments
  } catch (error) {
    showMessage('加载分配关系失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadWorkloadStats = async (): Promise<void> => {
  try {
    const response = await customerCareApi.getCaregiverWorkloadStats()
    if (response.code === 200) {
      workloadList.value = response.data
    }
  } catch (error) {
    console.error('加载工作负载统计失败:', error)
  }
}

const loadAvailableCaregivers = async (): Promise<void> => {
  try {
    const response = await customerCareApi.getAvailableCaregivers()
    if (response.code === 200) {
      availableCaregivers.value = response.data
    }
  } catch (error) {
    console.error('加载可用护理人员失败:', error)
  }
}

const loadCustomerList = async (): Promise<void> => {
  try {
    const response = await customerCareApi.getCustomerList()
    if (response.code === 200) {
      customerList.value = response.data
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const loadCaregiverList = async (): Promise<void> => {
  try {
    const response = await customerCareApi.getCaregiverList()
    if (response.code === 200) {
      caregiverList.value = response.data
    }
  } catch (error) {
    console.error('加载护理人员列表失败:', error)
  }
}

const loadAssignmentStats = async (): Promise<void> => {
  try {
    const response = await customerCareApi.getAssignmentStats()
    if (response.code === 200) {
      assignmentStats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleCustomerChange = (): void => {
  selectedCaregiver.value = ''
  loadAssignmentList()
}

const handleCaregiverChange = (): void => {
  selectedCustomer.value = ''
  loadAssignmentList()
}

const resetFilters = (): void => {
  searchKeyword.value = ''
  selectedCustomer.value = ''
  selectedCaregiver.value = ''
  loadAssignmentList()
}

const switchToWorkload = (): void => {
  activeTab.value = 'workload'
  loadWorkloadStats()
}

const switchToAvailable = (): void => {
  activeTab.value = 'available'
  loadAvailableCaregivers()
}

const showAssignModal = (): void => {
  isEditing.value = false
  resetAssignmentForm()
  showAssignmentModal.value = true
}

const editAssignment = (assignment: CustomerCareAssignment): void => {
  isEditing.value = true
  Object.assign(assignmentForm, {
    id: assignment.id,
    customerId: assignment.customerId,
    caregiverId: assignment.caregiverId,
    assignmentDate: assignment.assignmentDate,
    endDate: assignment.endDate || '',
    primaryCaregiver: assignment.primaryCaregiver,
    status: assignment.status
  })
  showAssignmentModal.value = true
}

const resetAssignmentForm = (): void => {
  Object.assign(assignmentForm, {
    id: undefined,
    customerId: undefined,
    caregiverId: undefined,
    assignmentDate: new Date().toISOString().split('T')[0],
    endDate: '',
    primaryCaregiver: 0,
    status: 1
  })
}

const closeAssignmentModal = (): void => {
  showAssignmentModal.value = false
  resetAssignmentForm()
}

const submitAssignment = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = isEditing.value
        ? await customerCareApi.updateAssignment(assignmentForm)
        : await customerCareApi.createAssignment(assignmentForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeAssignmentModal()
      loadAssignmentList()
      loadAssignmentStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('操作失败', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteAssignment = async (assignment: CustomerCareAssignment): Promise<void> => {
  if (!confirm(`确认删除 ${assignment.customerName} 和 ${assignment.caregiverName} 的分配关系吗？`)) return

  try {
    const response = await customerCareApi.deleteAssignment(assignment.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadAssignmentList()
      loadAssignmentStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const setPrimary = async (assignment: CustomerCareAssignment): Promise<void> => {
  try {
    const response = await customerCareApi.setPrimaryCaregiver(assignment.customerId!, assignment.caregiverId!)
    if (response.code === 200) {
      showMessage('设置主要护理人员成功', 'success')
      loadAssignmentList()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('设置失败', 'error')
  }
}

const showBatchAssignModal = (): void => {
  Object.assign(batchForm, {
    customerId: undefined,
    caregiverIds: [],
    primaryCaregiverId: undefined
  })
  showBatchModal.value = true
}

const closeBatchModal = (): void => {
  showBatchModal.value = false
}

const loadCustomerInfo = (): void => {
  // 当选择客户后，可以加载该客户的当前护理人员信息
}

const submitBatchAssign = async (): Promise<void> => {
  if (!batchForm.customerId || batchForm.caregiverIds.length === 0) {
    showMessage('请选择客户和护理人员', 'error')
    return
  }

  submitting.value = true
  try {
    const response = await customerCareApi.batchAssignCaregivers(
        batchForm.customerId,
        batchForm.caregiverIds,
        batchForm.primaryCaregiverId
    )

    if (response.code === 200) {
      showMessage('批量分配成功', 'success')
      closeBatchModal()
      loadAssignmentList()
      loadAssignmentStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量分配失败', 'error')
  } finally {
    submitting.value = false
  }
}

const quickAssign = (caregiver: User): void => {
  selectedCaregiverForQuick.value = caregiver
  quickAssignForm.caregiverId = caregiver.id
  quickAssignForm.customerId = undefined
  quickAssignForm.primaryCaregiver = 0
  showQuickAssignModal.value = true
}

const closeQuickAssignModal = (): void => {
  showQuickAssignModal.value = false
  selectedCaregiverForQuick.value = null
}

const submitQuickAssign = async (): Promise<void> => {
  if (!quickAssignForm.customerId || !quickAssignForm.caregiverId) {
    showMessage('请选择客户', 'error')
    return
  }

  submitting.value = true
  try {
    const assignmentData: CustomerCareAssignmentForm = {
      customerId: quickAssignForm.customerId,
      caregiverId: quickAssignForm.caregiverId,
      assignmentDate: new Date().toISOString().split('T')[0],
      primaryCaregiver: quickAssignForm.primaryCaregiver,
      status: 1
    }

    const response = await customerCareApi.createAssignment(assignmentData)
    if (response.code === 200) {
      showMessage('快速分配成功', 'success')
      closeQuickAssignModal()
      loadAssignmentList()
      loadAssignmentStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('快速分配失败', 'error')
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(() => {
  loadAssignmentList()
  loadCustomerList()
  loadCaregiverList()
  loadAssignmentStats()
})
</script>

<style scoped>
.customer-care-settings {
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

.add-btn, .batch-btn {
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

.batch-btn {
  background: #3498db;
  color: white;
}

.batch-btn:hover {
  background: #2980b9;
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

.stat-card.customers {
  border-left-color: #e74c3c;
}

.stat-card.average {
  border-left-color: #f39c12;
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

/* 标签页样式 */
.content-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #ecf0f1;
}

.tab-btn {
  flex: 1;
  padding: 15px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  color: #7f8c8d;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.tab-btn.active {
  color: #3498db;
  border-bottom-color: #3498db;
  background: #f8f9fa;
}

.tab-content {
  padding: 20px;
}

/* 表格样式 */
.table-container {
  overflow-x: auto;
}

.assignment-table {
  width: 100%;
  border-collapse: collapse;
}

.assignment-table th,
.assignment-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.assignment-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.assignment-table tr:hover {
  background: #f8f9fa;
}

.customer-name {
  font-weight: 600;
  color: #3498db;
}

.caregiver-name {
  color: #27ae60;
  font-weight: 500;
}

.primary-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.primary-badge.primary {
  background: #e8f5e8;
  color: #27ae60;
}

.primary-badge.secondary {
  background: #e3f2fd;
  color: #2196f3;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
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

.primary-btn {
  background: #f39c12;
  color: white;
}

.primary-btn:hover {
  background: #e67e22;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover {
  background: #c0392b;
}

/* 工作负载网格 */
.workload-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.workload-card {
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.caregiver-info h4 {
  margin-bottom: 8px;
  color: #2c3e50;
}

.caregiver-info .phone {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 4px;
}

.caregiver-info .role {
  color: #3498db;
  font-size: 12px;
  background: #e3f2fd;
  padding: 2px 6px;
  border-radius: 12px;
  display: inline-block;
}

.stats {
  display: flex;
  gap: 20px;
  margin: 15px 0;
}

.stat-item {
  text-align: center;
}

.stat-item .number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-item .label {
  font-size: 12px;
  color: #7f8c8d;
}

.progress-bar {
  height: 6px;
  background: #ecf0f1;
  border-radius: 3px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, #27ae60, #2ecc71);
  transition: width 0.3s ease;
}

/* 可用护理人员网格 */
.available-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.caregiver-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.caregiver-card:hover {
  transform: translateY(-2px);
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
}

.info {
  flex: 1;
}

.info h4 {
  margin-bottom: 4px;
  color: #2c3e50;
}

.info p {
  margin: 2px 0;
  font-size: 14px;
  color: #7f8c8d;
}

.info .role {
  background: #e8f5e8;
  color: #27ae60;
  padding: 2px 6px;
  border-radius: 12px;
  font-size: 12px;
}

.assign-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.assign-btn:hover {
  background: #219a52;
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
.assignment-form,
.batch-form,
.quick-form {
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

.form-group label {
  margin-bottom: 5px;
  font-weight: 500;
  color: #2c3e50;
}

.form-group input,
.form-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3498db;
}

.caregiver-selection {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
}

.caregiver-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid #f1f1f1;
}

.caregiver-item:last-child {
  border-bottom: none;
}

.caregiver-item input[type="checkbox"] {
  margin: 0;
}

.caregiver-item label {
  margin: 0;
  cursor: pointer;
  flex: 1;
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

  .assignment-table {
    font-size: 12px;
  }

  .assignment-table th,
  .assignment-table td {
    padding: 8px 6px;
  }

  .workload-grid {
    grid-template-columns: 1fr;
  }

  .available-grid {
    grid-template-columns: 1fr;
  }
}
</style>