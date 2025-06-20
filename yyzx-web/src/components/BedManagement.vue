<template>
  <div class="bed-management">
    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <div class="search-area">
        <input
            type="text"
            v-model="searchParams.searchKeyword"
            placeholder="搜索床位号、房间号或客户姓名..."
            class="search-input"
            @keyup.enter="searchBeds"
        >
        <select v-model="searchParams.status" class="filter-select">
          <option value="">全部状态</option>
          <option value="空闲">空闲</option>
          <option value="占用">占用</option>
          <option value="维修">维修</option>
          <option value="预留">预留</option>
        </select>
        <select v-model="searchParams.floorNumber" class="filter-select">
          <option :value="undefined">全部楼层</option>
          <option value="1">1楼</option>
          <option value="2">2楼</option>
          <option value="3">3楼</option>
        </select>
        <button @click="searchBeds" class="search-btn">搜索</button>
        <button @click="resetSearch" class="reset-btn">重置</button>
      </div>

      <div class="action-area">
        <button @click="showAddModal = true" class="add-btn">新增床位</button>
        <button
            @click="batchDeleteBeds"
            :disabled="selectedBeds.length === 0"
            class="delete-btn"
        >
          批量删除 ({{ selectedBeds.length }})
        </button>
      </div>
    </div>

    <!-- 床位统计卡片 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-icon">🛏️</div>
        <div class="stat-info">
          <div class="stat-number">{{ bedStats.total }}</div>
          <div class="stat-label">总床位</div>
        </div>
      </div>
      <div class="stat-card occupied">
        <div class="stat-icon">🔴</div>
        <div class="stat-info">
          <div class="stat-number">{{ bedStats.occupied }}</div>
          <div class="stat-label">已占用</div>
        </div>
      </div>
      <div class="stat-card available">
        <div class="stat-icon">🟢</div>
        <div class="stat-info">
          <div class="stat-number">{{ bedStats.available }}</div>
          <div class="stat-label">空闲</div>
        </div>
      </div>
      <div class="stat-card maintenance">
        <div class="stat-icon">🔧</div>
        <div class="stat-info">
          <div class="stat-number">{{ bedStats.maintenance }}</div>
          <div class="stat-label">维修中</div>
        </div>
      </div>
      <div class="stat-card reserved">
        <div class="stat-icon">🔵</div>
        <div class="stat-info">
          <div class="stat-number">{{ bedStats.reserved }}</div>
          <div class="stat-label">预留</div>
        </div>
      </div>
    </div>

    <!-- 床位列表 -->
    <div class="bed-table-container">
      <table class="bed-table">
        <thead>
        <tr>
          <th>
            <input
                type="checkbox"
                :checked="isAllSelected"
                @change="toggleSelectAll"
            >
          </th>
          <th>床位编号</th>
          <th>房间信息</th>
          <th>床位类型</th>
          <th>状态</th>
          <th>日费用</th>
          <th>客户信息</th>
          <th>描述</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="bed in bedList.beds" :key="bed.id" class="bed-row">
          <td>
            <input
                type="checkbox"
                :value="bed.id"
                v-model="selectedBeds"
            >
          </td>
          <td class="bed-number">{{ bed.bedNumber }}</td>
          <td>
            <div class="room-info">
              <div>{{ bed.roomNumber }}</div>
              <small>{{ bed.floorNumber }}楼</small>
            </div>
          </td>
          <td>{{ bed.bedType }}</td>
          <td>
              <span :class="['status-badge', getStatusClass(bed.status)]">
                {{ bed.status }}
              </span>
          </td>
          <td class="price">¥{{ bed.dailyPrice }}</td>
          <td>
              <span v-if="bed.customerName" class="customer-name">
                {{ bed.customerName }}
              </span>
            <span v-else class="no-customer">-</span>
          </td>
          <td class="description">
            {{ bed.description || '-' }}
          </td>
          <td class="create-time">
            {{ formatDate(bed.createdAt) }}
          </td>
          <td class="actions">
            <button @click="editBed(bed)" class="edit-btn">编辑</button>
            <button @click="changeStatus(bed)" class="status-btn">状态</button>
            <button @click="deleteBed(bed.id!)" class="delete-btn">删除</button>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 无数据提示 -->
      <div v-if="bedList.beds.length === 0" class="no-data">
        <div class="no-data-icon">🛏️</div>
        <div class="no-data-text">暂无床位数据</div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="bedList.totalPages > 1">
      <button
          @click="changePage(bedList.page - 1)"
          :disabled="bedList.page <= 1"
          class="page-btn"
      >
        上一页
      </button>

      <span class="page-info">
        第 {{ bedList.page }} 页，共 {{ bedList.totalPages }} 页
        (总计 {{ bedList.total }} 条记录)
      </span>

      <button
          @click="changePage(bedList.page + 1)"
          :disabled="bedList.page >= bedList.totalPages"
          class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 新增/编辑床位弹框 -->
    <div v-if="showAddModal || showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ showAddModal ? '新增床位' : '编辑床位' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>床位编号 *</label>
            <input
                type="text"
                v-model="currentBed.bedNumber"
                placeholder="请输入床位编号"
                class="form-input"
            >
          </div>

          <div class="form-group">
            <label>所属房间 *</label>
            <select v-model="currentBed.roomId" class="form-select">
              <option value="">请选择房间</option>
              <option
                  v-for="room in rooms"
                  :key="room.id"
                  :value="room.id"
              >
                {{ room.roomNumber }} ({{ room.floorNumber }}楼 - {{ room.roomType }})
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>床位类型</label>
            <select v-model="currentBed.bedType" class="form-select">
              <option value="普通床">普通床</option>
              <option value="护理床">护理床</option>
              <option value="电动床">电动床</option>
            </select>
          </div>

          <div class="form-group">
            <label>床位状态</label>
            <select v-model="currentBed.status" class="form-select">
              <option value="空闲">空闲</option>
              <option value="占用">占用</option>
              <option value="维修">维修</option>
              <option value="预留">预留</option>
            </select>
          </div>

          <div class="form-group">
            <label>日费用(元)</label>
            <input
                type="number"
                v-model="currentBed.dailyPrice"
                placeholder="请输入日费用"
                min="0"
                step="0.01"
                class="form-input"
            >
          </div>

          <div class="form-group">
            <label>描述</label>
            <textarea
                v-model="currentBed.description"
                placeholder="请输入床位描述"
                class="form-textarea"
                rows="3"
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeModal" class="cancel-btn">取消</button>
          <button @click="saveBed" class="save-btn" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 状态修改弹框 -->
    <div v-if="showStatusModal" class="modal-overlay" @click="closeStatusModal">
      <div class="modal small" @click.stop>
        <div class="modal-header">
          <h3>修改床位状态</h3>
          <button @click="closeStatusModal" class="close-btn">×</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>床位：{{ statusBed?.bedNumber }}</label>
            <label>当前状态：{{ statusBed?.status }}</label>
          </div>

          <div class="form-group">
            <label>新状态 *</label>
            <select v-model="newStatus" class="form-select">
              <option value="空闲">空闲</option>
              <option value="占用">占用</option>
              <option value="维修">维修</option>
              <option value="预留">预留</option>
            </select>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeStatusModal" class="cancel-btn">取消</button>
          <button @click="saveStatus" class="save-btn">确认修改</button>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner">加载中...</div>
    </div>

    <!-- 消息提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { bedApi } from '@/utils/api'
import type {
  Bed,
  Room,
  BedSearchParams,
  BedListResponse,
  BedStats
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const saving = ref<boolean>(false)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 搜索参数
const searchParams = reactive<BedSearchParams>({
  searchKeyword: '',
  status: '',
  floorNumber: undefined,
  page: 1,
  size: 10
})

// 床位列表数据
const bedList = ref<BedListResponse>({
  beds: [],
  total: 0,
  page: 1,
  size: 10,
  totalPages: 0
})

// 床位统计
const bedStats = ref<BedStats>({
  total: 0,
  occupied: 0,
  available: 0,
  maintenance: 0,
  reserved: 0
})

// 房间列表
const rooms = ref<Room[]>([])

// 选中的床位
const selectedBeds = ref<number[]>([])

// 弹框相关
const showAddModal = ref<boolean>(false)
const showEditModal = ref<boolean>(false)
const showStatusModal = ref<boolean>(false)

// 当前操作的床位
const currentBed = ref<Bed>({
  bedNumber: '',
  roomId: 0,
  bedType: '普通床',
  status: '空闲',
  dailyPrice: 150,
  description: ''
})

// 状态修改相关
const statusBed = ref<Bed | null>(null)
const newStatus = ref<string>('')

// 计算属性
const isAllSelected = computed(() => {
  return bedList.value.beds.length > 0 &&
      selectedBeds.value.length === bedList.value.beds.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const getStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '空闲': 'available',
    '占用': 'occupied',
    '维修': 'maintenance',
    '预留': 'reserved'
  }
  return statusMap[status] || ''
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const loadBedList = async (): Promise<void> => {
  loading.value = true
  try {
    const response = await bedApi.getBedList(searchParams)
    if (response.code === 200) {
      bedList.value = response.data
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载床位列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadBedStats = async (): Promise<void> => {
  try {
    const response = await bedApi.getBedStats()
    if (response.code === 200) {
      bedStats.value = response.data
    }
  } catch (error) {
    console.error('加载床位统计失败:', error)
  }
}

const loadRooms = async (): Promise<void> => {
  try {
    const response = await bedApi.getAllRooms()
    if (response.code === 200) {
      rooms.value = response.data
    }
  } catch (error) {
    console.error('加载房间列表失败:', error)
  }
}

const searchBeds = (): void => {
  searchParams.page = 1
  loadBedList()
}

const resetSearch = (): void => {
  Object.assign(searchParams, {
    searchKeyword: '',
    status: '',
    floorNumber: undefined,
    page: 1,
    size: 10
  })
  loadBedList()
}

const changePage = (page: number): void => {
  searchParams.page = page
  loadBedList()
}

const toggleSelectAll = (): void => {
  if (isAllSelected.value) {
    selectedBeds.value = []
  } else {
    selectedBeds.value = bedList.value.beds.map(bed => bed.id!).filter(id => id)
  }
}

const editBed = (bed: Bed): void => {
  currentBed.value = { ...bed }
  showEditModal.value = true
}

const deleteBed = async (id: number): Promise<void> => {
  if (!confirm('确认删除这个床位吗？')) return

  try {
    const response = await bedApi.deleteBed(id)
    if (response.code === 200) {
      showMessage('删除成功')
      loadBedList()
      loadBedStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchDeleteBeds = async (): Promise<void> => {
  if (selectedBeds.value.length === 0) return
  if (!confirm(`确认删除选中的 ${selectedBeds.value.length} 个床位吗？`)) return

  try {
    const response = await bedApi.deleteBeds(selectedBeds.value)
    if (response.code === 200) {
      showMessage('批量删除成功')
      selectedBeds.value = []
      loadBedList()
      loadBedStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const changeStatus = (bed: Bed): void => {
  statusBed.value = bed
  newStatus.value = bed.status
  showStatusModal.value = true
}

const saveBed = async (): Promise<void> => {
  if (!currentBed.value.bedNumber.trim()) {
    showMessage('请输入床位编号', 'error')
    return
  }
  if (!currentBed.value.roomId) {
    showMessage('请选择房间', 'error')
    return
  }

  saving.value = true
  try {
    let response
    if (showAddModal.value) {
      response = await bedApi.addBed(currentBed.value)
    } else {
      response = await bedApi.updateBed(currentBed.value)
    }

    if (response.code === 200) {
      showMessage(showAddModal.value ? '添加成功' : '更新成功')
      closeModal()
      loadBedList()
      loadBedStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('保存失败', 'error')
  } finally {
    saving.value = false
  }
}

const saveStatus = async (): Promise<void> => {
  if (!statusBed.value || !newStatus.value) return

  try {
    const response = await bedApi.updateBedStatus(statusBed.value.id!, newStatus.value)
    if (response.code === 200) {
      showMessage('状态修改成功')
      closeStatusModal()
      loadBedList()
      loadBedStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('状态修改失败', 'error')
  }
}

const closeModal = (): void => {
  showAddModal.value = false
  showEditModal.value = false
  currentBed.value = {
    bedNumber: '',
    roomId: 0,
    bedType: '普通床',
    status: '空闲',
    dailyPrice: 150,
    description: ''
  }
}

const closeStatusModal = (): void => {
  showStatusModal.value = false
  statusBed.value = null
  newStatus.value = ''
}

// 生命周期
onMounted(() => {
  loadBedList()
  loadBedStats()
  loadRooms()
})
</script>

<style scoped>
.bed-management {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 工具栏样式 */
.toolbar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-area {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 280px;
  font-size: 14px;
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.search-btn {
  background: #3498db;
  color: white;
}

.reset-btn {
  background: #95a5a6;
  color: white;
}

.action-area {
  display: flex;
  gap: 10px;
}

.add-btn {
  background: #27ae60;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.delete-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.delete-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 统计卡片 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border-left: 4px solid #3498db;
}

.stat-card.occupied {
  border-left-color: #e74c3c;
}

.stat-card.available {
  border-left-color: #27ae60;
}

.stat-card.maintenance {
  border-left-color: #f39c12;
}

.stat-card.reserved {
  border-left-color: #3498db;
}

.stat-icon {
  font-size: 24px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

/* 表格样式 */
.bed-table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.bed-table {
  width: 100%;
  border-collapse: collapse;
}

.bed-table th {
  background: #34495e;
  color: white;
  padding: 12px;
  text-align: left;
  font-weight: 500;
}

.bed-table td {
  padding: 12px;
  border-bottom: 1px solid #ecf0f1;
}

.bed-row:hover {
  background: #f8f9fa;
}

.bed-number {
  font-weight: bold;
  color: #2c3e50;
}

.room-info div {
  font-weight: bold;
}

.room-info small {
  color: #7f8c8d;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
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

.price {
  font-weight: bold;
  color: #e67e22;
}

.customer-name {
  color: #2c3e50;
  font-weight: 500;
}

.no-customer {
  color: #95a5a6;
}

.description {
  max-width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.create-time {
  color: #7f8c8d;
  font-size: 12px;
}

.actions {
  display: flex;
  gap: 5px;
}

.edit-btn, .status-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.edit-btn {
  background: #3498db;
  color: white;
}

.status-btn {
  background: #f39c12;
  color: white;
}

.actions .delete-btn {
  padding: 4px 8px;
  font-size: 12px;
}

/* 无数据提示 */
.no-data {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.no-data-text {
  font-size: 16px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  background: #f8f9fa;
  color: #6c757d;
  cursor: not-allowed;
}

.page-btn:not(:disabled):hover {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-info {
  color: #7f8c8d;
  font-size: 14px;
}

/* 弹框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal.small {
  max-width: 400px;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #2c3e50;
  font-weight: 500;
}

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  resize: vertical;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #ecf0f1;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn, .save-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn {
  background: #95a5a6;
  color: white;
}

.save-btn {
  background: #27ae60;
  color: white;
}

.save-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

/* 加载和消息样式 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  padding: 20px 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  font-size: 16px;
  color: #3498db;
}

.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  font-size: 14px;
  z-index: 1001;
  animation: fadeIn 0.3s ease;
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

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-area {
    justify-content: center;
  }

  .search-input {
    width: 100%;
  }

  .stats-section {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .bed-table-container {
    overflow-x: auto;
  }

  .bed-table {
    min-width: 800px;
  }
}
</style>