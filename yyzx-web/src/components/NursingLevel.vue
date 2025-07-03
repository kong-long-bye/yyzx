<template>
  <div class="nursing-level">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>护理级别管理</h1>
      <p>管理客户护理协议和等级设置</p>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索客户姓名或护理等级..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.levelStatus" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="生效">生效</option>
            <option value="暂停">暂停</option>
            <option value="终止">终止</option>
          </select>

          <select v-model="searchForm.levelCode" @change="handleSearch">
            <option value="">全部等级</option>
            <option v-for="level in nursingLevels"
                    :key="level.level_code"
                    :value="level.level_code">
              {{ level.level_name }}
            </option>
          </select>

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showAddModal" class="add-btn">
          <span class="icon">+</span>
          新建协议
        </button>

        <button @click="checkExpiring" class="warning-btn">
          <span class="icon">⚠️</span>
          即将到期
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card active">
        <div class="stat-number">{{ stats.totalActive }}</div>
        <div class="stat-label">生效协议</div>
      </div>
      <div class="stat-card suspended">
        <div class="stat-number">{{ stats.totalSuspended }}</div>
        <div class="stat-label">暂停协议</div>
      </div>
      <div class="stat-card terminated">
        <div class="stat-number">{{ stats.totalTerminated }}</div>
        <div class="stat-label">终止协议</div>
      </div>
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总协议数</div>
      </div>
    </div>

    <!-- 护理协议列表表格 -->
    <div class="table-container">
      <table class="agreement-table">
        <thead>
        <tr>
          <th>客户姓名</th>
          <th>护理等级</th>
          <th>月费用(元)</th>
          <th>协议状态</th>
          <th>开始日期</th>
          <th>结束日期</th>
          <th>服务内容</th>
          <th width="250">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="8" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="agreementList.length === 0" class="empty-row">
          <td colspan="8" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无护理协议数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="agreement in agreementList" :key="agreement.id" class="agreement-row">
          <td class="customer-name">{{ agreement.customerName }}</td>
          <td class="level-name">{{ agreement.levelName }}</td>
          <td class="fee">¥{{ agreement.monthlyFee?.toFixed(2) }}</td>
          <td>
            <span class="status-badge" :class="getStatusClass(agreement.levelStatus!)">
              {{ agreement.levelStatus }}
            </span>
          </td>
          <td>{{ agreement.startDate }}</td>
          <td>{{ agreement.endDate || '长期' }}</td>
          <td class="service-content">
            <span :title="agreement.serviceContent">
              {{ truncateText(agreement.serviceContent, 20) }}
            </span>
          </td>
          <td class="actions">
            <button @click="editAgreement(agreement)" class="edit-btn">编辑</button>
            <button @click="showStatusModal(agreement)" class="status-btn">状态</button>
            <button @click="showUpgradeModal(agreement)" class="upgrade-btn">调级</button>
            <button @click="renewAgreement(agreement)" class="renew-btn">续签</button>
            <button @click="deleteAgreement(agreement)" class="delete-btn">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalPages > 1">
      <button @click="goToPage(currentPage - 1)" :disabled="currentPage <= 1" class="page-btn">
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页，总计 {{ totalCount }} 条
      </span>
      <button @click="goToPage(currentPage + 1)" :disabled="currentPage >= totalPages" class="page-btn">
        下一页
      </button>
      <select v-model="pageSize" @change="handlePageSizeChange" class="page-size-select">
        <option value="10">10条/页</option>
        <option value="20">20条/页</option>
        <option value="50">50条/页</option>
      </select>
    </div>

    <!-- 添加/编辑协议弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑护理协议' : '新建护理协议' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="agreement-form">
          <div class="form-row">
            <div class="form-group">
              <label>客户 *</label>
              <select v-model="agreementForm.customerId" required :disabled="isEditing">
                <option value="">请选择客户</option>
                <option v-for="customer in customerList"
                        :key="customer.id"
                        :value="customer.id">
                  {{ customer.name }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>护理等级 *</label>
              <select v-model="agreementForm.levelCode" @change="onLevelChange" required>
                <option value="">请选择护理等级</option>
                <option v-for="level in nursingLevels"
                        :key="level.level_code"
                        :value="level.level_code">
                  {{ level.level_name }} - ¥{{ level.monthly_fee }}/月
                </option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>月费用(元) *</label>
              <input type="number" v-model="agreementForm.monthlyFee"
                     placeholder="5000.00" step="0.01" min="0" required>
            </div>

            <div class="form-group">
              <label>协议状态</label>
              <select v-model="agreementForm.levelStatus">
                <option value="生效">生效</option>
                <option value="暂停">暂停</option>
                <option value="终止">终止</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>开始日期 *</label>
              <input type="date" v-model="agreementForm.startDate" required>
            </div>

            <div class="form-group">
              <label>结束日期</label>
              <input type="date" v-model="agreementForm.endDate">
              <small>留空表示长期协议</small>
            </div>
          </div>

          <div class="form-group full-width">
            <label>服务内容 *</label>
            <textarea v-model="agreementForm.serviceContent"
                      placeholder="请输入详细的服务内容"
                      rows="4" required></textarea>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : (isEditing ? '更新' : '创建') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 状态修改弹窗 -->
    <div v-if="showStatusChange" class="modal-overlay" @click="closeStatusModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>修改协议状态</h3>
          <button @click="closeStatusModal" class="close-btn">×</button>
        </div>

        <div class="status-form">
          <p>客户：{{ currentAgreement?.customerName }}</p>
          <p>当前状态：<span class="current-status">{{ currentAgreement?.levelStatus }}</span></p>

          <div class="form-group">
            <label>新状态：</label>
            <select v-model="newStatus">
              <option value="生效">生效</option>
              <option value="暂停">暂停</option>
              <option value="终止">终止</option>
            </select>
          </div>

          <div class="form-group" v-if="newStatus === '暂停' || newStatus === '终止'">
            <label>原因：</label>
            <textarea v-model="statusReason" placeholder="请输入原因" rows="3"></textarea>
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

    <!-- 等级调整弹窗 -->
    <div v-if="showUpgradeChange" class="modal-overlay" @click="closeUpgradeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>护理等级调整</h3>
          <button @click="closeUpgradeModal" class="close-btn">×</button>
        </div>

        <div class="upgrade-form">
          <p>客户：{{ currentAgreement?.customerName }}</p>
          <p>当前等级：<span class="current-level">{{ currentAgreement?.levelName }}</span></p>

          <div class="form-group">
            <label>新护理等级：</label>
            <select v-model="upgradeForm.newLevelCode" @change="onUpgradeLevelChange">
              <option value="">请选择新等级</option>
              <option v-for="level in nursingLevels"
                      :key="level.level_code"
                      :value="level.level_code">
                {{ level.level_name }} - ¥{{ level.monthly_fee }}/月
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>新月费用(元)：</label>
            <input type="number" v-model="upgradeForm.newMonthlyFee"
                   step="0.01" min="0" readonly>
          </div>

          <div class="form-group">
            <label>调整原因：</label>
            <textarea v-model="upgradeForm.reason"
                      placeholder="请输入调整原因"
                      rows="3" required></textarea>
          </div>

          <div class="form-actions">
            <button @click="closeUpgradeModal" class="cancel-btn">取消</button>
            <button @click="submitUpgrade" :disabled="submitting || !upgradeForm.newLevelCode" class="submit-btn">
              {{ submitting ? '调整中...' : '确认调整' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 即将到期协议弹窗 -->
    <div v-if="showExpiringModal" class="modal-overlay" @click="closeExpiringModal">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>即将到期的协议 ({{ expiringDays }}天内)</h3>
          <button @click="closeExpiringModal" class="close-btn">×</button>
        </div>

        <div class="expiring-list">
          <div v-if="expiringAgreements.length === 0" class="empty-state">
            <p>暂无即将到期的协议</p>
          </div>
          <div v-else>
            <table class="expiring-table">
              <thead>
              <tr>
                <th>客户姓名</th>
                <th>护理等级</th>
                <th>到期日期</th>
                <th>剩余天数</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="agreement in expiringAgreements" :key="agreement.id">
                <td>{{ agreement.customerName }}</td>
                <td>{{ agreement.levelName }}</td>
                <td>{{ agreement.endDate }}</td>
                <td class="days-left">{{ calculateDaysLeft(agreement.endDate!) }}天</td>
                <td>
                  <button @click="renewAgreement(agreement)" class="renew-btn">续签</button>
                </td>
              </tr>
              </tbody>
            </table>
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
import { ref, reactive, onMounted } from 'vue'
import { nursingLevelApi } from '@/utils/nursingLevelApi.ts'
import type {
  NursingAgreement,
  NursingLevel,
  Customer,
  AgreementQueryParams,
  AgreementStats,
  AgreementForm,
  UpgradeForm
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const agreementList = ref<NursingAgreement[]>([])
const nursingLevels = ref<NursingLevel[]>([])
const customerList = ref<Customer[]>([])
const expiringAgreements = ref<NursingAgreement[]>([])

const showModal = ref<boolean>(false)
const showStatusChange = ref<boolean>(false)
const showUpgradeChange = ref<boolean>(false)
const showExpiringModal = ref<boolean>(false)
const isEditing = ref<boolean>(false)

const currentAgreement = ref<NursingAgreement | null>(null)
const newStatus = ref<string>('')
const statusReason = ref<string>('')
const expiringDays = ref<number>(30)

const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<AgreementStats>({
  totalActive: 0,
  totalSuspended: 0,
  totalTerminated: 0,
  total: 0,
  levelStats: {}
})

// 搜索表单
const searchForm = reactive<AgreementQueryParams>({
  searchKeyword: '',
  levelStatus: '',
  levelCode: '',
  page: 1,
  size: 10
})

// 协议表单
const agreementForm = reactive<AgreementForm>({
  customerId: null,
  levelName: '',
  levelCode: '',
  levelStatus: '生效',
  monthlyFee: 0,
  serviceContent: '',
  startDate: '',
  endDate: ''
})

// 升级表单
const upgradeForm = reactive<UpgradeForm>({
  customerId: null,
  newLevelCode: '',
  newLevelName: '',
  newMonthlyFee: 0,
  serviceContent: '',
  reason: ''
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const loadAgreementList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await nursingLevelApi.getAgreementList(params)
    if (response.code === 200) {
      agreementList.value = response.data.agreements
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载协议列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadNursingLevels = async (): Promise<void> => {
  try {
    const response = await nursingLevelApi.getNursingLevels()
    if (response.code === 200) {
      nursingLevels.value = response.data
    }
  } catch (error) {
    console.error('加载护理等级失败:', error)
  }
}

const loadCustomerList = async (): Promise<void> => {
  try {
    const response = await nursingLevelApi.getCustomerList()
    if (response.code === 200) {
      customerList.value = response.data
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await nursingLevelApi.getAgreementStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadAgreementList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    levelStatus: '',
    levelCode: ''
  })
  handleSearch()
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadAgreementList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadAgreementList()
}

const showAddModal = (): void => {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

const editAgreement = (agreement: NursingAgreement): void => {
  isEditing.value = true
  Object.assign(agreementForm, {
    id: agreement.id,
    customerId: agreement.customerId,
    levelName: agreement.levelName,
    levelCode: agreement.levelCode,
    levelStatus: agreement.levelStatus,
    monthlyFee: agreement.monthlyFee,
    serviceContent: agreement.serviceContent,
    startDate: agreement.startDate,
    endDate: agreement.endDate || ''
  })
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(agreementForm, {
    id: undefined,
    customerId: null,
    levelName: '',
    levelCode: '',
    levelStatus: '生效',
    monthlyFee: 0,
    serviceContent: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: ''
  })
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const onLevelChange = (): void => {
  const selectedLevel = nursingLevels.value.find(l => l.level_code === agreementForm.levelCode)
  if (selectedLevel) {
    agreementForm.levelName = selectedLevel.level_name
    agreementForm.monthlyFee = selectedLevel.monthly_fee
    agreementForm.serviceContent = selectedLevel.service_content
  }
}

const submitForm = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = isEditing.value
        ? await nursingLevelApi.updateAgreement(agreementForm)
        : await nursingLevelApi.createAgreement(agreementForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadAgreementList()
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

const deleteAgreement = async (agreement: NursingAgreement): Promise<void> => {
  if (!confirm(`确认删除客户 ${agreement.customerName} 的护理协议吗？`)) return

  try {
    const response = await nursingLevelApi.deleteAgreement(agreement.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadAgreementList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const showStatusModal = (agreement: NursingAgreement): void => {
  currentAgreement.value = agreement
  newStatus.value = agreement.levelStatus!
  statusReason.value = ''
  showStatusChange.value = true
}

const closeStatusModal = (): void => {
  showStatusChange.value = false
  currentAgreement.value = null
  newStatus.value = ''
  statusReason.value = ''
}

const updateStatus = async (): Promise<void> => {
  if (!currentAgreement.value) return

  submitting.value = true
  try {
    const response = await nursingLevelApi.updateAgreementStatus(
        currentAgreement.value.id!,
        newStatus.value
    )
    if (response.code === 200) {
      showMessage('状态更新成功', 'success')
      closeStatusModal()
      loadAgreementList()
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

const showUpgradeModal = (agreement: NursingAgreement): void => {
  currentAgreement.value = agreement
  Object.assign(upgradeForm, {
    customerId: agreement.customerId,
    newLevelCode: '',
    newLevelName: '',
    newMonthlyFee: 0,
    serviceContent: '',
    reason: ''
  })
  showUpgradeChange.value = true
}

const closeUpgradeModal = (): void => {
  showUpgradeChange.value = false
  currentAgreement.value = null
  Object.assign(upgradeForm, {
    customerId: null,
    newLevelCode: '',
    newLevelName: '',
    newMonthlyFee: 0,
    serviceContent: '',
    reason: ''
  })
}

const onUpgradeLevelChange = (): void => {
  const selectedLevel = nursingLevels.value.find(l => l.level_code === upgradeForm.newLevelCode)
  if (selectedLevel) {
    upgradeForm.newLevelName = selectedLevel.level_name
    upgradeForm.newMonthlyFee = selectedLevel.monthly_fee
    upgradeForm.serviceContent = selectedLevel.service_content
  }
}

const submitUpgrade = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = await nursingLevelApi.upgradeLevel(upgradeForm)
    if (response.code === 200) {
      showMessage('等级调整成功', 'success')
      closeUpgradeModal()
      loadAgreementList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('等级调整失败', 'error')
  } finally {
    submitting.value = false
  }
}

const renewAgreement = async (agreement: NursingAgreement): Promise<void> => {
  const newEndDate = prompt('请输入新的结束日期 (YYYY-MM-DD)，留空表示长期协议:')
  if (newEndDate === null) return

  try {
    const response = await nursingLevelApi.renewAgreement({
      customerId: agreement.customerId!,
      newEndDate: newEndDate || null,
      newMonthlyFee: agreement.monthlyFee!
    })
    if (response.code === 200) {
      showMessage('续签成功', 'success')
      loadAgreementList()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('续签失败', 'error')
  }
}

const checkExpiring = async (): Promise<void> => {
  try {
    const response = await nursingLevelApi.getExpiringAgreements(expiringDays.value)
    if (response.code === 200) {
      expiringAgreements.value = response.data
      showExpiringModal.value = true
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('查询失败', 'error')
  }
}

const closeExpiringModal = (): void => {
  showExpiringModal.value = false
  expiringAgreements.value = []
}

const calculateDaysLeft = (endDate: string): number => {
  const end = new Date(endDate)
  const now = new Date()
  const diffTime = end.getTime() - now.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

const getStatusClass = (status: string): string => {
  const statusMap: Record<string, string> = {
    '生效': 'active',
    '暂停': 'suspended',
    '终止': 'terminated'
  }
  return statusMap[status] || 'default'
}

const truncateText = (text: string | undefined, length: number): string => {
  if (!text) return '-'
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 生命周期
onMounted(() => {
  loadAgreementList()
  loadNursingLevels()
  loadCustomerList()
  loadStats()
})
</script>

<style scoped>
.nursing-level {
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

.add-btn, .warning-btn {
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

.warning-btn {
  background: #f39c12;
  color: white;
}

.warning-btn:hover {
  background: #e67e22;
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

.stat-card.active {
  border-left-color: #27ae60;
}

.stat-card.suspended {
  border-left-color: #f39c12;
}

.stat-card.terminated {
  border-left-color: #e74c3c;
}

.stat-card.total {
  border-left-color: #3498db;
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

.agreement-table, .expiring-table {
  width: 100%;
  border-collapse: collapse;
}

.agreement-table th,
.agreement-table td,
.expiring-table th,
.expiring-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.agreement-table th,
.expiring-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.agreement-table tr:hover,
.expiring-table tr:hover {
  background: #f8f9fa;
}

.customer-name {
  font-weight: 600;
  color: #8e44ad;
}

.level-name {
  font-weight: 600;
  color: #2980b9;
}

.fee {
  font-weight: 600;
  color: #27ae60;
}

.service-content {
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

.status-badge.active {
  background: #d5f4e6;
  color: #27ae60;
}

.status-badge.suspended {
  background: #fdeaa7;
  color: #f39c12;
}

.status-badge.terminated {
  background: #fadbd8;
  color: #e74c3c;
}

.actions {
  display: flex;
  gap: 5px;
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

.edit-btn {
  background: #3498db;
  color: white;
}

.edit-btn:hover {
  background: #2980b9;
}

.status-btn {
  background: #f39c12;
  color: white;
}

.status-btn:hover {
  background: #e67e22;
}

.upgrade-btn {
  background: #9b59b6;
  color: white;
}

.upgrade-btn:hover {
  background: #8e44ad;
}

.renew-btn {
  background: #1abc9c;
  color: white;
}

.renew-btn:hover {
  background: #16a085;
}

.delete-btn {
  background: #e74c3c;
  color: white;
}

.delete-btn:hover {
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

.days-left {
  font-weight: 600;
  color: #e74c3c;
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

.modal-content.large {
  max-width: 800px;
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
.agreement-form,
.status-form,
.upgrade-form,
.expiring-list {
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
  margin-top: 4px;
  font-size: 12px;
  color: #7f8c8d;
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

.current-status,
.current-level {
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

  .agreement-table {
    font-size: 12px;
  }

  .agreement-table th,
  .agreement-table td {
    padding: 8px 6px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>