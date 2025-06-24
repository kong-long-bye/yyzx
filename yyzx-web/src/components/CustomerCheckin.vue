<!-- src/components/CustomerCheckin.vue -->
<template>
  <div class="customer-checkin">
    <div class="page-header">
      <h1>客户入住登记</h1>
      <p>办理客户入住手续，分配床位和护理人员</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总客户数</div>
      </div>
      <div class="stat-card in-service">
        <div class="stat-number">{{ stats.inService }}</div>
        <div class="stat-label">在住客户</div>
      </div>
      <div class="stat-card pending">
        <div class="stat-number">{{ stats.pendingAudit }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card checked-out">
        <div class="stat-number">{{ stats.checkedOut }}</div>
        <div class="stat-label">已退住</div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索客户姓名、身份证号或手机号..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.auditStatus" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="待审核">待审核</option>
            <option value="已通过">已通过</option>
            <option value="已拒绝">已拒绝</option>
          </select>

          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部</option>
            <option value="1">在住</option>
            <option value="0">已退住</option>
          </select>

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showCheckinModal" class="add-btn">
          <span class="icon">+</span>
          入住登记
        </button>
      </div>
    </div>

    <!-- 客户列表 -->
    <div class="table-container">
      <table class="customer-table">
        <thead>
        <tr>
          <th>客户姓名</th>
          <th>身份证号</th>
          <th>性别</th>
          <th>年龄</th>
          <th>联系电话</th>
          <th>床位号</th>
          <th>护理等级</th>
          <th>入住日期</th>
          <th>审核状态</th>
          <th>状态</th>
          <th width="200">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="11" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="customerList.length === 0" class="empty-row">
          <td colspan="11" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">👥</span>
              <p>暂无客户数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="customer in customerList" :key="customer.id" class="customer-row">
          <td class="customer-name">{{ customer.name }}</td>
          <td>{{ customer.idCard }}</td>
          <td>{{ customer.gender }}</td>
          <td>{{ calculateAge(customer.birthDate) }}岁</td>
          <td>{{ customer.phone || '-' }}</td>
          <td>{{ customer.bedNumber || '-' }}</td>
          <td>{{ customer.nursingLevel || '-' }}</td>
          <td>{{ customer.admissionDate || '-' }}</td>
          <td>
              <span
                  class="status-badge"
                  :class="getAuditStatusClass(customer.auditStatus)"
              >
                {{ customer.auditStatus }}
              </span>
          </td>
          <td>
              <span
                  class="status-badge"
                  :class="getStatusClass(customer.status)"
              >
                {{ customer.status === 1 ? '在住' : '已退住' }}
              </span>
          </td>
          <td class="actions">
            <button
                v-if="customer.auditStatus === '待审核'"
                @click="approveCustomer(customer)"
                class="approve-btn"
            >
              审核通过
            </button>
            <button
                v-if="customer.auditStatus === '待审核'"
                @click="rejectCustomer(customer)"
                class="reject-btn"
            >
              审核拒绝
            </button>
            <button @click="viewCustomer(customer)" class="view-btn">查看</button>
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

    <!-- 入住登记弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>客户入住登记</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="checkin-form">
          <!-- 基本信息 -->
          <div class="form-section">
            <h4>基本信息</h4>
            <div class="form-row">
              <div class="form-group">
                <label>客户姓名 *</label>
                <input
                    type="text"
                    v-model="checkinForm.name"
                    placeholder="请输入客户姓名"
                    required
                >
              </div>
              <div class="form-group">
                <label>身份证号 *</label>
                <input
                    type="text"
                    v-model="checkinForm.idCard"
                    placeholder="请输入身份证号"
                    maxlength="18"
                    required
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>性别 *</label>
                <select v-model="checkinForm.gender" required>
                  <option value="">请选择性别</option>
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>出生日期 *</label>
                <input
                    type="date"
                    v-model="checkinForm.birthDate"
                    required
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>联系电话</label>
                <input
                    type="tel"
                    v-model="checkinForm.phone"
                    placeholder="请输入联系电话"
                >
              </div>
              <div class="form-group">
                <label>紧急联系人</label>
                <input
                    type="text"
                    v-model="checkinForm.emergencyContact"
                    placeholder="请输入紧急联系人"
                >
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>紧急联系电话</label>
                <input
                    type="tel"
                    v-model="checkinForm.emergencyPhone"
                    placeholder="请输入紧急联系电话"
                >
              </div>
              <div class="form-group">
                <label>入住日期</label>
                <input
                    type="date"
                    v-model="checkinForm.admissionDate"
                >
              </div>
            </div>

            <div class="form-group full-width">
              <label>家庭住址</label>
              <input
                  type="text"
                  v-model="checkinForm.address"
                  placeholder="请输入家庭住址"
              >
            </div>
          </div>

          <!-- 健康信息 -->
          <div class="form-section">
            <h4>健康信息</h4>
            <div class="form-group full-width">
              <label>病史</label>
              <textarea
                  v-model="checkinForm.medicalHistory"
                  placeholder="请输入既往病史"
                  rows="3"
              ></textarea>
            </div>
            <div class="form-group full-width">
              <label>过敏史</label>
              <textarea
                  v-model="checkinForm.allergies"
                  placeholder="请输入过敏史"
                  rows="2"
              ></textarea>
            </div>
          </div>

          <!-- 床位分配 -->
          <div class="form-section">
            <h4>床位分配</h4>
            <div class="form-group">
              <label>选择床位 *</label>
              <select v-model="checkinForm.bedId" required>
                <option value="">请选择床位</option>
                <option
                    v-for="bed in availableBeds"
                    :key="bed.id"
                    :value="bed.id"
                >
                  {{ bed.bed_number }} - {{ bed.room_number }}({{ bed.floor_number }}楼) - ¥{{ bed.daily_price }}/天
                </option>
              </select>
            </div>
          </div>

          <!-- 护理协议 -->
          <div class="form-section">
            <h4>护理协议</h4>
            <div class="form-row">
              <div class="form-group">
                <label>护理等级</label>
                <select v-model="checkinForm.agreement.levelName">
                  <option value="">请选择护理等级</option>
                  <option value="一级护理">一级护理 - ¥8000/月</option>
                  <option value="二级护理">二级护理 - ¥5000/月</option>
                  <option value="三级护理">三级护理 - ¥3000/月</option>
                </select>
              </div>
              <div class="form-group">
                <label>月费用</label>
                <input
                    type="number"
                    v-model="checkinForm.agreement.monthlyFee"
                    placeholder="月费用"
                    step="0.01"
                >
              </div>
            </div>
            <div class="form-group full-width">
              <label>服务内容</label>
              <textarea
                  v-model="checkinForm.agreement.serviceContent"
                  placeholder="请输入服务内容"
                  rows="3"
              ></textarea>
            </div>
          </div>

          <!-- 护理人员分配 -->
          <div class="form-section">
            <h4>护理人员分配</h4>
            <div class="caregiver-selection">
              <div
                  v-for="caregiver in caregivers"
                  :key="caregiver.id"
                  class="caregiver-item"
              >
                <label class="checkbox-label">
                  <input
                      type="checkbox"
                      :value="caregiver.id"
                      v-model="checkinForm.caregiverIds"
                  >
                  <span class="caregiver-info">
                    <strong>{{ caregiver.real_name }}</strong>
                    <span class="role">{{ caregiver.role_name }}</span>
                    <span class="workload">负责{{ caregiver.customer_count }}位客户</span>
                  </span>
                </label>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : '提交申请' }}
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
import { ref, reactive, onMounted } from 'vue'
import { customerApi } from '@/utils/customerApi'
import type {
  Customer,
  CustomerQueryParams,
  CustomerStats,
  BedOption,
  CaregiverOption
} from '@/types/customer'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const customerList = ref<Customer[]>([])
const availableBeds = ref<BedOption[]>([])
const caregivers = ref<CaregiverOption[]>([])
const showModal = ref<boolean>(false)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<CustomerStats>({
  total: 0,
  inService: 0,
  checkedOut: 0,
  pendingAudit: 0
})

// 搜索表单
const searchForm = reactive<CustomerQueryParams>({
  searchKeyword: '',
  auditStatus: '',
  status: undefined,
  page: 1,
  size: 10
})

// 入住登记表单
const checkinForm = reactive<any>({
  name: '',
  idCard: '',
  gender: '',
  birthDate: '',
  phone: '',
  emergencyContact: '',
  emergencyPhone: '',
  address: '',
  medicalHistory: '',
  allergies: '',
  bedId: null,
  admissionDate: new Date().toISOString().split('T')[0],
  agreement: {
    levelName: '',
    levelCode: '',
    monthlyFee: null,
    serviceContent: ''
  },
  caregiverIds: []
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const loadCustomerList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await customerApi.getCustomerList(params)

    if (response.code === 200) {
      customerList.value = response.data.customers
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
      await loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载客户列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await customerApi.getCustomerStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadAvailableBeds = async (): Promise<void> => {
  try {
    const response = await customerApi.getAvailableBeds()
    if (response.code === 200) {
      availableBeds.value = response.data
    }
  } catch (error) {
    console.error('加载可用床位失败:', error)
  }
}

const loadCaregivers = async (): Promise<void> => {
  try {
    const response = await customerApi.getCaregivers()
    if (response.code === 200) {
      caregivers.value = response.data
    }
  } catch (error) {
    console.error('加载护理人员失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadCustomerList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    auditStatus: '',
    status: undefined
  })
  handleSearch()
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadCustomerList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadCustomerList()
}

const showCheckinModal = (): void => {
  resetForm()
  loadAvailableBeds()
  loadCaregivers()
  showModal.value = true
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const resetForm = (): void => {
  Object.assign(checkinForm, {
    name: '',
    idCard: '',
    gender: '',
    birthDate: '',
    phone: '',
    emergencyContact: '',
    emergencyPhone: '',
    address: '',
    medicalHistory: '',
    allergies: '',
    bedId: null,
    admissionDate: new Date().toISOString().split('T')[0],
    agreement: {
      levelName: '',
      levelCode: '',
      monthlyFee: null,
      serviceContent: ''
    },
    caregiverIds: []
  })
}

const submitForm = async (): Promise<void> => {
  submitting.value = true
  try {
    // 设置护理等级编码和费用
    if (checkinForm.agreement.levelName) {
      const levelMap: Record<string, { code: string, fee: number }> = {
        '一级护理': { code: 'L001', fee: 8000 },
        '二级护理': { code: 'L002', fee: 5000 },
        '三级护理': { code: 'L003', fee: 3000 }
      }
      const levelInfo = levelMap[checkinForm.agreement.levelName]
      if (levelInfo) {
        checkinForm.agreement.levelCode = levelInfo.code
        checkinForm.agreement.monthlyFee = levelInfo.fee
      }
    }

    const response = await customerApi.checkinCustomer(checkinForm)

    if (response.code === 200) {
      showMessage('入住登记成功', 'success')
      closeModal()
      loadCustomerList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('入住登记失败', 'error')
  } finally {
    submitting.value = false
  }
}

const approveCustomer = async (customer: Customer): Promise<void> => {
  if (!confirm(`确认审核通过客户 ${customer.name} 的入住申请吗？`)) return

  try {
    const response = await customerApi.updateAuditStatus(customer.id!, '已通过')
    if (response.code === 200) {
      showMessage('审核通过成功', 'success')
      loadCustomerList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('审核操作失败', 'error')
  }
}

const rejectCustomer = async (customer: Customer): Promise<void> => {
  if (!confirm(`确认拒绝客户 ${customer.name} 的入住申请吗？`)) return

  try {
    const response = await customerApi.updateAuditStatus(customer.id!, '已拒绝')
    if (response.code === 200) {
      showMessage('审核拒绝成功', 'success')
      loadCustomerList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('审核操作失败', 'error')
  }
}

const viewCustomer = (customer: Customer): void => {
  // 这里可以显示客户详情弹窗
  alert(`查看客户详情: ${customer.name}`)
}

const calculateAge = (birthDate: string): number => {
  if (!birthDate) return 0
  const today = new Date()
  const birth = new Date(birthDate)
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age
}

const getAuditStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '待审核': 'pending',
    '已通过': 'approved',
    '已拒绝': 'rejected'
  }
  return statusMap[status] || 'default'
}

const getStatusClass = (status: number): string => {
  return status === 1 ? 'in-service' : 'checked-out'
}

// 生命周期
onMounted(() => {
  loadCustomerList()
  loadStats()
})
</script>

<style scoped>
.customer-checkin {
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

.stat-card.in-service {
  border-left-color: #27ae60;
}

.stat-card.pending {
  border-left-color: #f39c12;
}

.stat-card.checked-out {
  border-left-color: #95a5a6;
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

.add-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
  background: #27ae60;
  color: white;
}

.add-btn:hover {
  background: #219a52;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.customer-table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table th,
.customer-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.customer-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.customer-table tr:hover {
  background: #f8f9fa;
}

.customer-name {
  font-weight: 600;
  color: #3498db;
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

.status-badge.in-service {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.checked-out {
  background: #ecf0f1;
  color: #95a5a6;
}

.actions {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.actions button {
  padding: 5px 10px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.approve-btn {
  background: #27ae60;
  color: white;
}

.approve-btn:hover {
  background: #219a52;
}

.reject-btn {
  background: #e74c3c;
  color: white;
}

.reject-btn:hover {
  background: #c0392b;
}

.view-btn {
  background: #3498db;
  color: white;
}

.view-btn:hover {
  background: #2980b9;
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
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
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
.checkin-form {
  padding: 0 20px 20px;
}

.form-section {
  margin-bottom: 25px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ecf0f1;
}

.form-section:last-of-type {
  border-bottom: none;
}

.form-section h4 {
  margin-bottom: 15px;
  color: #2c3e50;
  font-size: 16px;
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

.caregiver-selection {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 10px;
}

.caregiver-item {
  padding: 10px;
  border: 1px solid #ecf0f1;
  border-radius: 4px;
  background: #f8f9fa;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  cursor: pointer;
}

.caregiver-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role {
  font-size: 12px;
  color: #7f8c8d;
}

.workload {
  font-size: 12px;
  color: #e67e22;
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

  .customer-table {
    font-size: 12px;
  }

  .customer-table th,
  .customer-table td {
    padding: 8px 6px;
  }

  .caregiver-selection {
    grid-template-columns: 1fr;
  }
}
</style>