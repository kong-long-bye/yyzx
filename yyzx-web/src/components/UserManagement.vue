<template>
  <div class="user-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>用户管理</h1>
      <p>管理系统用户信息和权限</p>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <div class="toolbar">
      <div class="search-filters">
        <div class="search-group">
          <input
              type="text"
              v-model="searchForm.searchKeyword"
              placeholder="搜索用户名、真实姓名或手机号..."
              class="search-input"
              @keyup.enter="handleSearch"
          >
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="filter-group">
          <select v-model="searchForm.roleId" @change="handleSearch">
            <option value="">全部角色</option>
            <option v-for="role in roleOptions"
                    :key="role.id"
                    :value="role.id">
              {{ role.roleName }}
            </option>
          </select>

          <select v-model="searchForm.status" @change="handleSearch">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>

          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="showAddModal" class="add-btn">
          <span class="icon">+</span>
          添加用户
        </button>

        <button
            @click="batchDelete"
            :disabled="selectedUsers.length === 0"
            class="delete-btn"
        >
          <span class="icon">🗑️</span>
          批量删除 ({{ selectedUsers.length }})
        </button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card total">
        <div class="stat-number">{{ stats.total }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      <div class="stat-card active">
        <div class="stat-number">{{ stats.active }}</div>
        <div class="stat-label">启用用户</div>
      </div>
      <div class="stat-card inactive">
        <div class="stat-number">{{ stats.inactive }}</div>
        <div class="stat-label">禁用用户</div>
      </div>
      <div class="stat-card recent">
        <div class="stat-number">{{ recentUsers.length }}</div>
        <div class="stat-label">今日新增</div>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <div class="table-container">
      <table class="user-table">
        <thead>
        <tr>
          <th width="50">
            <input
                type="checkbox"
                v-model="selectAll"
                @change="handleSelectAll"
            >
          </th>
          <th>用户名</th>
          <th>真实姓名</th>
          <th>手机号</th>
          <th>邮箱</th>
          <th>角色</th>
          <th>状态</th>
          <th>最后登录</th>
          <th>创建时间</th>
          <th width="220">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="loading" class="loading-row">
          <td colspan="10" class="loading-cell">
            <div class="loading-spinner">加载中...</div>
          </td>
        </tr>
        <tr v-else-if="userList.length === 0" class="empty-row">
          <td colspan="10" class="empty-cell">
            <div class="empty-state">
              <span class="empty-icon">👤</span>
              <p>暂无用户数据</p>
            </div>
          </td>
        </tr>
        <tr v-else v-for="user in userList" :key="user.id" class="user-row">
          <td>
            <input
                type="checkbox"
                :value="user.id"
                v-model="selectedUsers"
            >
          </td>
          <td class="username">{{ user.username }}</td>
          <td class="real-name">{{ user.realName }}</td>
          <td>{{ user.phone || '-' }}</td>
          <td>{{ user.email || '-' }}</td>
          <td class="role-name">{{ user.roleName }}</td>
          <td>
              <span
                  class="status-badge"
                  :class="getStatusClass(user.status)"
                  @click="showStatusModal(user)"
              >
                {{ user.status === 1 ? '启用' : '禁用' }}
              </span>
          </td>
          <td class="last-login">
            {{ formatDateTime(user.lastLoginTime) }}
          </td>
          <td class="created-at">
            {{ formatDateTime(user.createdAt) }}
          </td>
          <td class="actions">
            <button @click="editUser(user)" class="edit-btn">编辑</button>
            <button @click="resetPassword(user)" class="reset-btn">重置密码</button>
            <button
                @click="deleteUser(user)"
                class="delete-btn"
                :disabled="user.id === 1"
            >
              删除
            </button>
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

    <!-- 添加/编辑用户弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditing ? '编辑用户' : '添加用户' }}</h3>
          <button @click="closeModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitForm" class="user-form">
          <div class="form-row">
            <div class="form-group">
              <label>用户名 *</label>
              <input
                  type="text"
                  v-model="userForm.username"
                  placeholder="请输入用户名"
                  required
                  :disabled="isEditing"
              >
            </div>

            <div class="form-group">
              <label>真实姓名 *</label>
              <input
                  type="text"
                  v-model="userForm.realName"
                  placeholder="请输入真实姓名"
                  required
              >
            </div>
          </div>

          <div class="form-row" v-if="!isEditing">
            <div class="form-group">
              <label>密码 *</label>
              <input
                  type="password"
                  v-model="userForm.password"
                  placeholder="请输入密码"
                  required
              >
            </div>

            <div class="form-group">
              <label>确认密码 *</label>
              <input
                  type="password"
                  v-model="userForm.confirmPassword"
                  placeholder="请再次输入密码"
                  required
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>手机号</label>
              <input
                  type="tel"
                  v-model="userForm.phone"
                  placeholder="请输入手机号"
              >
            </div>

            <div class="form-group">
              <label>邮箱</label>
              <input
                  type="email"
                  v-model="userForm.email"
                  placeholder="请输入邮箱"
              >
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>角色 *</label>
              <select v-model="userForm.roleId" required>
                <option value="">请选择角色</option>
                <option v-for="role in roleOptions"
                        :key="role.id"
                        :value="role.id">
                  {{ role.roleName }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>状态</label>
              <select v-model="userForm.status">
                <option value="1">启用</option>
                <option value="0">禁用</option>
              </select>
            </div>
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
          <h3>修改用户状态</h3>
          <button @click="closeStatusModal" class="close-btn">×</button>
        </div>

        <div class="status-form">
          <p>用户：{{ currentUser?.realName }} ({{ currentUser?.username }})</p>
          <p>当前状态：<span class="current-status">{{ currentUser?.status === 1 ? '启用' : '禁用' }}</span></p>

          <div class="form-group">
            <label>新状态：</label>
            <select v-model="newStatus">
              <option value="1">启用</option>
              <option value="0">禁用</option>
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

    <!-- 重置密码弹窗 -->
    <div v-if="showPasswordReset" class="modal-overlay" @click="closePasswordModal">
      <div class="modal-content small" @click.stop>
        <div class="modal-header">
          <h3>重置用户密码</h3>
          <button @click="closePasswordModal" class="close-btn">×</button>
        </div>

        <form @submit.prevent="submitPasswordReset" class="password-form">
          <p>用户：{{ currentUser?.realName }} ({{ currentUser?.username }})</p>

          <div class="form-group">
            <label>新密码 *</label>
            <input
                type="password"
                v-model="newPassword"
                placeholder="请输入新密码"
                required
            >
          </div>

          <div class="form-group">
            <label>确认密码 *</label>
            <input
                type="password"
                v-model="confirmNewPassword"
                placeholder="请再次输入新密码"
                required
            >
          </div>

          <div class="form-actions">
            <button type="button" @click="closePasswordModal" class="cancel-btn">取消</button>
            <button type="submit" :disabled="submitting" class="submit-btn">
              {{ submitting ? '重置中...' : '确认重置' }}
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
import { userApi } from '@/utils/api'
import type {
  User,
  UserQueryParams,
  UserListResponse,
  UserStats,
  UserForm,
  Role
} from '@/types'

// 响应式数据
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)
const userList = ref<User[]>([])
const roleOptions = ref<Role[]>([])
const selectedUsers = ref<number[]>([])
const selectAll = ref<boolean>(false)
const showModal = ref<boolean>(false)
const showStatusChange = ref<boolean>(false)
const showPasswordReset = ref<boolean>(false)
const isEditing = ref<boolean>(false)
const currentUser = ref<User | null>(null)
const newStatus = ref<number>(1)
const newPassword = ref<string>('')
const confirmNewPassword = ref<string>('')
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

// 分页数据
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)
const totalCount = ref<number>(0)
const totalPages = ref<number>(0)

// 统计数据
const stats = ref<UserStats>({
  total: 0,
  active: 0,
  inactive: 0
})

const recentUsers = ref<User[]>([])

// 搜索表单
const searchForm = reactive<UserQueryParams>({
  searchKeyword: '',
  roleId: undefined,
  status: undefined,
  page: 1,
  size: 10
})

// 用户表单
const userForm = reactive<UserForm>({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  roleId: null,
  status: 1
})

// 计算属性
const selectAllState = computed(() => {
  if (userList.value.length === 0) return false
  return selectedUsers.value.length === userList.value.length
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadUserList = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: currentPage.value,
      size: pageSize.value
    }

    const response = await userApi.getUserList(params)

    if (response.code === 200) {
      userList.value = response.data.users
      totalCount.value = response.data.total
      totalPages.value = response.data.totalPages
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('加载用户列表失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadRoleOptions = async (): Promise<void> => {
  try {
    const response = await userApi.getRoles()
    if (response.code === 200) {
      roleOptions.value = response.data
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

const loadStats = async (): Promise<void> => {
  try {
    const response = await userApi.getUserStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentUsers = async (): Promise<void> => {
  try {
    const response = await userApi.getRecentUsers(5)
    if (response.code === 200) {
      recentUsers.value = response.data
    }
  } catch (error) {
    console.error('加载最近用户失败:', error)
  }
}

const handleSearch = (): void => {
  currentPage.value = 1
  loadUserList()
}

const resetSearch = (): void => {
  Object.assign(searchForm, {
    searchKeyword: '',
    roleId: undefined,
    status: undefined
  })
  handleSearch()
}

const handleSelectAll = (): void => {
  if (selectAll.value) {
    selectedUsers.value = userList.value.map(user => user.id!).filter(id => id !== undefined)
  } else {
    selectedUsers.value = []
  }
}

const goToPage = (page: number): void => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadUserList()
  }
}

const handlePageSizeChange = (): void => {
  currentPage.value = 1
  searchForm.size = pageSize.value
  loadUserList()
}

const showAddModal = (): void => {
  isEditing.value = false
  resetForm()
  showModal.value = true
}

const editUser = (user: User): void => {
  isEditing.value = true
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    phone: user.phone || '',
    email: user.email || '',
    roleId: user.roleId,
    status: user.status
  })
  showModal.value = true
}

const resetForm = (): void => {
  Object.assign(userForm, {
    id: undefined,
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    phone: '',
    email: '',
    roleId: null,
    status: 1
  })
}

const closeModal = (): void => {
  showModal.value = false
  resetForm()
}

const submitForm = async (): Promise<void> => {
  if (!isEditing.value) {
    if (userForm.password !== userForm.confirmPassword) {
      showMessage('两次密码输入不一致', 'error')
      return
    }
  }

  submitting.value = true
  try {
    const response = isEditing.value
        ? await userApi.updateUser(userForm)
        : await userApi.addUser(userForm)

    if (response.code === 200) {
      showMessage(response.msg, 'success')
      closeModal()
      loadUserList()
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

const deleteUser = async (user: User): Promise<void> => {
  if (user.id === 1) {
    showMessage('系统管理员不能删除', 'error')
    return
  }

  if (!confirm(`确认删除用户 ${user.realName} (${user.username}) 吗？`)) return

  try {
    const response = await userApi.deleteUser(user.id!)
    if (response.code === 200) {
      showMessage('删除成功', 'success')
      loadUserList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('删除失败', 'error')
  }
}

const batchDelete = async (): Promise<void> => {
  if (selectedUsers.value.length === 0) return

  // 检查是否包含系统管理员
  if (selectedUsers.value.includes(1)) {
    showMessage('系统管理员不能删除', 'error')
    return
  }

  if (!confirm(`确认删除选中的 ${selectedUsers.value.length} 个用户吗？`)) return

  try {
    const response = await userApi.deleteUsers(selectedUsers.value)
    if (response.code === 200) {
      showMessage('批量删除成功', 'success')
      selectedUsers.value = []
      selectAll.value = false
      loadUserList()
      loadStats()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('批量删除失败', 'error')
  }
}

const showStatusModal = (user: User): void => {
  if (user.id === 1) {
    showMessage('系统管理员状态不能修改', 'error')
    return
  }
  currentUser.value = user
  newStatus.value = user.status
  showStatusChange.value = true
}

const closeStatusModal = (): void => {
  showStatusChange.value = false
  currentUser.value = null
  newStatus.value = 1
}

const updateStatus = async (): Promise<void> => {
  if (!currentUser.value || newStatus.value === currentUser.value.status) {
    closeStatusModal()
    return
  }

  submitting.value = true
  try {
    const response = await userApi.updateUserStatus(currentUser.value.id!, newStatus.value)
    if (response.code === 200) {
      showMessage('状态更新成功', 'success')
      closeStatusModal()
      loadUserList()
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

const resetPassword = (user: User): void => {
  currentUser.value = user
  newPassword.value = ''
  confirmNewPassword.value = ''
  showPasswordReset.value = true
}

const closePasswordModal = (): void => {
  showPasswordReset.value = false
  currentUser.value = null
  newPassword.value = ''
  confirmNewPassword.value = ''
}

const submitPasswordReset = async (): Promise<void> => {
  if (newPassword.value !== confirmNewPassword.value) {
    showMessage('两次密码输入不一致', 'error')
    return
  }

  if (newPassword.value.length < 6) {
    showMessage('密码长度不能少于6位', 'error')
    return
  }

  submitting.value = true
  try {
    const response = await userApi.resetPassword(currentUser.value!.id!, {
      password: newPassword.value
    })
    if (response.code === 200) {
      showMessage('密码重置成功', 'success')
      closePasswordModal()
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('密码重置失败', 'error')
  } finally {
    submitting.value = false
  }
}

const getStatusClass = (status: number): string => {
  return status === 1 ? 'active' : 'inactive'
}

// 生命周期
onMounted(() => {
  loadUserList()
  loadRoleOptions()
  loadStats()
  loadRecentUsers()
})

// 监听选中状态
import { watch } from 'vue'
watch(selectedUsers, () => {
  selectAll.value = selectAllState.value
}, { deep: true })
</script>

<style scoped>
.user-management {
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

.stat-card.recent {
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

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th,
.user-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.user-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.user-table tr:hover {
  background: #f8f9fa;
}

.username {
  font-weight: 600;
  color: #3498db;
}

.real-name {
  font-weight: 600;
  color: #2c3e50;
}

.role-name {
  color: #8e44ad;
}

.last-login,
.created-at {
  font-size: 12px;
  color: #7f8c8d;
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
  flex-wrap: wrap;
}

.actions .edit-btn,
.actions .reset-btn,
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

.actions .reset-btn {
  background: #f39c12;
  color: white;
}

.actions .reset-btn:hover {
  background: #e67e22;
}

.actions .delete-btn {
  background: #e74c3c;
  color: white;
}

.actions .delete-btn:hover:not(:disabled) {
  background: #c0392b;
}

.actions .delete-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
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


</style>

<style>
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

.user-form,
.status-form,
.password-form {
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

.form-group input:disabled {
  background: #f8f9fa;
  color: #6c757d;
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

.status-form p,
.password-form p {
  margin-bottom: 10px;
  color: #2c3e50;
}

.current-status {
  font-weight: 600;
  color: #e74c3c;
}

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

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
