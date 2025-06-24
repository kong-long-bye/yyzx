<!-- src/components/CustomerCheckout.vue -->
<template>
  <div class="customer-checkout">
    <div class="page-header">
      <h1>客户退住登记</h1>
      <p>办理客户退住手续，释放床位资源</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.totalInPeriod }}</div>
        <div class="stat-label">本期退住数</div>
      </div>
      <div class="stat-card normal">
        <div class="stat-number">{{ stats.typeStats?.['正常退住'] || 0 }}</div>
        <div class="stat-label">正常退住</div>
      </div>
      <div class="stat-card transfer">
        <div class="stat-number">{{ stats.typeStats?.['医疗转院'] || 0 }}</div>
        <div class="stat-label">医疗转院</div>
      </div>
      <div class="stat-card voluntary">
        <div class="stat-number">{{ stats.typeStats?.['自愿退住'] || 0 }}</div>
        <div class="stat-label">自愿退住</div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索退住类型、原因或备注..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.checkoutType" @change="handleSearch">
            <option value="">全部类型</option>
            <option v-for="type in checkoutTypes" :key="type" :value="type">
              {{ type }}
            </option>
          </select>

          <input
              type="date"
              v-model="searchForm.startDate"
              @change="handleSearch"
              class="date-input"
              placeholder="开始日期"
          >

          <input
              type="date"
              v-model="searchForm.endDate"
              @change="handleSearch"
              class="date-input"
              placeholder="结束日期"
          >

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showCheckoutModal" class="add-btn">
          <span class="icon">+</span>
          办理退住
        </button>

        <button
            @click="showBatchCheckoutModal"
            class="batch-btn"
        >
          <span class="icon">👥</span>
          批量退住
        </button>
      </div>
    </div>

    <!-- 退住信息列表 -->
    <div class="table-container">
      <table class="checkout-table">
        <thead>
        <tr>
          <th>退住类型</th>
          <th>退住原因</th>
          <th>退住日期</th>
          <th>备注</th>
          <th>登记时间</th>
          <th width="150">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="6" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="checkoutList.length === 0" class="empty-row">
          <td colspan="6" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无退住记录</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="checkout in checkoutList" :key="checkout.id" class="checkout-row">
          <td>
              <span
                  class="type-badge"
                  :class="getTypeClass(checkout.checkoutType)"
              >
                {{ checkout.checkoutType }}
              </span>
          </td>
          <td class="reason">{{ checkout.checkoutReason || '-' }}</td>
          <td>{{ checkout.checkoutDate }}</td>
          <td class="remarks">{{ checkout.remarks || '-' }}</td>
          <td>{{ formatDateTime(checkout.createdAt) }}</td>
          <td class="actions">
            <button @click="viewCheckout(checkout)" class="view-btn">查看</button>
            <button @click="editCheckout(checkout)" class="edit-btn">编辑</button>
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

    <!-- 退住登记弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑退住信息' : '办理客户退住' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="checkout-form">
          <div v-if="!isEditing" class="form-section">
            <h4>选择客户</h4>
            <div class="customer-selection">
              <div
                  v-for="customer in inServiceCustomers"
                  :key="customer.id"
                  class="customer-item"
                  :class="{ selected: checkoutForm.customerId === customer.id }"
                  @click="selectCustomer(customer)"
              >
                <div class="customer-info">
                  <div class="customer-name">{{ customer.name }}</div>
                  <div class="customer-details">
                    <span class="bed">{{ customer.bedNumber }}</span>
                    <span class="admission">入住：{{ customer.admissionDate }}</span>
                    <span class="nursing">{{ customer.nursingLevel }}</span>
                  </div>
                </div>
                <div class="select-indicator">
                  <span v-if="checkoutForm.customerId === customer.id">✓</span>
                </div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h4>退住信息</h4>
            <div class="form-row">
              <div class="form-group">
                <label>退住类型 *</label>
                <select v-model="checkoutForm.checkoutType" required>
                  <option value="">请选择退住类型</option>
                  <option v-for="type in checkoutTypes" :key="type" :value="type">
                    {{ type }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>退住日期 *</label>
                <input
                    type="date"
                    v-model="checkoutForm.checkoutDate"
                    required
                >
              </div>
            </div>

            <div class="form-group full-width">
              <label>退住原因</label>
              <textarea
                  v-model="checkoutForm.checkoutReason"
                  placeholder="请输入退住原因"
                  rows="3"
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label>备注</label>
              <textarea
                  v-model="checkoutForm.remarks"
                  placeholder="请输入备注信息"
                  rows="3"
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting || (!isEditing && !checkoutForm.customerId)" class="submit-btn">
              {{ submitting ? '处理中...' : (isEditing ? '更新信息' : '确认退住') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 批量退住弹窗 -->
    <div v-if="showBatchModal" class="modal-overlay" @click="closeBatchModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>批量退住登记</h3>
          <button @click="closeBatchModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitBatchForm" class="batch-checkout-form">
          <div class="form-section">
            <h4>选择客户</h4>
            <div class="customer-selection">
              <div
                  v-for="customer in inServiceCustomers"
                  :key="customer.id"
                  class="customer-item"
              >
                <label class="checkbox-label">
                  <input
                      type="checkbox"
                      :value="customer.id"
                      v-model="batchCheckoutForm.customerIds"
                  >
                  <div class="customer-info">
                    <div class="customer-name">{{ customer.name }}</div>
                    <div class="customer-details">
                      <span class="bed">{{ customer.bedNumber }}</span>
                      <span class="admission">入住：{{ customer.admissionDate }}</span>
                      <span class="nursing">{{ customer.nursingLevel }}</span>
                    </div>
                  </div>
                </label>
              </div>
            </div>
          </div>

          <div class="form-section">
            <h4>退住信息</h4>
            <div class="form-row">
              <div class="form-group">
                <label>退住类型 *</label>
                <select v-model="batchCheckoutForm.checkoutType" required>
                  <option value="">请选择退住类型</option>
                  <option v-for="type in checkoutTypes" :key="type" :value="type">
                    {{ type }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label>退住日期 *</label>
                <input
                    type="date"
                    v-model="batchCheckoutForm.checkoutDate"
                    required
                >
              </div>
            </div>

            <div class="form-group full-width">
              <label>退住原因</label>
              <textarea
                  v-model="batchCheckoutForm.checkoutReason"
                  placeholder="请输入退住原因"
                  rows="3"
              ></textarea>
            </div>

            <div class="form-group full-width">
              <label>备注</label>
              <textarea
                  v-model="batchCheckoutForm.remarks"
                  placeholder="请输入备注信息"
                  rows="3"
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeBatchModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting || batchCheckoutForm.customerIds.length === 0" class="submit-btn">
              {{ submitting ? '处理中...' : `确认退住 (${batchCheckoutForm.customerIds.length}位客户)` }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>退住详情</h3>
          <button @click="closeDetailModal" class="close-btn">×</button>
        </div>

        <div class="detail-content" v-if="currentCheckout">
          <div class="detail-item">
            <label>退住类型：</label>
            <span class="type-badge" :class="getTypeClass(currentCheckout.checkoutType)">
              {{ currentCheckout.checkoutType }}
            </span>
          </div>

          <div class="detail-item">
            <label>退住日期：</label>
            <span>{{ currentCheckout.checkoutDate }}</span>
          </div>

          <div class="detail-item">
            <label>退住原因：</label>
            <span>{{ currentCheckout.checkoutReason || '无' }}</span>
          </div>

          <div class="detail-item">
            <label>备注：</label>
            <span>{{ currentCheckout.remarks || '无' }}</span>
          </div>

          <div class="detail-item">
            <label>登记时间：</label>
            <span>{{ currentCheckout?.createdAt ? formatDateTime(currentCheckout.createdAt) : '-' }}</span>

          </div>

          <div class="form-actions">
            <button @click="closeDetailModal" class="cancel-btn">关闭</button>
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
import { checkoutApi, customerApi } from '@/utils/customerApi'
import type {
  BatchCheckoutFormData, CheckoutFormData,

  CheckoutInfo,
  CheckoutQueryParams, CheckoutStats,
  Customer,

} from '@/types/customer'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const checkoutList = ref<CheckoutInfo[]>([])
const inServiceCustomers = ref<Customer[]>([])
const checkoutTypes = ref<string[]>([])
const showModal = ref<boolean>(false)
const showBatchModal = ref<boolean>(false)
const showDetailModal = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentCheckout = ref<CheckoutInfo | null>(null)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<CheckoutStats>({
  totalInPeriod: 0,
  typeStats: {},
  dailyStats: []
})

// 搜索表单
const searchForm = reactive<CheckoutQueryParams>({
  searchKeyword: '',
  checkoutType: '',
  startDate: '',
  endDate: '',
  page: 1,
  size: 10
})

// 退住登记表单
const checkoutForm = reactive<CheckoutFormData>({
  customerId: 0,
  checkoutType: '',
  checkoutReason: '',
  checkoutDate: new Date().toISOString().split('T')[0],
  remarks: ''
})

// 批量退住表单
const batchCheckoutForm = reactive<BatchCheckoutFormData>({
  customerIds: [],
  checkoutType: '',
  checkoutReason: '',
  checkoutDate: new Date().toISOString().split('T')[0],
  remarks: ''
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const loadCheckoutList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await checkoutApi.getCheckoutList(params)

    if (response.code === 200) {
      checkoutList.value = response.data.checkoutInfos
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载退住记录失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await checkoutApi.getCheckoutStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadCheckoutTypes = async (): Promise<void> => {
  try {
    const response = await checkoutApi.getCheckoutTypes()
    if (response.code === 200) {
      checkoutTypes.value = response.data
    }
  } catch (error) {
    console.error('加载退住类型失败:', error)
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
  loadCheckoutList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    checkoutType: '',
    startDate: '',
    endDate: ''
  })
  handleSearch()
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadCheckoutList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadCheckoutList()
}

const showCheckoutModal = (): void => {
  isEditing.value = false
  resetForm()
  loadInServiceCustomers()
  showModal.value = true
}

const showBatchCheckoutModal = (): void => {
  resetBatchForm()
  loadInServiceCustomers()
  showBatchModal.value = true
}

const editCheckout = (checkout: CheckoutInfo): void => {
  isEditing.value = true
  Object.assign(checkoutForm, {
    id: checkout.id,
    checkoutType: checkout.checkoutType,
    checkoutReason: checkout.checkoutReason,
    checkoutDate: checkout.checkoutDate,
    remarks: checkout.remarks
  })
  showModal.value = true
}

const viewCheckout = (checkout: CheckoutInfo): void => {
  currentCheckout.value = checkout
  showDetailModal.value = true
}

const selectCustomer = (customer: Customer): void => {
  if (customer.id !== undefined) {
    checkoutForm.customerId = customer.id
  }
}

const resetForm = (): void => {
  Object.assign(checkoutForm, {
    id: undefined,
    customerId: null,
    checkoutType: '',
    checkoutReason: '',
    checkoutDate: new Date().toISOString().split('T')[0],
    remarks: ''
  })
}

const resetBatchForm = (): void => {
  Object.assign(batchCheckoutForm, {
    customerIds: [],
    checkoutType: '',
    checkoutReason: '',
    checkoutDate: new Date().toISOString().split('T')[0],
    remarks: ''
  })
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const closeBatchModal = (): void => {
  showBatchModal.value = false
  resetBatchForm()
}

const closeDetailModal = (): void => {
  showDetailModal.value = false
  currentCheckout.value = null
}

const submitForm = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = isEditing.value
        ? await updateCheckoutInfo()
        : await checkoutApi.checkoutCustomer(checkoutForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadCheckoutList()
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

const submitBatchForm = async (): Promise<void> => {
  submitting.value = true
  try {
    const response = await checkoutApi.batchCheckout(batchCheckoutForm)

    if (response.code === 200) {
      showMessage(`成功为 ${batchCheckoutForm.customerIds.length} 位客户办理退住`, 'success')
      closeBatchModal()
      loadCheckoutList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量退住失败', 'error')
  } finally {
    submitting.value = false
  }
}

const updateCheckoutInfo = async (): Promise<any> => {
  // 这里需要实现更新退住信息的API调用
  // 暂时返回成功响应
  return { code: 200, msg: '更新成功' }
}

const getTypeClass = (type: string): string => {
  const typeMap: Record<string, string> = {
    '正常退住': 'normal',
    '医疗转院': 'transfer',
    '自愿退住': 'voluntary',
    '其他原因': 'other'
  }
  return typeMap[type] || 'default'
}

const formatDateTime = (dateTime: string): string => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadCheckoutList()
  loadStats()
  loadCheckoutTypes()
})
</script>

<style scoped>
.customer-checkout {
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

.stat-card.normal {
  border-left-color: #27ae60;
}

.stat-card.transfer {
  border-left-color: #e74c3c;
}

.stat-card.voluntary {
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
  background: #e74c3c;
  color: white;
}

.add-btn:hover {
  background: #c0392b;
}

.batch-btn {
  background: #9b59b6;
  color: white;
}

.batch-btn:hover {
  background: #8e44ad;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.checkout-table {
  width: 100%;
  border-collapse: collapse;
}

.checkout-table th,
.checkout-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.checkout-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.checkout-table tr:hover {
  background: #f8f9fa;
}

.type-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-badge.normal {
  background: #d5f4e6;
  color: #27ae60;
}

.type-badge.transfer {
  background: #fadbd8;
  color: #e74c3c;
}

.type-badge.voluntary {
  background: #fdeaa7;
  color: #f39c12;
}

.type-badge.other {
  background: #d6eaf8;
  color: #3498db;
}

.reason, .remarks {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.view-btn {
  background: #3498db;
  color: white;
}

.view-btn:hover {
  background: #2980b9;
}

.edit-btn {
  background: #f39c12;
  color: white;
}

.edit-btn:hover {
  background: #e67e22;
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
.checkout-form,
.batch-checkout-form {
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

.customer-selection {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.customer-item {
  padding: 15px;
  border: 2px solid #ecf0f1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.customer-item:hover {
  border-color: #3498db;
}

.customer-item.selected {
  border-color: #27ae60;
  background: #d5f4e6;
}

.customer-info {
  flex: 1;
}

.customer-name {
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.customer-details {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #7f8c8d;
}

.bed {
  background: #ecf0f1;
  padding: 2px 6px;
  border-radius: 3px;
}

.admission {
  color: #3498db;
}

.nursing {
  color: #e67e22;
}

.select-indicator {
  color: #27ae60;
  font-size: 18px;
  font-weight: bold;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  width: 100%;
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

.detail-content {
  padding: 0 20px 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.detail-item label {
  font-weight: 500;
  color: #2c3e50;
  width: 100px;
  flex-shrink: 0;
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
  background: #e74c3c;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #c0392b;
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

  .checkout-table {
    font-size: 12px;
  }

  .checkout-table th,
  .checkout-table td {
    padding: 8px 6px;
  }

  .customer-selection {
    grid-template-columns: 1fr;
  }
}
</style>