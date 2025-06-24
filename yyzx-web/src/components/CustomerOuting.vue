<!-- src/components/CustomerOuting.vue -->
<template>
  <div class="customer-outing">
    <div class="page-header">
      <h1>外出登记管理</h1>
      <p>管理客户外出申请、审批和返回登记</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总外出次数</div>
      </div>
      <div class="stat-card approved">
        <div class="stat-number">{{ stats.approved }}</div>
        <div class="stat-label">已批准</div>
      </div>
      <div class="stat-card pending">
        <div class="stat-number">{{ stats.pending }}</div>
        <div class="stat-label">待审批</div>
      </div>
      <div class="stat-card unreturned">
        <div class="stat-number">{{ stats.unreturned }}</div>
        <div class="stat-label">未返回</div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索客户姓名、目的地或陪同人员..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.approvalStatus" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="待批准">待批准</option>
            <option value="已批准">已批准</option>
            <option value="已拒绝">已拒绝</option>
          </select>

          <input
              type="date"
              v-model="searchForm.outingDate"
              @change="handleSearch"
              class="date-input"
          >

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showOutingModal" class="add-btn">
          <span class="icon">+</span>
          外出申请
        </button>

        <button
            @click="batchApprove"
            :disabled="selectedRecords.length === 0"
            class="approve-btn"
        >
          <span class="icon">✓</span>
          批量审批 ({{ selectedRecords.length }})
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

    <!-- 外出记录列表 -->
    <div class="table-container">
      <table class="outing-table">
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
          <th>外出日期</th>
          <th>外出时间</th>
          <th>预计返回</th>
          <th>实际返回</th>
          <th>目的地</th>
          <th>陪同人员</th>
          <th>外出原因</th>
          <th>审批状态</th>
          <th>登记人</th>
          <th width="200">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="12" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="outingList.length === 0" class="empty-row">
          <td colspan="12" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">🚶</span>
              <p>暂无外出记录</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="record in outingList" :key="record.id" class="outing-row">
          <td>
            <input
                type="checkbox"
                :value="record.id"
                v-model="selectedRecords"
            >
          </td>
          <td class="customer-name">{{ record.customerName }}</td>
          <td>{{ record.outingDate }}</td>
          <td>{{ record.outingTime }}</td>
          <td>{{ record.returnTime || '-' }}</td>
          <td class="return-time">
            {{ record.actualReturnTime || '-' }}
          </td>
          <td class="destination">{{ record.destination || '-' }}</td>
          <td>{{ record.companion || '-' }}</td>
          <td class="reason">{{ record.reason || '-' }}</td>
          <td>
              <span
                  class="status-badge"
                  :class="getApprovalStatusClass(record.approvalStatus)"
              >
                {{ record.approvalStatus }}
              </span>
          </td>
          <td>{{ record.caregiverName || '-' }}</td>
          <td class="actions">
            <button
                v-if="record.approvalStatus === '待批准'"
                @click="approveRecord(record)"
                class="approve-btn"
            >
              批准
            </button>
            <button
                v-if="record.approvalStatus === '待批准'"
                @click="rejectRecord(record)"
                class="reject-btn"
            >
              拒绝
            </button>
            <button
                v-if="record.approvalStatus === '已批准' && !record.actualReturnTime"
                @click="markReturn(record)"
                class="return-btn"
            >
              登记返回
            </button>
            <button @click="editRecord(record)" class="edit-btn">编辑</button>
            <button @click="deleteRecord(record)" class="delete-btn">删除</button>
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

    <!-- 添加/编辑外出申请弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑外出申请' : '添加外出申请' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="outing-form">
          <div class="form-row">
            <div class="form-group">
              <label>客户 *</label>
              <select v-model="outingForm.customerId" required>
                <option value="">请选择客户</option>
                <option
                    v-for="customer in inServiceCustomers"
                    :key="customer.id"
                    :value="customer.id"
                >
                  {{ customer.name }} ({{ customer.bedNumber }})
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>外出日期 *</label>
              <input
                  type="date"
                  v-model="outingForm.outingDate"
                  required
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>外出时间 *</label>
              <input
                  type="time"
                  v-model="outingForm.outingTime"
                  required
              >
            </div>

            <div class="form-group">
              <label>预计返回时间</label>
              <input
                  type="time"
                  v-model="outingForm.returnTime"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>目的地</label>
              <input
                  type="text"
                  v-model="outingForm.destination"
                  placeholder="请输入外出目的地"
              >
            </div>

            <div class="form-group">
              <label>陪同人员</label>
              <input
                  type="text"
                  v-model="outingForm.companion"
                  placeholder="请输入陪同人员"
              >
            </div>
          </div>

          <div class="form-group full-width">
            <label>外出原因</label>
            <textarea
                v-model="outingForm.reason"
                placeholder="请输入外出原因"
                rows="3"
            ></textarea>
          </div>

          <div class="form-group full-width">
            <label>备注</label>
            <textarea
                v-model="outingForm.notes"
                placeholder="请输入备注信息"
                rows="2"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : (isEditing ? '更新' : '提交申请') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 批量审批弹窗 -->
    <div v-if="showBatchApprovalModal" class="modal-overlay" @click="closeBatchApprovalModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>批量审批外出申请</h3>
          <button @click="closeBatchApprovalModal" class="close-btn">×</button>
        </div>

        <div class="batch-approval-form">
          <p>已选择 {{ selectedRecords.length }} 条外出申请</p>

          <div class="form-group">
            <label>审批结果：</label>
            <select v-model="batchApprovalStatus">
              <option value="已批准">批准</option>
              <option value="已拒绝">拒绝</option>
            </select>
          </div>

          <div class="form-actions">
            <button @click="closeBatchApprovalModal" class="cancel-btn">取消</button>
            <button @click="confirmBatchApproval" :disabled="submitting" class="submit-btn">
              {{ submitting ? '处理中...' : '确认审批' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 返回登记弹窗 -->
    <div v-if="showReturnModal" class="modal-overlay" @click="closeReturnModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>登记返回时间</h3>
          <button @click="closeReturnModal" class="close-btn">×</button>
        </div>

        <div class="return-form">
          <p>客户：{{ currentRecord?.customerName }}</p>
          <p>外出时间：{{ currentRecord?.outingDate }} {{ currentRecord?.outingTime }}</p>

          <div class="form-group">
            <label>实际返回时间：</label>
            <input
                type="time"
                v-model="returnTime"

            >
          </div>

          <div class="form-actions">
            <button @click="closeReturnModal" class="cancel-btn">取消</button>
            <button @click="quickReturn" class="quick-btn">当前时间返回</button>
            <button @click="confirmReturn" :disabled="submitting" class="submit-btn">
              {{ submitting ? '登记中...' : '确认返回' }}
            </button>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { outingApi, customerApi } from '@/utils/customerApi'
import type {
  OutingRecord,
  OutingQueryParams,
  OutingStats,
  Customer
} from '@/types/customer'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const outingList = ref<OutingRecord[]>([])
const inServiceCustomers = ref<Customer[]>([])
const selectedRecords = ref<number[]>([])
const selectAll = ref<boolean>(false)
const showModal = ref<boolean>(false)
const showBatchApprovalModal = ref<boolean>(false)
const showReturnModal = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentRecord = ref<OutingRecord | null>(null)
const returnTime = ref<string>('')
const batchApprovalStatus = ref<string>('已批准')
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<OutingStats>({
  total: 0,
  approved: 0,
  pending: 0,
  rejected: 0,
  unreturned: 0
})

// 搜索表单
const searchForm = reactive<OutingQueryParams>({
  searchKeyword: '',
  approvalStatus: '',
  outingDate: '',
  page: 1,
  size: 10
})

// 外出申请表单
const outingForm = reactive<any>({
  customerId: null,
  outingDate: new Date().toISOString().split('T')[0],
  outingTime: '',
  returnTime: '',
  destination: '',
  companion: '',
  reason: '',
  notes: ''
})

// 计算属性
const selectAllState = computed(() => {
  if (outingList.value.length === 0) return false
  return selectedRecords.value.length === outingList.value.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const loadOutingList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await outingApi.getOutingList(params)

    if (response.code === 200) {
      outingList.value = response.data.records
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载外出记录失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await outingApi.getOutingStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadInServiceCustomers = async (): Promise<void> => {
  try {
    const response = await customerApi.getCustomerList({ status: 1, size: 100 })
    if (response.code === 200) {
      inServiceCustomers.value = response.data.customers
    }
  } catch (error) {
    console.error('加载在住客户失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadOutingList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    approvalStatus: '',
    outingDate: ''
  })
  handleSearch()
}

const handleSelectAll = (): void => {
  if (selectAll.value) {
    selectedRecords.value = outingList.value.map(record => record.id!).filter(id => id !== undefined)
  } else {
    selectedRecords.value = []
  }
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadOutingList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadOutingList()
}

const showOutingModal = (): void => {
  isEditing.value = false
  resetForm()
  loadInServiceCustomers()
  showModal.value = true
}

const editRecord = (record: OutingRecord): void => {
  isEditing.value = true
  Object.assign(outingForm, {
    id: record.id,
    customerId: record.customerId,
    outingDate: record.outingDate,
    outingTime: record.outingTime,
    returnTime: record.returnTime || '',
    destination: record.destination || '',
    companion: record.companion || '',
    reason: record.reason || '',
    notes: record.notes || ''
  })
  loadInServiceCustomers()
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(outingForm, {
    id: undefined,
    customerId: null,
    outingDate: new Date().toISOString().split('T')[0],
    outingTime: '',
    returnTime: '',
    destination: '',
    companion: '',
    reason: '',
    notes: ''
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
        ? await outingApi.updateOutingRecord(outingForm)
        : await outingApi.addOutingRecord(outingForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadOutingList()
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

const approveRecord = async (record: OutingRecord): Promise<void> => {
  if (!confirm(`确认批准客户 ${record.customerName} 的外出申请吗？`)) return

  try {
    const response = await outingApi.updateApprovalStatus(record.id!, '已批准')
    if (response.code === 200) {
      showMessage('审批成功', 'success')
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('审批操作失败', 'error')
  }
}

const rejectRecord = async (record: OutingRecord): Promise<void> => {
  if (!confirm(`确认拒绝客户 ${record.customerName} 的外出申请吗？`)) return

  try {
    const response = await outingApi.updateApprovalStatus(record.id!, '已拒绝')
    if (response.code === 200) {
      showMessage('审批成功', 'success')
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('审批操作失败', 'error')
  }
}

const markReturn = (record: OutingRecord): void => {
  currentRecord.value = record
  returnTime.value = getCurrentTime()
  showReturnModal.value = true
}

const closeReturnModal = (): void => {
  showReturnModal.value = false
  currentRecord.value = null
  returnTime.value = ''
}

const getCurrentTime = (): string => {
  const now = new Date()
  return now.toTimeString().slice(0, 5)
}

const quickReturn = async (): Promise<void> => {
  if (!currentRecord.value) return

  submitting.value = true
  try {
    const response = await outingApi.quickReturn(currentRecord.value.id!)
    if (response.code === 200) {
      showMessage('返回登记成功', 'success')
      closeReturnModal()
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('返回登记失败', 'error')
  } finally {
    submitting.value = false
  }
}

const confirmReturn = async (): Promise<void> => {
  if (!currentRecord.value || !returnTime.value) return

  submitting.value = true
  try {
    // 这里需要调用带时间参数的返回接口
    const response = await outingApi.quickReturn(currentRecord.value.id!) // 简化处理
    if (response.code === 200) {
      showMessage('返回登记成功', 'success')
      closeReturnModal()
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('返回登记失败', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteRecord = async (record: OutingRecord): Promise<void> => {
  if (!confirm(`确认删除客户 ${record.customerName} 的外出记录吗？`)) return

  try {
    const response = await outingApi.deleteOutingRecord(record.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchApprove = (): void => {
  if (selectedRecords.value.length === 0) return
  showBatchApprovalModal.value = true
}

const closeBatchApprovalModal = (): void => {
  showBatchApprovalModal.value = false
  batchApprovalStatus.value = '已批准'
}

const confirmBatchApproval = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = await outingApi.batchApprove(selectedRecords.value, batchApprovalStatus.value)
    if (response.code === 200) {
      showMessage('批量审批成功', 'success')
      closeBatchApprovalModal()
      selectedRecords.value = []
      selectAll.value = false
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量审批失败', 'error')
  } finally {
    submitting.value = false
  }
}

const batchDelete = async (): Promise<void> => {
  if (selectedRecords.value.length === 0) return

  if (!confirm(`确认删除选中的 ${selectedRecords.value.length} 条外出记录吗？`)) return

  try {
    const response = await outingApi.deleteOutingRecords(selectedRecords.value)
    if (response.code === 200) {
      showMessage('批量删除成功', 'success')
      selectedRecords.value = []
      selectAll.value = false
      loadOutingList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const getApprovalStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '待批准': 'pending',
    '已批准': 'approved',
    '已拒绝': 'rejected'
  }
  return statusMap[status] || 'default'
}

// 生命周期
onMounted(() => {
  loadOutingList()
  loadStats()
})

// 监听选中状态
import { watch } from 'vue'
watch(selectedRecords, () => {
  selectAll.value = selectAllState.value
}, { deep: true })
</script>

<style scoped>
.customer-outing {
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

.stat-card.approved {
  border-left-color: #27ae60;
}

.stat-card.pending {
  border-left-color: #f39c12;
}

.stat-card.unreturned {
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

.add-btn, .approve-btn, .delete-btn {
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

.approve-btn {
  background: #3498db;
  color: white;
}

.approve-btn:hover:not(:disabled) {
  background: #2980b9;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover:not(:disabled) {
  background: #c0392b;
}

.approve-btn:disabled,
.delete-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.outing-table {
  width: 100%;
  border-collapse: collapse;
}

.outing-table th,
.outing-table td {
  padding: 12px 8px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
  font-size: 13px;
}

.outing-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.outing-table tr:hover {
  background: #f8f9fa;
}

.customer-name {
  font-weight: 600;
  color: #3498db;
}

.return-time {
  color: #27ae60;
  font-weight: 500;
}

.destination, .reason {
  max-width: 120px;
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

.status-badge.pending {
  background: #fdeaa7;
  color: #f39c12;
}

.status-badge.approved {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.rejected {
  background: #fadbd8;
  color: #e74c3c;
}

.actions {
  display: flex;
  gap: 3px;
  flex-wrap: wrap;
}

.actions button {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 11px;
  transition: background-color 0.3s;
}

.actions .approve-btn {
  background: #27ae60;
  color: white;
}

.actions .approve-btn:hover {
  background: #219a52;
}

.actions .reject-btn {
  background: #e74c3c;
  color: white;
}

.actions .reject-btn:hover {
  background: #c0392b;
}

.actions .return-btn {
  background: #f39c12;
  color: white;
}

.actions .return-btn:hover {
  background: #e67e22;
}

.actions .edit-btn {
  background: #3498db;
  color: white;
}

.actions .edit-btn:hover {
  background: #2980b9;
}

.actions .delete-btn {
  background: #95a5a6;
  color: white;
}

.actions .delete-btn:hover {
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
.outing-form,
.batch-approval-form,
.return-form {
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

.batch-approval-form p,
.return-form p {
  margin-bottom: 15px;
  color: #2c3e50;
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
.submit-btn,
.quick-btn {
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

.quick-btn {
  background: #f39c12;
  color: white;
}

.quick-btn:hover {
  background: #e67e22;
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

  .outing-table {
    font-size: 11px;
  }

  .outing-table th,
  .outing-table td {
    padding: 6px 4px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>