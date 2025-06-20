<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>东软颐养中心管理系统</h1>
        <p>欢迎登录</p>
      </div>

      <div class="login-tabs">
        <button
            :class="['tab-btn', { active: activeTab === 'login' }]"
            @click="activeTab = 'login'"
        >
          登录
        </button>
        <button
            :class="['tab-btn', { active: activeTab === 'register' }]"
            @click="activeTab = 'register'"
        >
          注册
        </button>
      </div>

      <!-- 登录表单 -->
      <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input
              type="text"
              v-model="loginForm.username"
              placeholder="请输入用户名"
              required
          >
        </div>
        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              required
          >
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <!-- 注册表单 -->
      <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input
              type="text"
              v-model="registerForm.username"
              placeholder="请输入用户名"
              required
          >
        </div>
        <div class="form-group">
          <label>真实姓名</label>
          <input
              type="text"
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              required
          >
        </div>
        <div class="form-group">
          <label>密码</label>
          <input
              type="password"
              v-model="registerForm.password"
              placeholder="请输入密码"
              required
          >
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input
              type="password"
              v-model="registerForm.confirmPassword"
              placeholder="请确认密码"
              required
          >
        </div>
        <div class="form-group">
          <label>角色</label>
          <select v-model="registerForm.roleId" required>
            <option value="1">系统管理员</option>
            <option value="2">健康管家</option>
            <option value="3">医护人员</option>
          </select>
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div v-if="message" class="message" :class="messageType">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/utils/api'
import type { LoginRequest, RegisterRequest, User } from '@/types'

// 组合式API设置
const router = useRouter()

// 响应式数据
const activeTab = ref<'login' | 'register'>('login')
const loading = ref<boolean>(false)
const message = ref<string>('')
const messageType = ref<'success' | 'error'>('success')

const loginForm = reactive<LoginRequest>({
  username: '',
  password: ''
})

const registerForm = reactive<RegisterRequest>({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  roleId: 2 // 默认健康管家
})

// 方法定义
const showMessage = (msg: string, type: 'success' | 'error' = 'success'): void => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

const handleLogin = async (): Promise<void> => {
  loading.value = true
  try {
    const response = await userApi.login(loginForm)

    if (response.code === 200) {
      showMessage('登录成功！')
      // 存储用户信息
      localStorage.setItem('user', JSON.stringify(response.data.user))
      localStorage.setItem('token', response.data.token)

      // 跳转到管理页面
      setTimeout(() => {
        router.push('/admin')
      }, 1000)
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('登录失败，请检查网络连接', 'error')
  } finally {
    loading.value = false
  }
}

const handleRegister = async (): Promise<void> => {
  if (registerForm.password !== registerForm.confirmPassword) {
    showMessage('两次密码输入不一致', 'error')
    return
  }

  loading.value = true
  try {
    const response = await userApi.register(registerForm)

    if (response.code === 200) {
      showMessage('注册成功！请登录')
      activeTab.value = 'login'
      // 清空注册表单
      Object.assign(registerForm, {
        username: '',
        realName: '',
        password: '',
        confirmPassword: '',
        roleId: 2
      })
    } else {
      showMessage(response.msg, 'error')
    }
  } catch (error) {
    showMessage('注册失败，请检查网络连接', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  border-radius: 10px;
  padding: 40px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 10px;
}

.login-header p {
  color: #7f8c8d;
  font-size: 16px;
}

.login-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 1px solid #ecf0f1;
}

.tab-btn {
  flex: 1;
  padding: 12px;
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
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3498db;
}

.login-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.login-btn:hover:not(:disabled) {
  background: #2980b9;
}

.login-btn:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.message {
  margin-top: 15px;
  padding: 10px;
  border-radius: 5px;
  text-align: center;
  font-size: 14px;
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
</style>