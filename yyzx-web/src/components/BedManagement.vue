<template>
  <div class="bed-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>床位管理</h1>
      <p>管理养老院所有床位信息</p>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索床位号、房间号或客户姓名..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option v-for="option in bedStatusOptions"
                    :key="option.value"
                    :value="option.value">
              {{ option.label }}
            </option>
          </select>

          <select v-model="searchForm.floorNumber" @change="handleSearch">
            <option value="">全部楼层</option>
            <option v-for="floor in floorOptions"
                    :key="floor"
                    :value="floor">
              {{ floor }}楼
            </option>
          </select>

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showAddModal" class="add-btn">
          <span class="icon">+</span>
          添加床位
        </button>

        <button
            @click="batchDelete"
            :disabled="selectedBeds.length === 0"
            class="delete-btn"
        >
          <span class="icon">🗑️</span>
          批量删除 ({{ selectedBeds.length }})
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总床位数</div>
      </div>
      <div class="stat-card available">
        <div class="stat-number">{{ stats.available }}</div>
        <div class="stat-label">空闲床位</div>
      </div>
      <div class="stat-card occupied">
        <div class="stat-number">{{ stats.occupied }}</div>
        <div class="stat-label">已占用</div>
      </div>
      <div class="stat-card maintenance">
        <div class="stat-number">{{ stats.maintenance }}</div>
        <div class="stat-label">维修中</div>
      </div>
      <div class="stat-card reserved">
        <div class="stat-number">{{ stats.reserved }}</div>
        <div class="stat-label">预留</div>
      </div>
    </div>

    <!-- 床位列表表格 -->
    <div class="table-container">
      <table class="bed-table">
        <thead>
        <tr>
          <th width="50">
            <input
                type="checkbox"
                v-model="selectAll"
                @change="handleSelectAll"
            >
          </th>
          <th>床位编号</th>
          <th>房间号</th>
          <th>楼层</th>
          <th>床位类型</th>
          <th>状态</th>
          <th>日费用(元)</th>
          <th>入住客户</th>
          <th>描述</th>
          <th width="200">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="10" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="bedList.length === 0" class="empty-row">
          <td colspan="10" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">🛏️</span>
              <p>暂无床位数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="bed in bedList" :key="bed.id" class="bed-row">
          <td>
            <input
                type="checkbox"
                :value="bed.id"
                v-model="selectedBeds"
            >
          </td>
          <td class="bed-number">{{ bed.bedNumber }}</td>
          <td>{{ bed.roomNumber }}</td>
          <td>{{ bed.floorNumber }}楼</td>
          <td>{{ bed.bedType }}</td>
          <td>
              <span
                  class="status-badge"
                  :class="getStatusClass(bed.status)"
                  @click="showStatusModal(bed)"
              >
                {{ bed.status }}
              </span>
          </td>
          <td class="price">¥{{ bed.dailyPrice }}</td>
          <td class="customer-name">
            {{ bed.customerName || '-' }}
          </td>
          <td class="description">
            {{ bed.description || '-' }}
          </td>
          <td class="actions">
            <button @click="editBed(bed)" class="edit-btn">编辑</button>
            <button @click="deleteBed(bed)" class="delete-btn">删除</button>
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

    <!-- 添加/编辑床位弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑床位' : '添加床位' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="bed-form">
          <div class="form-row">
            <div class="form-group">
              <label>床位编号 *</label>
              <input
                  type="text"
                  v-model="bedForm.bedNumber"
                  placeholder="请输入床位编号"
                  required
              >
            </div>

            <div class="form-group">
              <label>所属房间 *</label>
              <select v-model="bedForm.roomId" required>
                <option value="">请选择房间</option>
                <option v-for="room in roomList"
                        :key="room.id"
                        :value="room.id">
                  {{ room.roomNumber }} - {{ room.floorNumber }}楼 ({{ room.roomType }})
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>床位类型</label>
              <select v-model="bedForm.bedType">
                <option v-for="type in bedTypeOptions"
                        :key="type.value"
                        :value="type.value">
                  {{ type.label }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>床位状态</label>
              <select v-model="bedForm.status">
                <option v-for="status in bedStatusOptions"
                        :key="status.value"
                        :value="status.value">
                  {{ status.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>日费用(元)</label>
              <input
                  type="number"
                  v-model="bedForm.dailyPrice"
                  placeholder="150.00"
                  step="0.01"
                  min="0"
              >
            </div>
          </div>

          <div class="form-group full-width">
            <label>描述</label>
            <textarea
                v-model="bedForm.description"
                placeholder="请输入床位描述"
                rows="3"
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
          <h3>修改床位状态</h3>
          <button @click="closeStatusModal" class="close-btn">×</button>
        </div>

        <div class="status-form">
          <p>床位：{{ currentBed?.bedNumber }}</p>
          <p>当前状态：<span class="current-status">{{ currentBed?.status }}</span></p>

          <div class="form-group">
            <label>新状态：</label>
            <select v-model="newStatus">
              <option v-for="status in bedStatusOptions"
                      :key="status.value"
                      :value="status.value">
                {{ status.label }}
              </option>
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

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { bedApi } from '@/utils/bedApi'
import type {
  Bed,
  Room,
  BedQueryParams,
  BedStats,
  BedForm
} from '@/types'
import { BedStatusOptions, BedTypeOptions } from '@/types/bed'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const bedList = ref<Bed[]>([])
const roomList = ref<Room[]>([])
const selectedBeds = ref<number[]>([])
const selectAll = ref<boolean>(false)
const showModal = ref<boolean>(false)
const showStatusChange = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentBed = ref<Bed | null>(null)
const newStatus = ref<string>('')
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<BedStats>({
  total: 0,
  occupied: 0,
  available: 0,
  maintenance: 0,
  reserved: 0
})

// 搜索表单
const searchForm = reactive<BedQueryParams>({
  searchKeyword: '',
  status: '',
  floorNumber: undefined,
  page: 1,
  size: 10
})

// 床位表单
const bedForm = reactive<BedForm>({
  bedNumber: '',
  roomId: null,
  bedType: '普通床',
  status: '空闲',
  dailyPrice: 150.00,
  description: ''
})

// 选项数据
const bedStatusOptions = BedStatusOptions
const bedTypeOptions = BedTypeOptions
const floorOptions = [1, 2, 3, 4, 5] // 可根据实际情况调整

// 计算属性
const selectAllState = computed(() => {
  if (bedList.value.length === 0) return false
  return selectedBeds.value.length === bedList.value.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const loadBedList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await bedApi.getBedList(params)

    if (response.code === 200) {
      bedList.value = response.data.beds
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载床位列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadRoomList = async (): Promise<void> => {
  try {
    const response = await bedApi.getAllRooms()
    if (response.code === 200) {
      roomList.value = response.data
    }
  } catch (error) {
    console.error('加载房间列表失败:', error)
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await bedApi.getBedStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadBedList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    status: '',
    floorNumber: undefined
  })
  handleSearch()
}

const handleSelectAll = (): void => {
  if (selectAll.value) {
    selectedBeds.value = bedList.value.map(bed => bed.id!).filter(id => id !== undefined)
  } else {
    selectedBeds.value = []
  }
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadBedList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadBedList()
}

const showAddModal = (): void => {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

const editBed = (bed: Bed): void => {
  isEditing.value = true
  Object.assign(bedForm, {
    id: bed.id,
    bedNumber: bed.bedNumber,
    roomId: bed.roomId,
    bedType: bed.bedType,
    status: bed.status,
    dailyPrice: bed.dailyPrice,
    description: bed.description || ''
  })
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(bedForm, {
    id: undefined,
    bedNumber: '',
    roomId: null,
    bedType: '普通床',
    status: '空闲',
    dailyPrice: 150.00,
    description: ''
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
        ? await bedApi.updateBed(bedForm)
        : await bedApi.addBed(bedForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadBedList()
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

const deleteBed = async (bed: Bed): Promise<void> => {
  if (!confirm(`确认删除床位 ${bed.bedNumber} 吗？`)) return

  try {
    const response = await bedApi.deleteBed(bed.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadBedList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchDelete = async (): Promise<void> => {
  if (selectedBeds.value.length === 0) return

  if (!confirm(`确认删除选中的 ${selectedBeds.value.length} 个床位吗？`)) return

  try {
    const response = await bedApi.deleteBeds(selectedBeds.value)
    if (response.code === 200) {
      showMessage('批量删除成功', 'success')
      selectedBeds.value = []
      selectAll.value = false
      loadBedList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const showStatusModal = (bed: Bed): void => {
  currentBed.value = bed
  newStatus.value = bed.status
  showStatusChange.value = true
}

const closeStatusModal = (): void => {
  showStatusChange.value = false
  currentBed.value = null
  newStatus.value = ''
}

const updateStatus = async (): Promise<void> => {
  if (!currentBed.value || newStatus.value === currentBed.value.status) {
    closeStatusModal()
    return
  }

  submitting.value = true
  try {
    const response = await bedApi.updateBedStatus(currentBed.value.id!, newStatus.value)
    if (response.code === 200) {
      showMessage('状态更新成功', 'success')
      closeStatusModal()
      loadBedList()
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

const getStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '空闲': 'available',
    '占用': 'occupied',
    '维修': 'maintenance',
    '预留': 'reserved'
  }
  return statusMap[status] || 'default'
}

// 生命周期
onMounted(() => {
  loadBedList()
  loadRoomList()
  loadStats()
})

// 监听选中状态
import { watch } from 'vue'
watch(selectedBeds, () => {
  selectAll.value = selectAllState.value
}, { deep: true })
</script>

<style scoped>
.bed-management {
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

.stat-card.available {
  border-left-color: #27ae60;
}

.stat-card.occupied {
  border-left-color: #e74c3c;
}

.stat-card.maintenance {
  border-left-color: #f39c12;
}

.stat-card.reserved {
  border-left-color: #9b59b6;
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

.bed-table {
  width: 100%;
  border-collapse: collapse;
}

.bed-table th,
.bed-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.bed-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.bed-table tr:hover {
  background: #f8f9fa;
}

.bed-number {
  font-weight: 600;
  color: #3498db;
}

.price {
  font-weight: 600;
  color: #27ae60;
}

.customer-name {
  color: #8e44ad;
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

.status-badge.available {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.occupied {
  background: #fadbd8;
  color: #e74c3c;
}

.status-badge.maintenance {
  background: #fdeaa7;
  color: #f39c12;
}

.status-badge.reserved {
  background: #d6eaf8;
  color: #3498db;
}

.actions {
  display: flex;
  gap: 5px;
}

.actions .edit-btn,
.actions .delete-btn {
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
.bed-form,
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

  .bed-table {
    font-size: 12px;
  }

  .bed-table th,
  .bed-table td {
    padding: 8px 6px;
  }
}
</style>